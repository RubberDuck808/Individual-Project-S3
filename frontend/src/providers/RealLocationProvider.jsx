import React, { useEffect, useState } from "react";
import { useLocation as useRouterLocation } from "react-router-dom";
import LocationContext from "../context/LocationContext";
import PropTypes from "prop-types";

/**
 * RealLocationProvider - Provides real GPS location data.
 * 
 * IMPORTANT: Geolocation is only enabled when the user is on pages that require it
 * (e.g., /map). This improves privacy and performance by not requesting location
 * on pages like login, signup, or admin pages where it's not needed.
 */
export default function RealLocationProvider({ children }) {
  const [gpsLocation, setGpsLocation] = useState(null);
  const routerLocation = useRouterLocation();

  useEffect(() => {
    // Only enable geolocation on pages that actually need it
    // Currently, only the map page requires real-time location tracking
    const needsLocation = routerLocation.pathname === "/map";
    
    if (!navigator.geolocation || !needsLocation) {
      // Clear location when not needed
      if (!needsLocation) {
        setGpsLocation(null);
      }
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (pos) => {
        setGpsLocation({
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
  }, [routerLocation.pathname]);

  return (
    <LocationContext.Provider value={gpsLocation}>
      {children}
    </LocationContext.Provider>
  );
}

RealLocationProvider.propTypes = {
  children: PropTypes.node.isRequired,
};
