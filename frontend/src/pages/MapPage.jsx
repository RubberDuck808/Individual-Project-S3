import React, { useEffect, useRef, useState, useCallback } from "react";
import Navbar from "../components/Navbar";
import UserLocationMap from "../components/map/UserLocationMap";
import HazardFormPanel from "../components/map/HazardFormPanel";
import VotePanel from "../components/map/VotePanel";
import { useTheme } from "../context/ThemeContext";
import { useLocation } from "../context/LocationContext";
import { getCategoriesCached, getAllHazards } from "../api/hazardApi";
import { useHazardsWebSocket } from "../components/map/useHazardsWebSocket";

import "mapbox-gl/dist/mapbox-gl.css";
import { iconMap } from "../utils/iconMap";

import { useTrip } from "../components/trip/useTrip";
import TripControls from "../components/trip/TripControls";

const OPEN_DISTANCE_METERS = 150;
const VOTE_TIME_SECONDS = 15;

// Haversine distance in meters
function haversineMeters(lat1, lng1, lat2, lng2) {
  const R = 6371000;
  const toRad = (d) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;

  return 2 * R * Math.asin(Math.sqrt(a));
}

// returns nearest hazard within maxMeters, else null
function findNearestHazardWithin(hazards, location, maxMeters) {
  if (!location) return null;

  let best = null;
  let bestD = Infinity;

  for (const h of hazards) {
    if (!h || h.latitude == null || h.longitude == null) continue;

    const d = haversineMeters(location.lat, location.lng, h.latitude, h.longitude);
    if (d <= maxMeters && d < bestD) {
      best = h;
      bestD = d;
    }
  }

  return best ? { hazard: best, distance: bestD } : null;
}

