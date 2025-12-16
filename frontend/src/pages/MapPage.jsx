import React from "react";
import { useState, useRef, useEffect } from "react";
import Navbar from "../components/Navbar";
import UserLocationMap from "../components/map/UserLocationMap";
import HazardFormPanel from "../components/HazardFormPanel";
import VotePanel from "../components/VotePanel";
import HazardListPanel from "../components/HazardListPanel";
import { useTheme } from "../context/ThemeContext";
import { useLocation } from "../context/LocationContext";
import { getCategoriesCached, getAllHazards } from "../api/hazardApi";
import "mapbox-gl/dist/mapbox-gl.css";
import { iconMap } from "../utils/iconMap";



export default function MapPage() {
  const mapRef = useRef(null);
  const location = useLocation();

  const [hazards, setHazards] = useState([]); 
  const [reportOpen, setReportOpen] = useState(false);
  const [routeInfo, setRouteInfo] = useState(null);
  const [selectedHazardId, setSelectedHazardId] = useState(null);
  const [showHazardList, setShowHazardList] = useState(false);
  const [isFollowing, setIsFollowing] = useState(true);

  const { darkMode } = useTheme();

  // Load categories & hazards
  useEffect(() => {
    getCategoriesCached().catch(console.error);

    getAllHazards()
      .then((data) => setHazards(data))
      .catch(console.error);
  }, []);

  const openHazardForm = () => {
    if (!location) {
      alert("Still determining your location…");
      return;
    }
    setReportOpen(true);
  };

  const handleSearchSelect = (place) => {
    const [lng, lat] = place.center;
    setIsFollowing(false);
    mapRef.current?.goToAndRoute(lng, lat);
  };

  return (
    <div
      className={`h-full w-full flex flex-col transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      <Navbar onSearchSelect={handleSearchSelect} userLocation={location} />

      <div className="flex-1 relative">

        {/* MAP */}
        <UserLocationMap
          ref={mapRef}
          hazards={hazards}                               
          renderHazardIcon={(hazard) => {
            const el = document.createElement("div");
            el.className = "w-10 h-10 flex items-center justify-center cursor-pointer";

            const iconFilename = hazard.category?.icon;
            const resolvedIcon = iconMap[iconFilename];

            if (resolvedIcon) {
              const img = document.createElement("img");
              img.src = resolvedIcon;
              img.className = "w-full h-full object-contain";
              el.appendChild(img);
            } else {
              // Fallback emoji if icon is missing
              el.innerHTML = "⚠️";
              el.style.fontSize = "26px";
            }

            return el;
          }}


          onHazardClick={(haz) => {
            setSelectedHazardId(haz.id);
            setIsFollowing(false);
          }}
          followUser={isFollowing}
          onFollowChange={setIsFollowing}
          onRoutePreview={setRouteInfo}
        />

        {/* RECENTER BUTTON */}
        {!isFollowing && (
          <button
            onClick={() => {
              setIsFollowing(true);
              mapRef.current?.recenterToUser();
            }}
            className="fixed bottom-36 left-1/2 -translate-x-1/2 bg-blue-600 text-white px-6 py-2 rounded-full shadow-lg hover:bg-blue-700 transition z-40"
          >
            Recenter
          </button>
        )}

        {/* ROUTE PREVIEW */}
        {routeInfo && (
          <div
            className={`fixed right-4 top-24 rounded-xl p-4 w-72 z-40 shadow-lg transition-colors duration-300 ${
              darkMode ? "bg-gray-800 text-white" : "bg-white text-gray-900"
            }`}
          >
            <p className="font-bold text-lg">Route Preview</p>
            <p>Distance: {(routeInfo.distance / 1000).toFixed(1)} km</p>
            <p>Duration: {(routeInfo.duration / 60).toFixed(0)} min</p>

            <button className="w-full mt-3 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
              Start Route
            </button>
          </div>
        )}

        {/* HAZARD LIST PANEL */}
        {showHazardList && (
          <HazardListPanel
            onSelect={(id) => {
              setSelectedHazardId(id);
              setShowHazardList(false);
            }}
            onClose={() => setShowHazardList(false)}
          />
        )}

        {/* VOTE PANEL */}
        {selectedHazardId && (
          <VotePanel
            hazardId={selectedHazardId}
            onClose={() => setSelectedHazardId(null)}
          />
        )}

        {/* VOTE BUTTON */}
        <button
          onClick={() => setShowHazardList(true)}
          className="fixed bottom-32 right-7 bg-blue-600 text-white px-9 py-3 rounded-full shadow-lg hover:bg-blue-700 transition z-40"
        >
          Vote
        </button>

        {/* REPORT BUTTON */}
        <button
          onClick={openHazardForm}
          className="fixed bottom-20 right-7 bg-red-600 text-white px-9 py-3 rounded-full shadow-lg hover:bg-red-700 transition z-40"
        >
          Report
        </button>

        {/* REPORT PANEL */}
        {reportOpen && (
          <HazardFormPanel
            coords={location}
            onClose={() => setReportOpen(false)}
          />
        )}

      </div>
    </div>
  );
}
