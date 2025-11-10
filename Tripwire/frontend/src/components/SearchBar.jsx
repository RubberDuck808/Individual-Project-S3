import { useState, useEffect, useRef } from "react";

export default function SearchBar({ onSelect, userLocation, darkMode }) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      return;
    }

    const timeout = setTimeout(async () => {
      const baseUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(
        query
      )}.json?access_token=${import.meta.env.VITE_MAPBOX_TOKEN}&autocomplete=true&types=poi,address,place&limit=6`;

      const url =
        userLocation?.lng && userLocation?.lat
          ? `${baseUrl}&proximity=${userLocation.lng},${userLocation.lat}`
          : baseUrl;

      const res = await fetch(url);
      const data = await res.json();
      setResults(data.features || []);
      setShowDropdown(true);
    }, 300);

    return () => clearTimeout(timeout);
  }, [query, userLocation]);

  const handleSelect = (place) => {
    onSelect(place);
    setQuery(getLabel(place));
    setShowDropdown(false);
    inputRef.current?.blur();
  };

  const getLabel = (place) => {
    if (place.place_type.includes("poi")) return place.text;
    return place.place_name;
  };

  return (
    <div className="relative w-[320px] pointer-events-auto">
      <input
        ref={inputRef}
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Search location..."
        className={`w-full px-4 py-2.5 rounded-full text-sm shadow-lg outline-none border transition-all duration-300 ${
          darkMode
            ? "bg-gray-800 text-gray-100 placeholder-gray-400 border-gray-700 focus:ring-2 focus:ring-blue-500"
            : "bg-white/90 text-gray-900 placeholder-gray-500 border-gray-200 focus:ring-2 focus:ring-blue-500"
        }`}
      />

      {showDropdown && results.length > 0 && (
        <div
          className={`absolute top-full mt-2 left-0 w-full rounded-xl shadow-xl overflow-hidden z-50 border transition-colors duration-300 ${
            darkMode
              ? "bg-gray-800 border-gray-700"
              : "bg-white/95 border-gray-200"
          }`}
        >
          {results.map((place) => (
            <div
              key={place.id}
              onClick={() => handleSelect(place)}
              className={`px-4 py-2.5 cursor-pointer transition ${
                darkMode
                  ? "hover:bg-gray-700 text-gray-100"
                  : "hover:bg-gray-100 text-gray-900"
              }`}
            >
              <div className="font-medium text-sm">{getLabel(place)}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
