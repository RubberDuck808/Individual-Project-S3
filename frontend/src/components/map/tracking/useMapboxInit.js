import { useEffect, useRef } from "react";
import { MapAdapter } from "./mapAdapter";

export function useMapboxInit(containerRef, darkMode, onReady) {
  const onReadyRef = useRef(onReady);
  
  // Keep ref updated
  useEffect(() => {
    onReadyRef.current = onReady;
  }, [onReady]);

  useEffect(() => {
    if (!containerRef.current) return;

    const adapter = new MapAdapter(
      containerRef.current,
      darkMode
        ? "mapbox://styles/mapbox/navigation-night-v1"
        : "mapbox://styles/mapbox/streets-v11"
    );

    let isMounted = true;

    adapter.map.on("load", () => {
      if (isMounted && onReadyRef.current) {
        onReadyRef.current(adapter);
      }
    });

    return () => {
      isMounted = false;
      adapter.map.remove();
    };
  }, [containerRef, darkMode]);
}
