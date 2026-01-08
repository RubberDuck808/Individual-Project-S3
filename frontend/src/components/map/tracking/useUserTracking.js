import { useEffect, useRef } from "react";

export function useUserTracking({ map, location, userMarkerRef, followUser, onMove }) {
  const elRef = useRef(null);

  useEffect(() => {
    if (!map || !location) return;

    const coords = [location.lng, location.lat];

    const ensureMarkerAndUpdate = () => {
      if (!elRef.current) {
        const el = document.createElement("div");
        el.className = "user-marker";

        // Blue dot
        el.style.width = "20px";
        el.style.height = "20px";
        el.style.borderRadius = "50%";
        el.style.backgroundColor = "#3b82f6";
        el.style.border = "3px solid white";
        el.style.boxShadow = "0 0 6px rgba(0,0,0,0.35)";
        el.style.boxSizing = "border-box";

        elRef.current = el;
      }

      // Create marker
      if (!userMarkerRef.current) {
        userMarkerRef.current = map.addHTMLMarker(elRef.current, coords);
      } else if (typeof userMarkerRef.current.setLngLat === "function") {
        userMarkerRef.current.setLngLat(coords);
      }

      if (followUser) {
        map.easeToCenter(coords);
      }

      if (onMove) onMove(location);
    };

    if (typeof map.isLoaded === "function" && !map.isLoaded()) {
      if (typeof map.once === "function") {
        map.once("load", ensureMarkerAndUpdate);
      }
      return;
    }

    ensureMarkerAndUpdate();
  }, [map, location, followUser, onMove, userMarkerRef]);
}
