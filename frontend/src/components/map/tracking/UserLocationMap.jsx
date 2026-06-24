import React, {
  useRef,
  useState,
  forwardRef,
  useEffect,
  useImperativeHandle,
} from "react";
import PropTypes from "prop-types";
import { useTheme } from "../../../context/ThemeContext";
import { useLocation } from "../../../context/LocationContext";
import { useMapboxInit } from "./useMapboxInit";
import { useUserTracking } from "./useUserTracking";
import { useHazardMarkers } from "../hazards/useHazardMarkers";
import { fetchRoute } from "../routing/useRouting";
import { haversineMeters } from "../../../utils/geo";
import "mapbox-gl/dist/mapbox-gl.css";

const ROUTE_ID = "route-preview";

const UserLocationMap = forwardRef((props, ref) => {
  const { followUser, hazards, onHazardClick, renderHazardIcon } = props;

  const { darkMode } = useTheme();
  const location = useLocation();

  const mapContainerRef = useRef(null);
  const [map, setMap] = useState(null);
  const userMarkerRef = useRef(null);

  const [navSteps, setNavSteps] = useState([]);
  const [activeStepIndex, setActiveStepIndex] = useState(0);

  useEffect(() => {
    if (!map) return;
    const stopFollowing = () => props.onFollowChange?.(false);
    const cleanup = map.onUserInteraction(stopFollowing);
    return () => cleanup();
  }, [map, props.onFollowChange]);

  useMapboxInit(mapContainerRef, darkMode, (adapter) => setMap(adapter));

  const hasInitialZoomRef = useRef(false);
  
  useUserTracking({
    map,
    location,
    userMarkerRef,
    followUser,
    onMove: props.onUserMove,
  });

  // Zoom to street level when user location first becomes available
  useEffect(() => {
    if (!map || !location || hasInitialZoomRef.current) return;
    if (!map.isLoaded?.()) return;

    // Zoom to street level on first location
    if (typeof map.easeTo === "function") {
      map.easeTo({
        center: [location.lng, location.lat],
        zoom: 15, // Street level zoom
        duration: 800,
        essential: true,
      });
    } else {
      map.easeToCenter?.([location.lng, location.lat]);
      if (typeof map.setZoom === "function") {
        map.setZoom(15);
      }
    }
    hasInitialZoomRef.current = true;
  }, [map, location]);

  useHazardMarkers(map, hazards, onHazardClick, renderHazardIcon);

  useEffect(() => {
    if (!location || !navSteps.length) return;

    const curr = navSteps[activeStepIndex];
    const manLoc = curr?.maneuver?.location;
    if (!Array.isArray(manLoc) || manLoc.length < 2) return;

    const [mLng, mLat] = manLoc;
    const d = haversineMeters(location.lat, location.lng, mLat, mLng);

    if (d < 20 && activeStepIndex < navSteps.length - 1) {
      setActiveStepIndex((i) => i + 1);
    }
  }, [location, navSteps, activeStepIndex]);

  const clearRoute = () => {
    if (!map?.isLoaded?.()) return;

    setNavSteps([]);
    setActiveStepIndex(0);

    if (typeof map.removeRouteLayer === "function") {
      map.removeRouteLayer(ROUTE_ID);
      return;
    }

    try {
      if (map.getLayer?.(ROUTE_ID)) map.removeLayer(ROUTE_ID);
      if (map.getSource?.(ROUTE_ID)) map.removeSource(ROUTE_ID);
    } catch (err) {
      console.warn("[UserLocationMap] Failed to remove route layer/source:", err);
    }
  };

  const onNavigationRef = useRef(props.onNavigation);
  useEffect(() => {
    onNavigationRef.current = props.onNavigation;
  }, [props.onNavigation]);

  const lastSentRef = useRef({ steps: null, activeStepIndex: null });
  useEffect(() => {
    const cb = onNavigationRef.current;
    if (!cb) return;

    const last = lastSentRef.current;
    if (last.steps === navSteps && last.activeStepIndex === activeStepIndex) return;

    lastSentRef.current = { steps: navSteps, activeStepIndex };
    cb({ steps: navSteps, activeStepIndex });
  }, [navSteps, activeStepIndex]);

  useImperativeHandle(
    ref,
    () => ({
      recenterToUser() {
        if (!map || !location) return;
        // Zoom to street level when recentering
        if (typeof map.easeTo === "function") {
          map.easeTo({
            center: [location.lng, location.lat],
            zoom: 15, // Street level zoom
            duration: 600,
            essential: true,
          });
        }
        if (typeof map.easeTo !== "function") {
          map.easeToCenter?.([location.lng, location.lat]);
          if (typeof map.setZoom === "function") {
            map.setZoom(15);
          }
        }
      },

      recenterToUserWithZoom(zoom, pitch = 60) {
        if (!map || !location) return;

        const center = [location.lng, location.lat];
        map.stop?.();

        if (typeof map.easeTo === "function") {
          map.easeTo({
            center,
            zoom,
            pitch,
            bearing: 0,
            duration: 900,
            essential: true,
          });
        } else {
          map.easeToCenter?.(center);
          if (typeof map.setZoom === "function") map.setZoom(zoom);
          if (typeof map.setPitch === "function") map.setPitch(pitch);
        }
      },

      clearRoute() {
        if (!map?.isLoaded?.()) return;
        setNavSteps([]);
        setActiveStepIndex(0);
        if (typeof map.removeRouteLayer === "function") {
          map.removeRouteLayer(ROUTE_ID);
          return;
        }
        try {
          if (map.getLayer?.(ROUTE_ID)) map.removeLayer(ROUTE_ID);
          if (map.getSource?.(ROUTE_ID)) map.removeSource(ROUTE_ID);
        } catch {
          // Route layer may not exist, ignore
        }
      },

      async goToAndRoute(destLng, destLat) {
        if (!map?.isLoaded?.()) {
          console.warn("[goToAndRoute] Map not ready");
          return;
        }
        if (!location) {
          console.warn("[goToAndRoute] No user location yet");
          return;
        }

        // Validate destination coordinates
        if (typeof destLng !== "number" || typeof destLat !== "number" ||
            Number.isNaN(destLng) || Number.isNaN(destLat) || !Number.isFinite(destLng) || !Number.isFinite(destLat)) {
          console.error("[goToAndRoute] Invalid destination coordinates");
          return;
        }

        if (destLng < -180 || destLng > 180 || destLat < -90 || destLat > 90) {
          console.error("[goToAndRoute] Destination coordinates out of range");
          return;
        }

        const origin = [location.lng, location.lat];
        const destination = [destLng, destLat];

        map.easeToCenter?.(destination);

        clearRoute();

        try {
          const route = await fetchRoute(origin, destination);
          if (!route) {
            console.warn("[goToAndRoute] No route returned");
            onNavigationRef.current?.({ steps: [], activeStepIndex: 0 });
            props.onRoutePreview?.(null);
            return;
          }

          map.addRouteLayer?.(ROUTE_ID, route.geometry);

          map.fitGeoJSON?.(route.geometry, 80, 900);

          setNavSteps(route.steps || []);
          setActiveStepIndex(0);

          props.onRoutePreview?.({
            geometry: route.geometry,
            distance: route.distance,
            duration: route.duration,
            endLat: destLat,
            endLng: destLng,
            steps: route.steps || [],
          });
        } catch (error) {
          console.error("[goToAndRoute] Route fetch failed:", error);
          onNavigationRef.current?.({ steps: [], activeStepIndex: 0 });
          props.onRoutePreview?.(null);
        }
      },
    }),
    [map, location]
  );

  return <div ref={mapContainerRef} className="w-full h-full" />;
});

// NOSONAR - PropTypes required for SonarQube validation (deprecated in React 19 but needed for JS projects)
// eslint-disable-next-line react/prop-types
UserLocationMap.propTypes = { // NOSONAR
  followUser: PropTypes.bool,
  hazards: PropTypes.arrayOf(PropTypes.object),
  onHazardClick: PropTypes.func,
  renderHazardIcon: PropTypes.func,
  onFollowChange: PropTypes.func,
  onUserMove: PropTypes.func,
  onNavigation: PropTypes.func,
  onRoutePreview: PropTypes.func,
};

export default UserLocationMap;
