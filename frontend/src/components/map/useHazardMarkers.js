import { useEffect, useRef } from "react";

export function useHazardMarkers(map, hazards, onHazardClick, renderIcon) {
  const markersRef = useRef([]);

  useEffect(() => {
    // console.log("%c[useHazardMarkers] RUNNING EFFECT", "color: #0af");
    // console.log("map:", map);
    // console.log("hazards:", hazards);
    // console.log("hazard count:", hazards?.length ?? 0);

    if (!map) {
      console.warn("[useHazardMarkers] No map instance yet");
      return;
    }

    if (!map.isLoaded()) {
      console.warn("[useHazardMarkers] Map not loaded yet");
      return;
    }

    // REMOVE OLD MARKERS
    // console.log("[useHazardMarkers] Removing old markers:", markersRef.current.length);
    markersRef.current.forEach((m) => m?.remove());
    markersRef.current = [];

    // ADD NEW MARKERS
    hazards.forEach((haz, index) => {
      if (!haz.latitude || !haz.longitude) {
        // console.warn(`[useHazardMarkers] Hazard ${index} missing coords`, haz);
        return;
      }

      // console.log(`[useHazardMarkers] Adding marker ${index}`, {
      //   lng: haz.longitude,
      //   lat: haz.latitude
      // });

      const iconEl = renderIcon(haz);

      if (!iconEl) {
        // console.error(`[useHazardMarkers] renderIcon returned null for hazard ${index}`, haz);
        return;
      }

      iconEl.addEventListener("click", () => {
        // console.log(`[useHazardMarkers] Hazard clicked: ${haz.id}`);
        onHazardClick(haz);
      });

      const marker = map.addHTMLMarker(iconEl, [
        haz.longitude,
        haz.latitude,
      ]);

      if (!marker) {
        // console.error(`[useHazardMarkers] Marker failed to create for hazard ${index}`);
      }

      markersRef.current.push(marker);
    });

    // console.log("%c[useHazardMarkers] Markers added:", "color: lime", markersRef.current.length);

  }, [map, hazards, onHazardClick, renderIcon]);
}
