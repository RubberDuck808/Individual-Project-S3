import { useEffect, useRef } from "react";

const ZOOM_ANCHOR = 14;
const SCALE_PER_ZOOM = 0.06;
const MIN_SCALE = 0.85;
const MAX_SCALE = 1.05;

function clamp(n, min, max) {
  return Math.max(min, Math.min(max, n));
}

function scaleForZoom(z) {
  const s = 1 + (z - ZOOM_ANCHOR) * SCALE_PER_ZOOM;
  return clamp(s, MIN_SCALE, MAX_SCALE);
}

function createMarkerEntry(haz, iconEl, map, onHazardClick, isMounted) {
  // Click handler
  const onClick = () => onHazardClick?.(haz);
  iconEl.addEventListener("click", onClick);

  // Create marker
  const marker = map.addHTMLMarker(iconEl, [haz.longitude, haz.latitude]);
  if (!marker) {
    iconEl.removeEventListener("click", onClick);
    return null;
  }

  const applyScale = createScaleHandler(iconEl, map, isMounted);
  applyScale();

  const canListen = typeof map.on === "function" && typeof map.off === "function";
  if (canListen) {
    map.on("zoom", applyScale);
  }

  const cleanup = createCleanupHandler(iconEl, onClick, map, applyScale, canListen);

  return { marker, cleanup };
}

function createScaleHandler(iconEl, map, isMounted) {
  return () => {
    if (!isMounted) return;
    if (typeof map.getZoom !== "function") return;
    if (typeof map.isLoaded === "function" && !map.isLoaded()) return;
    
    try {
      const z = map.getZoom();
      const s = scaleForZoom(z);
      iconEl.style.transformOrigin = "center center";
      iconEl.style.transform = `scale(${s})`;
    } catch (e) {
      // Map may have been removed, ignore
      console.debug("Failed to apply scale:", e);
    }
  };
}

function createCleanupHandler(iconEl, onClick, map, applyScale, canListen) {
  return () => {
    iconEl.removeEventListener("click", onClick);
    if (canListen) {
      try {
        map.off("zoom", applyScale);
      } catch (error) {
        // Map may have been removed, ignore
        console.debug("Failed to remove zoom listener:", error);
      }
    }
  };
}

export function useHazardMarkers(map, hazards, onHazardClick, renderIcon) {
  const markersRef = useRef([]);

  useEffect(() => {
    if (!map) return;

    let loadListenerCleanup = null;
    let isMounted = true;

    const setupMarkers = () => {
      if (!isMounted) return;

      // Remove old markers
    for (const entry of markersRef.current) {
      try {
        entry?.cleanup?.();
      } catch (error) {
        console.debug("Failed to cleanup marker entry:", error);
      }
      try {
        entry?.marker?.remove?.();
      } catch (error) {
        console.debug("Failed to remove marker:", error);
      }
    }
    markersRef.current = [];

    if (!Array.isArray(hazards) || hazards.length === 0) return;
    if (typeof renderIcon !== "function") return;

    // Add new markers
    for (const haz of hazards) {
      if (haz?.latitude == null || haz?.longitude == null) continue;

      const iconEl = renderIcon(haz);
      if (!iconEl) continue;

      const markerEntry = createMarkerEntry(haz, iconEl, map, onHazardClick, isMounted);
      if (markerEntry) {
        markersRef.current.push(markerEntry);
      }
    }
    };

    if (typeof map.isLoaded === "function" && !map.isLoaded()) {
      if (typeof map.once === "function") {
        loadListenerCleanup = map.once("load", setupMarkers);
      }
    } else {
      setupMarkers();
    }

    return () => {
      isMounted = false;
      
      if (loadListenerCleanup && typeof loadListenerCleanup === "function") {
        loadListenerCleanup();
      }

      for (const entry of markersRef.current) {
        try {
          entry?.cleanup?.();
        } catch (error) {
          // Marker cleanup may fail if already removed, ignore
          console.debug("Failed to cleanup marker:", error);
        }
        try {
          entry?.marker?.remove?.();
        } catch (error) {
          // Marker removal may fail if already removed, ignore
          console.debug("Failed to remove marker:", error);
        }
      }
      markersRef.current = [];
    };
  }, [map, hazards, onHazardClick, renderIcon]);
}
