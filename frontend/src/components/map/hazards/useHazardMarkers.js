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

export function useHazardMarkers(map, hazards, onHazardClick, renderIcon) {
  const markersRef = useRef([]);

  useEffect(() => {
    if (!map) return;

    if (typeof map.isLoaded === "function" && !map.isLoaded()) {
      if (typeof map.once === "function") {
        map.once("load", () => {
        });
      }
    }

    // Remove old markers
    markersRef.current.forEach((entry) => {
      try {
        entry?.cleanup?.();
      } catch (_) {}
      try {
        entry?.marker?.remove?.();
      } catch (_) {}
    });
    markersRef.current = [];

    if (!Array.isArray(hazards) || hazards.length === 0) return;
    if (typeof renderIcon !== "function") return;

    // Add new markers
    hazards.forEach((haz) => {
      if (haz?.latitude == null || haz?.longitude == null) return;

      const iconEl = renderIcon(haz);
      if (!iconEl) return;

      // Click handler
      const onClick = () => onHazardClick?.(haz);
      iconEl.addEventListener("click", onClick);

      // Create marker
      const marker = map.addHTMLMarker(iconEl, [haz.longitude, haz.latitude]);
      if (!marker) {
        iconEl.removeEventListener("click", onClick);
        return;
      }

      const applyScale = () => {
        if (typeof map.getZoom !== "function") return;
        const z = map.getZoom();
        const s = scaleForZoom(z);
        iconEl.style.transformOrigin = "center center";
        iconEl.style.transform = `scale(${s})`;
      };

      applyScale();

      const canListen = typeof map.on === "function" && typeof map.off === "function";
      if (canListen) {
        map.on("zoom", applyScale);
      }

      const cleanup = () => {
        iconEl.removeEventListener("click", onClick);
        if (canListen) {
          try {
            map.off("zoom", applyScale);
          } catch (_) {}
        }
      };

      markersRef.current.push({ marker, cleanup });
    });

    return () => {
      markersRef.current.forEach((entry) => {
        try {
          entry?.cleanup?.();
        } catch (_) {}
        try {
          entry?.marker?.remove?.();
        } catch (_) {}
      });
      markersRef.current = [];
    };
  }, [map, hazards, onHazardClick, renderIcon]);
}
