import { useEffect } from "react";
import { MapAdapter } from "./mapAdapter";

export function useMapboxInit(containerRef, darkMode, onReady) {
  useEffect(() => {
    if (!containerRef.current) return;

    const adapter = new MapAdapter(
      containerRef.current,
      darkMode
        ? "mapbox://styles/mapbox/navigation-night-v1"
        : "mapbox://styles/mapbox/streets-v11"
    );

    adapter.map.on("load", () => {
      onReady(adapter); 
    });

    return () => adapter.map.remove();
  }, [containerRef, darkMode]);
}
