import { useState, useRef } from "react";
import Navbar from "../components/Navbar";
import UserLocationMap from "../components/UserLocationMap";
import HazardFormPanel from "../components/HazardFormPanel";
import VotePanel from "../components/VotePanel";
import HazardListPanel from "../components/HazardListPanel";
import { useTheme } from "../context/ThemeContext";
import "mapbox-gl/dist/mapbox-gl.css";

export default function MapPage() {
  const mapRef = useRef(null);

  const [coords, setCoords] = useState(null);
  const [userLocation, setUserLocation] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);

  const [selectedHazardId, setSelectedHazardId] = useState(null);
  const [showHazardList, setShowHazardList] = useState(false);

  const { darkMode } = useTheme();

  const openHazardForm = () => {
    navigator.geolocation.getCurrentPosition(
      (pos) =>
        setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      () => alert("Location access denied, cannot create hazard")
    );
  };

  const handleFormClose = () => setCoords(null);

  const handleSearchSelect = (place) => {
    const [lng, lat] = place.center;
    mapRef.current?.goToAndRoute(lng, lat);
  };

  const openVoteList = () => {
    setShowHazardList(true);
  };

  return (
    <div
      className={`h-full w-full flex flex-col transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      <Navbar onSearchSelect={handleSearchSelect} userLocation={userLocation} />

      <div className="flex-1 relative">
        {/* MAP */}
        <UserLocationMap
          ref={mapRef}
          onUserLocation={setUserLocation}
          onRoutePreview={setRouteInfo}
          onHazardSelect={setSelectedHazardId}
        />

        {/* ROUTE PREVIEW */}
        {routeInfo && (
          <div
            className={`fixed right-4 top-24 rounded-xl p-4 w-72 z-50 shadow-lg transition-colors duration-300 ${
              darkMode ? "bg-gray-800 text-white" : "bg-white text-gray-900"
            }`}
          >
            <p className="font-bold text-lg">Route Preview</p>
            <p>Distance: {(routeInfo.distance / 1000).toFixed(1)} km</p>
            <p>Duration: {(routeInfo.duration / 60).toFixed(0)} min</p>

            <button
              className="w-full mt-3 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
            >
              Start Route
            </button>
          </div>
        )}

        {/* HAZARD LIST PANEL FOR VOTING */}
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
          onClick={openVoteList}
          className="fixed bottom-32 right-7 bg-blue-600 text-white px-9 py-3 rounded-full shadow-lg hover:bg-blue-700 transition z-50"
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


        {/* HAZARD FORM */}
        {coords && (
          <HazardFormPanel coords={coords} onClose={handleFormClose} />
        )}
      </div>
    </div>
  );
}
