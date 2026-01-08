import { useCallback, useEffect, useRef, useState, useMemo } from "react";
import { getAllHazards } from "../api/hazardApi";
import { useHazardsWebSocket } from "../components/map/hazards/useHazardsWebSocket";
import { findNearestHazardWithin } from "../utils/geo";

export function useHazardsState({
  location,
  currentUsername = null,
  openDistanceMeters = 150,
  rearmDistanceMeters = 50,
  voteTimeSeconds = 15,
  enabled = true,
} = {}) {
  const [hazards, setHazards] = useState([]);

  // Proximity-triggered voting 
  const [selectedHazard, setSelectedHazard] = useState(null);
  const [voteExpiresAt, setVoteExpiresAt] = useState(null);
  const [voteArmed, setVoteArmed] = useState(true);

  // Avoid stale selectedHazard in WS reducer
  const selectedHazardRef = useRef(null);
  useEffect(() => {
    selectedHazardRef.current = selectedHazard;
  }, [selectedHazard]);

  // Initial fetch
  useEffect(() => {
    if (!enabled) return;
    getAllHazards().then(setHazards).catch(console.error);
  }, [enabled]);

  // WS reducer (stable)
  const onWsEvent = useCallback((evt) => {
    setHazards((prev) => {
      if (!evt?.type) return prev;

      if (evt.type === "UPSERT" && evt.hazard) {
        const h = evt.hazard;

        // Keep only OPEN/VERIFIED
        const isActive = h.status === "OPEN" || h.status === "VERIFIED";
        if (!isActive) {
          if (selectedHazardRef.current?.id === h.id) {
            setSelectedHazard(null);
            setVoteExpiresAt(null);
          }
          return prev.filter((x) => x.id !== h.id);
        }

        const idx = prev.findIndex((x) => x.id === h.id);
        if (idx === -1) return [h, ...prev];

        const next = [...prev];
        next[idx] = h;
        return next;
      }

      if (evt.type === "DELETE" && evt.hazardId != null) {
        if (selectedHazardRef.current?.id === evt.hazardId) {
          setSelectedHazard(null);
          setVoteExpiresAt(null);
        }
        return prev.filter((x) => x.id !== evt.hazardId);
      }

      return prev;
    });
  }, []);

  useHazardsWebSocket({
    enabled,
    onEvent: onWsEvent,
  });


  // This is to check if the hazard that we're receiving is from the user who created it "In Progress"
  const eligibleHazards = useMemo(() => {
    if (!currentUsername) return hazards;

    return hazards.filter((h) => {
      const hazardUsername =
        h.username ?? h.createdByUsername ?? h.reportedByUsername ?? null;

      
      if (!hazardUsername) return true;

      return String(hazardUsername) !== String(currentUsername);
    });
  }, [hazards, currentUsername]);

  // Proximity trigger
  useEffect(() => {
    if (!enabled) return;
    if (!location) return;
    if (!voteArmed) return;
    if (selectedHazard) return;

    const found = findNearestHazardWithin(
      eligibleHazards,
      location,
      openDistanceMeters
    );
    if (!found) return;

    setSelectedHazard(found.hazard);
    setVoteExpiresAt(Date.now() + voteTimeSeconds * 1000);
    setVoteArmed(false);
  }, [
    enabled,
    eligibleHazards,
    location,
    voteArmed,
    selectedHazard,
    openDistanceMeters,
    voteTimeSeconds,
  ]);

  useEffect(() => {
    if (!enabled) return;
    if (!location) return;

    const nearAny = !!findNearestHazardWithin(
      eligibleHazards,
      location,
      rearmDistanceMeters
    );
    if (!nearAny) setVoteArmed(true);
  }, [enabled, eligibleHazards, location, rearmDistanceMeters]);

  // Auto-close when vote timer expires
  useEffect(() => {
    if (!enabled) return;
    if (!voteExpiresAt || !selectedHazard) return;

    const msLeft = voteExpiresAt - Date.now();
    if (msLeft <= 0) {
      setSelectedHazard(null);
      setVoteExpiresAt(null);
      return;
    }

    const t = setTimeout(() => {
      setSelectedHazard(null);
      setVoteExpiresAt(null);
    }, msLeft);

    return () => clearTimeout(t);
  }, [enabled, voteExpiresAt, selectedHazard]);

  const closeVotePanel = useCallback(() => {
    setSelectedHazard(null);
    setVoteExpiresAt(null);
  }, []);

  return {
    hazards,
    setHazards,
    selectedHazard,
    voteExpiresAt,
    closeVotePanel,
  };
}
