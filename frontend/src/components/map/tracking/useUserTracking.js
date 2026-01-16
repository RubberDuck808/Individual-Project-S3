import { useEffect, useRef } from "react";

export function useUserTracking({ map, location, userMarkerRef, followUser, onMove }) {
  const elRef = useRef(null);

  useEffect(() => {
    if (!map || !location) return;

    const coords = [location.lng, location.lat];
    let cleanupLoadListener = null;

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
        // Zoom to street level (15) when following user
        if (typeof map.easeTo === "function") {
          map.easeTo({
            center: coords,
            zoom: 15, // Street level zoom
            duration: 600,
            essential: true,
          });
        } else {
          map.easeToCenter(coords);
          if (typeof map.setZoom === "function") {
            map.setZoom(15);
          }
        }
      }

      if (onMove) onMove(location);
    };

    // Check if map is loaded
    if (typeof map.isLoaded === "function" && !map.isLoaded()) {
      // Map not loaded yet, wait for load event
      if (typeof map.once === "function") {
        cleanupLoadListener = map.once("load", ensureMarkerAndUpdate);
      }
      return () => {
        if (cleanupLoadListener && typeof cleanupLoadListener === "function") {
          cleanupLoadListener();
        }
      };
    }

    // Map is already loaded, update immediately
    ensureMarkerAndUpdate();
  }, [map, location, followUser, onMove, userMarkerRef]);
}
