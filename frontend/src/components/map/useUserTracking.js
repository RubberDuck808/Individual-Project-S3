// src/components/map/useUserTracking.js
import { useEffect } from "react";

export function useUserTracking({ map, location, userMarkerRef, followUser, onMove }) {
  useEffect(() => {
    if (!map) return;
    if (!map.isLoaded()) return;
    if (!location) return;

    const coords = [location.lng, location.lat];

    if (!userMarkerRef.current) {
      userMarkerRef.current = map.addColorMarker("red", coords);
    } else {
      userMarkerRef.current.setLngLat(coords);
    }

    if (followUser) {
      map.easeToCenter(coords);
    }

    // ✅ notify trip tracker
    if (onMove) onMove(location);
  }, [map, location, followUser, onMove]);
}
