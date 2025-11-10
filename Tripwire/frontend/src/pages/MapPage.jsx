import { useState, useRef } from "react";
import Navbar from "../components/Navbar";
import UserLocationMap from "../components/UserLocationMap";
import HazardFormPanel from "../components/HazardFormPanel";
import { useTheme } from "../context/ThemeContext";
import "mapbox-gl/dist/mapbox-gl.css";

export default function MapPage() {
  const mapRef = useRef(null);
  const [coords, setCoords] = useState(null);
  const [userLocation, setUserLocation] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
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

  return (
    <div
      className={`h-full w-full flex flex-col transition-colors duration-300 ${
        darkMode ? "bg-gray-900 text-white" : "bg-gray-100 text-gray-900"
      }`}
    >
      {/* Map-specific navbar */}
      <Navbar onSearchSelect={handleSearchSelect} userLocation={userLocation} />

      <div className="flex-1 relative">
        <UserLocationMap
          ref={mapRef}
          onUserLocation={setUserLocation}
          onRoutePreview={setRouteInfo}
        />

        {/* Route preview panel */}
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
              onClick={() => console.log("Start navigation")}
            >
              Start Route
            </button>
          </div>
        )}

        {/* Floating "Report" button */}
        <button
          onClick={openHazardForm}
          className="fixed bottom-20 right-7 bg-red-600 text-white px-9 py-3 rounded-full shadow-lg hover:bg-red-700 transition"
        >
          Report
        </button>

        {/* Bottom hazard form sheet */}
        {coords && <HazardFormPanel coords={coords} onClose={handleFormClose} />}
      </div>
    </div>
  );
}
