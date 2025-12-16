import { useEffect } from "react";

export function useUserTracking({ map, location, userMarkerRef, followUser }) {
  useEffect(() => {
    if (!map) return;              
    if (!map.isLoaded()) return;   
    if (!location) return;

    console.log("useUserTracking map:", map);
    console.log("map loaded?", map?.isLoaded());
    console.log("location:", location);


    const coords = [location.lng, location.lat];

    // Create marker
    if (!userMarkerRef.current) {
      userMarkerRef.current = map.addColorMarker("red", coords);
    } else {
      userMarkerRef.current.setLngLat(coords);
    }

    if (followUser) {
      map.easeToCenter(coords);
    }
  }, [map, location, followUser]);
}
