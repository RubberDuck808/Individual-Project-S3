import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import SearchBar from "../components/map/routing/SearchBar";
import UserLocationMap from "../components/map/tracking/UserLocationMap";
import HazardFormPanel from "../components/map/hazards/HazardFormPanel";
import VotePanel from "../components/map/votes/VotePanel";
import { useTheme } from "../context/ThemeContext";
import { useLocation } from "../context/LocationContext";
import { getCategoriesCached } from "../api/hazardApi";
import { renderHazardIcon } from "../utils/renderHazardIcon";

import Cone from "../assets/icons/report-cone.svg?react";
import ReportButton from "../components/map/hazards/ReportButton";

import "mapbox-gl/dist/mapbox-gl.css";

import { useTrip } from "../components/map/trip/useTrip";
import TripControls from "../components/map/trip/TripControls";
import TripNavigationHUD from "../components/map/trip/TripNavigationHUD";

import { useHazardsState } from "../hooks/useHazardsState";
import { getStoredUser } from "../api/auth";

const OPEN_DISTANCE_METERS = 150;
const REARM_DISTANCE_METERS = 50;
const VOTE_TIME_SECONDS = 15;
const TRIP_ZOOM = 17;
const TRIP_PITCH = 60;

export default function MapPage() {
  const mapRef = useRef(null);
  const location = useLocation();
  const { darkMode } = useTheme();
  const trip = useTrip();

  const me = getStoredUser();
  const currentUsername = me?.username ?? null;

  const [reportOpen, setReportOpen] = useState(false);
  const [routeInfo, setRouteInfo] = useState(null);

  const [isFollowing, setIsFollowing] = useState(true);

  const [closeSearchSignal, setCloseSearchSignal] = useState(0);
  const [searchClearSignal, setSearchClearSignal] = useState(0);

  const [nav, setNav] = useState({ steps: [], activeStepIndex: 0 });

  const [categories, setCategories] = useState([]);

  const { hazards, selectedHazard, voteExpiresAt, closeVotePanel } =
    useHazardsState({
      location,
      currentUsername,
      openDistanceMeters: OPEN_DISTANCE_METERS,
      rearmDistanceMeters: REARM_DISTANCE_METERS,
      voteTimeSeconds: VOTE_TIME_SECONDS,
      enabled: true,
    });

  useEffect(() => {
    getCategoriesCached().then(setCategories).catch(console.error);
  }, []);

  const norm = useCallback((s) => (s ?? "").trim().toLowerCase(), []);

  const categoriesByName = useMemo(() => {
    return Object.fromEntries(categories.map((c) => [norm(c.name), c]));
  }, [categories, norm]);

  const renderHazardIconWithLookup = useCallback(
    (haz) => {
      const categoryName =
        typeof haz?.category === "string" ? haz.category : haz?.category?.name;

      const categoryObj = categoriesByName[norm(categoryName)];

      return renderHazardIcon({
        ...haz,
        category: categoryObj ?? haz.category,
      });
    },
    [categoriesByName, norm]
  );

  const openHazardForm = useCallback(() => {
    if (!location) {
      alert("Still determining your location…");
      return;
    }
    setReportOpen(true);
  }, [location]);

  const resetRoutePreview = useCallback(() => {
    setRouteInfo(null);
    setNav({ steps: [], activeStepIndex: 0 });

    mapRef.current?.clearRoute?.();

    setCloseSearchSignal((x) => x + 1);
    setSearchClearSignal((x) => x + 1);
  }, []);

  const handleSearchSelect = useCallback((place) => {
    const [lng, lat] = place.center;
    setIsFollowing(false);
    mapRef.current?.goToAndRoute?.(lng, lat);
    setCloseSearchSignal((x) => x + 1);
  }, []);

  const suppressSearchDropdown = !!routeInfo || trip.isActive;

  const hideFab =
    reportOpen ||
    (!!selectedHazard && !!voteExpiresAt) ||
    (!!routeInfo && !trip.isActive);

  const handleEndTrip = useCallback(async () => {
    if (!location) return;

    try {
      await trip.endTrip({
        currentLat: location.lat,
        currentLng: location.lng,
      });

      resetRoutePreview();
    } catch (e) {
      alert(e?.message || "Failed to save trip");
    }
  }, [location, trip, resetRoutePreview]);

  const handleCancelTrip = useCallback(() => {
    trip.cancelTrip();
    resetRoutePreview();
  }, [trip, resetRoutePreview]);

  return (
    <div
      className={`h-full w-full flex flex-col transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      {!trip.isActive && (
        <div className="w-full fixed top-0 left-0 z-50 px-4 py-3 pointer-events-none bg-transparent">
          <div className="pointer-events-auto w-fit">
            <SearchBar
              onSelect={handleSearchSelect}
              onCleared={resetRoutePreview}
              userLocation={location}
              darkMode={darkMode}
              clearSignal={searchClearSignal}
              closeDropdownSignal={closeSearchSignal}
              suppressDropdown={suppressSearchDropdown}
            />
          </div>
        </div>
      )}

      <div className="flex-1 relative">
        {trip.isActive && (
          <TripNavigationHUD
            steps={nav.steps}
            activeStepIndex={nav.activeStepIndex}
            userLocation={location}
            submitting={trip.submitting}
            onEnd={handleEndTrip}
            onCancel={handleCancelTrip}
          />
        )}

        <UserLocationMap
          ref={mapRef}
          hazards={hazards}
          renderHazardIcon={renderHazardIconWithLookup}
          onHazardClick={() => setIsFollowing(false)}
          followUser={isFollowing}
          onFollowChange={setIsFollowing}
          onUserMove={trip.onUserMove}
          onRoutePreview={setRouteInfo}
          onNavigation={setNav}
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

        {routeInfo && !trip.isActive && (
          <div
            className={`fixed right-4 top-24 rounded-xl p-4 w-72 z-40 shadow-lg transition-colors duration-300 ${
              darkMode ? "bg-gray-800 text-white" : "bg-white text-gray-900"
            }`}
          >
            <div className="flex items-start justify-between gap-3">
              <p className="font-bold text-lg">Route Preview</p>
              <button
                onClick={resetRoutePreview}
                className="rounded-full p-2 text-gray-500 hover:bg-black/5 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
                aria-label="Close route preview"
                title="Close"
              >
                ✕
              </button>
            </div>

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

  requestAnimationFrame(() => {
    mapRef.current?.recenterToUserWithZoom?.(TRIP_ZOOM, TRIP_PITCH);
    window.setTimeout(() => {
      mapRef.current?.recenterToUserWithZoom?.(TRIP_ZOOM, TRIP_PITCH);
    }, 250);
  });
}}

              onEnd={handleEndTrip}
              onCancel={handleCancelTrip}
            />
          </div>
        )}

        {selectedHazard && voteExpiresAt && (
          <VotePanel
            hazard={selectedHazard}
            userLocation={location}
            allowedDistanceMeters={250}
            expiresAt={voteExpiresAt}
            onClose={closeVotePanel}
          />
        )}

        {!hideFab && (
          <ReportButton onClick={openHazardForm}>
            <Cone className="h-[88px] w-[88px]" />
          </ReportButton>
        )}

        {reportOpen && (
          <HazardFormPanel
            coords={location}
            onClose={() => setReportOpen(false)}
          />
        )}
      </div>
    </div>
  );
}
