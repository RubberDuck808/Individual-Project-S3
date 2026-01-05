// src/components/map/UserLocationMap.jsx
import React, {
  useRef,
  useState,
  forwardRef,
  useEffect,
  useImperativeHandle,
} from "react";
import { useTheme } from "../../context/ThemeContext";
import { useLocation } from "../../context/LocationContext";
import { useMapboxInit } from "./useMapboxInit";
import { useUserTracking } from "./useUserTracking";
import { useHazardMarkers } from "./useHazardMarkers";
import { fetchRoute } from "./useRouting";
import "mapbox-gl/dist/mapbox-gl.css";

const ROUTE_ID = "route-preview";

const UserLocationMap = forwardRef((props, ref) => {
  const { followUser, hazards, onHazardClick, renderHazardIcon } = props;

  const { darkMode } = useTheme();
  const location = useLocation();

  const mapContainerRef = useRef(null);
  const [map, setMap] = useState(null);
  const userMarkerRef = useRef(null);

  // Stop follow mode on manual interaction
  useEffect(() => {
    if (!map) return;
    const stopFollowing = () => props.onFollowChange?.(false);
    const cleanup = map.onUserInteraction(stopFollowing);
    return () => cleanup();
  }, [map, props]);

  // Init map
  useMapboxInit(mapContainerRef, darkMode, (adapter) => setMap(adapter));

  // Track user position
  useUserTracking({
    map,
    location,
    userMarkerRef,
    followUser,
    onMove: props.onUserMove,
  });

  // Render hazard markers
  useHazardMarkers(map, hazards, onHazardClick, renderHazardIcon);

  // Helper: clear route line from the map
  const clearRoute = () => {
    if (!map || !map.isLoaded()) return;

    // Your adapter likely has this; if not, see note below.
    if (typeof map.removeRouteLayer === "function") {
      map.removeRouteLayer(ROUTE_ID);
      return;
    }

    // Fallback if adapter exposes raw Mapbox methods:
    try {
      if (map.getLayer?.(ROUTE_ID)) map.removeLayer(ROUTE_ID);
      if (map.getSource?.(ROUTE_ID)) map.removeSource(ROUTE_ID);
    } catch (e) {
      // ignore
    }
  };

  useImperativeHandle(
    ref,
    () => ({
      recenterToUser() {
        if (!map || !location) return;
        map.easeToCenter([location.lng, location.lat]);
      },

      clearRoute() {
        clearRoute();
      },

      async goToAndRoute(destLng, destLat) {
        if (!map || !map.isLoaded()) {
          console.warn("[goToAndRoute] Map not ready");
          return;
        }
        if (!location) {
          console.warn("[goToAndRoute] No user location yet");
          return;
        }

        const origin = [location.lng, location.lat];
        const destination = [destLng, destLat];

        // Move camera towards destination
        map.easeToCenter(destination);

        // ✅ Clear any previous preview route before drawing a new one
        clearRoute();

        // Fetch route from Mapbox
        const route = await fetchRoute(origin, destination);
        if (!route) {
          console.warn("[goToAndRoute] No route returned");
          props.onRoutePreview?.(null);
          return;
        }

        // Draw route
        map.addRouteLayer(ROUTE_ID, route.geometry);
        map.fitGeoJSON(route.geometry, 80, 900);

        // Send preview info to MapPage
        props.onRoutePreview?.({
          geometry: route.geometry,
          distance: route.distance,
          duration: route.duration,
          endLat: destLat,
          endLng: destLng,
        });
      },
    }),
    [map, location, props]
  );

  return <div ref={mapContainerRef} className="w-full h-full" />;
});

export default UserLocationMap;
