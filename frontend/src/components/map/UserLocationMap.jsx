import React from "react";
import { useRef, useState, forwardRef } from "react";
import { useTheme } from "../../context/ThemeContext";
import { useLocation } from "../../context/LocationContext";
import "mapbox-gl/dist/mapbox-gl.css";
import { useEffect } from "react";


import { useMapboxInit } from "./useMapboxInit";
import { useUserTracking } from "./useUserTracking";
import { useHazardMarkers } from "./useHazardMarkers";

const UserLocationMap = forwardRef((props, ref) => {
  const { followUser, hazards, onHazardClick, renderHazardIcon } = props;

  const { darkMode } = useTheme();
  const location = useLocation();

  const mapContainerRef = useRef(null);
  const [map, setMap] = useState(null);
  const userMarkerRef = useRef(null);



  // Detect manual user interaction and disable follow mode
  useEffect(() => {
  if (!map) return;

  const stopFollowing = () => {
    // ONLY stop following due to real user movement
    props.onFollowChange?.(false);
  };

  const cleanup = map.onUserInteraction(stopFollowing);
  return () => cleanup();
}, [map]);



  // Initialize map
  useMapboxInit(mapContainerRef, darkMode, (adapter) => {
    setMap(adapter);
  });

  // Track user position
  useUserTracking({
    map,
    location,
    userMarkerRef,
    followUser,
  });

  // Render hazard markers
  useHazardMarkers(map, hazards, onHazardClick, renderHazardIcon);

  return <div ref={mapContainerRef} className="w-full h-full" />;
});

export default UserLocationMap;
