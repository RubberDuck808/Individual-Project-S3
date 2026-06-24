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
  onWsAuthError,
} = {}) {
  const [hazards, setHazards] = useState([]);

  // Proximity-triggered voting 
  const [selectedHazard, setSelectedHazard] = useState(null);
  const [voteExpiresAt, setVoteExpiresAt] = useState(null);
  const [voteArmed, setVoteArmed] = useState(true);

  // Avoid stale selectedHazard in WS reducer
  const selectedHazardRef = useRef(null);
  const currentUsernameRef = useRef(currentUsername);
  
  useEffect(() => {
    selectedHazardRef.current = selectedHazard;
  }, [selectedHazard]);
  
  useEffect(() => {
    currentUsernameRef.current = currentUsername;
  }, [currentUsername]);

  useEffect(() => {
    if (!enabled) return;
    getAllHazards().then(setHazards).catch(console.error);
  }, [enabled]);

  const clearSelectionIfMatches = useCallback((hazardId) => {
    if (selectedHazardRef.current?.id === hazardId) {
      setSelectedHazard(null);
      setVoteExpiresAt(null);
    }
  }, []);
  
  const isSelectedHazard = useCallback((hazardId) => {
    return selectedHazardRef.current?.id === hazardId;
  }, []);

  const isCurrentUser = useCallback((hazardUsername) => {
    const username = currentUsernameRef.current;
    return username && String(hazardUsername) === String(username);
  }, []);

  // Helper function - doesn't need to be a callback since it doesn't use reactive values
  const getHazardUsername = (h) => {
    return h?.username ?? h?.createdByUsername ?? h?.reportedByUsername ?? null;
  };

  const clearSelectionIfFromCurrentUser = useCallback((h) => {
    const hazardUsername = getHazardUsername(h);
    if (!hazardUsername || !isSelectedHazard(h.id)) return;
    if (isCurrentUser(hazardUsername)) {
      setSelectedHazard(null);
      setVoteExpiresAt(null);
    }
  }, [isSelectedHazard, isCurrentUser]);
  
  const isHazardActive = useCallback((h) => {
    return h.status === "OPEN" || h.status === "VERIFIED";
  }, []);

  const handleUpsertEvent = useCallback((prev, h) => {
    if (!isHazardActive(h)) {
      clearSelectionIfMatches(h.id);
      return prev.filter((x) => x.id !== h.id);
    }

    clearSelectionIfFromCurrentUser(h);
    
    const idx = prev.findIndex((x) => x.id === h.id);
    if (idx === -1) {
      return [h, ...prev];
    }

    const next = [...prev];
    next[idx] = h;
    return next;
  }, [isHazardActive, clearSelectionIfMatches, clearSelectionIfFromCurrentUser]);
  
  const handleDeleteEvent = useCallback((prev, hazardId) => {
    clearSelectionIfMatches(hazardId);
    return prev.filter((x) => x.id !== hazardId);
  }, [clearSelectionIfMatches]);

  const processWsEvent = useCallback((prev, evt) => {
    if (evt.type === "UPSERT" && evt.hazard) {
      return handleUpsertEvent(prev, evt.hazard);
    }
    if (evt.type === "DELETE" && evt.hazardId != null) {
      return handleDeleteEvent(prev, evt.hazardId);
    }
    return prev;
  }, [handleUpsertEvent, handleDeleteEvent]);

  const onWsEvent = useCallback((evt) => {
    if (!evt?.type) return;
    setHazards((prev) => processWsEvent(prev, evt));
  }, [processWsEvent]);

  useHazardsWebSocket({
    enabled,
    onEvent: onWsEvent,
    onAuthError: onWsAuthError,
  });

  const isHazardFromCurrentUser = useCallback((h) => {
    if (!currentUsername || !h) return false;
    const hazardUsername = getHazardUsername(h);
    return hazardUsername && String(hazardUsername) === String(currentUsername);
  }, [currentUsername]);

  // Filter out hazards created by the current user (they can't vote on their own hazards)
  const eligibleHazards = useMemo(() => {
    if (!currentUsername) return hazards;

    return hazards.filter((h) => !isHazardFromCurrentUser(h));
  }, [hazards, currentUsername, isHazardFromCurrentUser]);

  const canTriggerProximityVote = useCallback(() => {
    return enabled && location && voteArmed && !selectedHazard;
  }, [enabled, location, voteArmed, selectedHazard]);

  const triggerVoteForHazard = useCallback((hazard) => {
    if (isHazardFromCurrentUser(hazard)) {
      return;
    }
    setSelectedHazard(hazard);
    setVoteExpiresAt(Date.now() + voteTimeSeconds * 1000);
    setVoteArmed(false);
  }, [isHazardFromCurrentUser, voteTimeSeconds]);

  // Proximity trigger
  useEffect(() => {
    if (!canTriggerProximityVote()) return;

    const found = findNearestHazardWithin(
      eligibleHazards,
      location,
      openDistanceMeters
    );
    if (!found) return;

    triggerVoteForHazard(found.hazard);
  }, [
    canTriggerProximityVote,
    eligibleHazards,
    location,
    openDistanceMeters,
    triggerVoteForHazard,
  ]);

  const closeVotePanelInternal = useCallback(() => {
    setSelectedHazard(null);
    setVoteExpiresAt(null);
    setVoteArmed(false);
  }, []);

  // Close vote panel if currently selected hazard is from current user
  useEffect(() => {
    if (!enabled || !selectedHazard || !currentUsername) return;
    if (isHazardFromCurrentUser(selectedHazard)) {
      closeVotePanelInternal();
    }
  }, [enabled, selectedHazard, currentUsername, isHazardFromCurrentUser, closeVotePanelInternal]);

  const checkAndRearmVote = useCallback(() => {
    if (!enabled || !location) return;
    const nearAny = !!findNearestHazardWithin(
      eligibleHazards,
      location,
      rearmDistanceMeters
    );
    if (!nearAny) {
      setVoteArmed(true);
    }
  }, [enabled, location, eligibleHazards, rearmDistanceMeters]);

  useEffect(() => {
    checkAndRearmVote();
  }, [checkAndRearmVote]);

  const getVoteTimeRemaining = useCallback((expiresAt) => {
    return expiresAt - Date.now();
  }, []);

  // Auto-close when vote timer expires
  useEffect(() => {
    if (!enabled || !voteExpiresAt || !selectedHazard) return;

    const msLeft = getVoteTimeRemaining(voteExpiresAt);
    if (msLeft <= 0) {
      closeVotePanelInternal();
      return;
    }

    const t = setTimeout(() => {
      closeVotePanelInternal();
    }, msLeft);

    return () => clearTimeout(t);
  }, [enabled, voteExpiresAt, selectedHazard, getVoteTimeRemaining, closeVotePanelInternal]);

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