export default function MapPage() {
  const mapRef = useRef(null);
  const location = useLocation();

  const [hazards, setHazards] = useState([]);
  const [reportOpen, setReportOpen] = useState(false);
  const [routeInfo, setRouteInfo] = useState(null);

  // Proximity-triggered voting (latched)
  const [selectedHazard, setSelectedHazard] = useState(null);
  const [voteExpiresAt, setVoteExpiresAt] = useState(null);

  const [voteArmed, setVoteArmed] = useState(true);

  const [isFollowing, setIsFollowing] = useState(true);
  const [closeSearchSignal, setCloseSearchSignal] = useState(0);

  const { darkMode } = useTheme();
  const trip = useTrip();

  useEffect(() => {
    getCategoriesCached().catch(console.error);
    getAllHazards().then(setHazards).catch(console.error);
  }, []);

  // WS event handler (stable reference)
  const handleHazardEvent = useCallback(
    (evt) => {
      setHazards((prev) => {
        if (!evt || !evt.type) return prev;

        if (evt.type === "UPSERT" && evt.hazard) {
          const h = evt.hazard;

          // keep map showing only OPEN/VERIFIED hazards
          const isActive = h.status === "OPEN" || h.status === "VERIFIED";
          if (!isActive) {
            // If the hazard we are voting on disappears/turns inactive, close panel
            if (selectedHazard?.id === h.id) {
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
          if (selectedHazard?.id === evt.hazardId) {
            setSelectedHazard(null);
            setVoteExpiresAt(null);
          }
          return prev.filter((x) => x.id !== evt.hazardId);
        }

        return prev;
      });
    },
    [selectedHazard]
  );

  useHazardsWebSocket({
    enabled: true,
    onEvent: handleHazardEvent,
  });

  // "Arming": prevents immediate retrigger spam while still in range
  // Trigger: only when armed AND no panel already open
  useEffect(() => {
    if (!location) return;
    if (!voteArmed) return;
    if (selectedHazard) return; // already open -> ignore proximity checks

    const found = findNearestHazardWithin(hazards, location, OPEN_DISTANCE_METERS);
    if (!found) return;

    // Latch the hazard + start timer
    setSelectedHazard(found.hazard);
    setVoteExpiresAt(Date.now() + VOTE_TIME_SECONDS * 1000);

    // Disarm so we don’t retrigger repeatedly while still near
    setVoteArmed(false);
  }, [hazards, location, voteArmed, selectedHazard]);

  // Re-arm ONLY when user is not near ANY hazard (within 50m).
  useEffect(() => {
    if (!location) return;

    const nearAny = !!findNearestHazardWithin(hazards, location, OPEN_DISTANCE_METERS);
    if (!nearAny) {
      setVoteArmed(true);
    }
  }, [hazards, location]);

  // Auto-close when 15s expires
  useEffect(() => {
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
  }, [voteExpiresAt, selectedHazard]);

  const openHazardForm = () => {
    if (!location) {
      alert("Still determining your location…");
      return;
    }
    setReportOpen(true);
  };

  const handleSearchSelect = (place) => {
    const [lng, lat] = place.center;
    setIsFollowing(false);

    mapRef.current?.goToAndRoute?.(lng, lat);
    setCloseSearchSignal((x) => x + 1);
  };

  const suppressSearchDropdown = !!routeInfo || trip.isActive;

  return (
    <div
      className={`h-full w-full flex flex-col transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      <Navbar
        onSearchSelect={handleSearchSelect}
        userLocation={location}
        closeSearchSignal={closeSearchSignal}
        suppressSearchDropdown={suppressSearchDropdown}
      />

      <div className="flex-1 relative">
        <UserLocationMap
          ref={mapRef}
          hazards={hazards}
          renderHazardIcon={(hazard) => {
            const el = document.createElement("div");
            el.className = "w-10 h-10 flex items-center justify-center cursor-pointer";

            const iconFilename = hazard.category?.icon;
            const resolvedIcon = iconMap[iconFilename];

            if (resolvedIcon) {
              const img = document.createElement("img");
              img.src = resolvedIcon;
              img.className = "w-full h-full object-contain";
              el.appendChild(img);
            } else {
              el.innerHTML = "⚠️";
              el.style.fontSize = "26px";
            }

            return el;
          }}
          onHazardClick={() => setIsFollowing(false)}
          followUser={isFollowing}
          onFollowChange={setIsFollowing}
          onUserMove={trip.onUserMove}
          onRoutePreview={setRouteInfo}
        />

        {!isFollowing && (
          <button
            onClick={() => {
              setIsFollowing(true);
              mapRef.current?.recenterToUser?.();
            }}
            className="fixed bottom-36 left-1/2 -translate-x-1/2 bg-blue-600 text-white px-6 py-2 rounded-full shadow-lg hover:bg-blue-700 transition z-40"
          >
            Recenter
          </button>
        )}

        {routeInfo && (
          <div
            className={`fixed right-4 top-24 rounded-xl p-4 w-72 z-40 shadow-lg transition-colors duration-300 ${
              darkMode ? "bg-gray-800 text-white" : "bg-white text-gray-900"
            }`}
          >
            <p className="font-bold text-lg">Route Preview</p>
            <p>Distance: {(routeInfo.distance / 1000).toFixed(1)} km</p>
            <p>Duration: {(routeInfo.duration / 60).toFixed(0)} min</p>

            <TripControls
              isActive={trip.isActive}
              distanceKm={trip.display?.distanceKm ?? 0}
              submitting={trip.submitting}
              canStart={!!routeInfo && !!location}
              onStart={() => {
                if (!location || !routeInfo) return;

                if (routeInfo.endLat == null || routeInfo.endLng == null) {
                  alert("Missing destination coords (endLat/endLng) in routeInfo.");
                  return;
                }

                setCloseSearchSignal((x) => x + 1);

                trip.startTrip({
                  startLat: location.lat,
                  startLng: location.lng,
                  endLat: routeInfo.endLat,
                  endLng: routeInfo.endLng,
                });

                setIsFollowing(true);
              }}
              onEnd={async () => {
                if (!location) return;

                try {
                  setCloseSearchSignal((x) => x + 1);

                  await trip.endTrip({
                    currentLat: location.lat,
                    currentLng: location.lng,
                  });

                  setRouteInfo(null);
                  mapRef.current?.clearRoute?.();
                } catch (e) {
                  alert(e?.message || "Failed to save trip");
                }
              }}
              onCancel={() => {
                trip.cancelTrip();
                setRouteInfo(null);
                mapRef.current?.clearRoute?.();
                setCloseSearchSignal((x) => x + 1);
              }}
            />
          </div>
        )}

        {/* Vote Panel: stays open until vote/close/timer */}
        {selectedHazard && voteExpiresAt && (
          <VotePanel
            hazard={selectedHazard}
            userLocation={location}
            allowedDistanceMeters={250}
            expiresAt={voteExpiresAt}
            onClose={() => {
              setSelectedHazard(null);
              setVoteExpiresAt(null);
            }}
          />
        )}

        <button
          onClick={openHazardForm}
          className="fixed bottom-20 right-7 bg-red-600 text-white px-9 py-3 rounded-full shadow-lg hover:bg-red-700 transition z-40"
        >
          Report
        </button>

        {reportOpen && (
          <HazardFormPanel coords={location} onClose={() => setReportOpen(false)} />
        )}
      </div>
    </div>
  );
}
