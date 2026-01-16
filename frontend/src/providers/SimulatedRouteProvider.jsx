import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import LocationContext from "../context/LocationContext";
import { haversineMeters } from "../utils/geo";

// Calculate bearing (radians)
function calculateBearing(lat1, lng1, lat2, lng2) {
  const lat1Rad = (lat1 * Math.PI) / 180;
  const lat2Rad = (lat2 * Math.PI) / 180;
  const lng1Rad = (lng1 * Math.PI) / 180;
  const lng2Rad = (lng2 * Math.PI) / 180;

  const y = Math.sin(lng2Rad - lng1Rad) * Math.cos(lat2Rad);
  const x =
    Math.cos(lat1Rad) * Math.sin(lat2Rad) -
    Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(lng2Rad - lng1Rad);

  return Math.atan2(y, x);
}

export default function SimulatedRouteProvider({ children }) {
  const start = { lat: 51.829968, lng: 5.254388 };
  const end = { lat: 51.928691, lng: 5.164837 };

  const [location, setLocation] = useState(start);

  const speedKmh = 120; // speed in km/h
  const metersPerSecond = (speedKmh * 1000) / 3600;

  // Compute fixed bearing from start → end once
  const bearingRad = calculateBearing(start.lat, start.lng, end.lat, end.lng);

  useEffect(() => {
    const interval = setInterval(() => {
      setLocation((loc) => {
        const remaining = haversineMeters(
          loc.lat,
          loc.lng,
          end.lat,
          end.lng
        );

        // Stop when arrived
        if (remaining < 10) return end;

        // Convert movement to lat/lng offsets
        const deltaLat = (metersPerSecond * Math.cos(bearingRad)) / 111320;
        const deltaLng =
          (metersPerSecond * Math.sin(bearingRad)) /
          (111320 * Math.cos((loc.lat * Math.PI) / 180));

        return {
          lat: loc.lat + deltaLat,
          lng: loc.lng + deltaLng,
        };
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [bearingRad, end.lat, end.lng, metersPerSecond]);

  return (
    <LocationContext.Provider value={location}>
      {children}
    </LocationContext.Provider>
  );
}

// PropTypes validation
SimulatedRouteProvider.propTypes = {
  children: PropTypes.node.isRequired,
};
