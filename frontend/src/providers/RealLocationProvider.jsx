import { useEffect, useState } from "react";
import LocationContext from "../context/LocationContext";
import React from "react";

export default function RealLocationProvider({ children }) {
  const [location, setLocation] = useState(null);

  useEffect(() => {
    if (!navigator.geolocation) return;

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        setLocation({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        });
      },
      (err) => console.warn("Real GPS error:", err),
      {
        enableHighAccuracy: false,
        timeout: 15000,
        maximumAge: 3000,
      }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, []);

  return (
    <LocationContext.Provider value={location}>
      {children}
    </LocationContext.Provider>
  );
}
