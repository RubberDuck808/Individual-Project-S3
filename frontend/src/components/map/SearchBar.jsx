import React, { useEffect, useRef, useState } from "react";

export default function SearchBar({
  onSelect,
  userLocation,
  darkMode,
  closeDropdownSignal = 0,     // 🔹 bump this number to force-close
  suppressDropdown = false,     // 🔹 true when route/trip is active
}) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const inputRef = useRef(null);
  const wrapperRef = useRef(null);

  // ✅ Close dropdown + clear results when parent signals it (start route/trip, etc.)
  useEffect(() => {
    setShowDropdown(false);
    setResults([]);
    // optionally blur
    inputRef.current?.blur();
  }, [closeDropdownSignal]);

  // ✅ Click outside closes dropdown
  useEffect(() => {
    function onDocMouseDown(e) {
      if (!wrapperRef.current) return;
      if (!wrapperRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    }
    document.addEventListener("mousedown", onDocMouseDown);
    return () => document.removeEventListener("mousedown", onDocMouseDown);
  }, []);

  // Mapbox autocomplete
  useEffect(() => {
    // If dropdown is suppressed (route/trip active), still allow typing but do not show suggestions
    if (!query.trim()) {
      setResults([]);
      setShowDropdown(false);
      return;
    }

    const timeout = setTimeout(async () => {
      try {
        const baseUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(
          query
        )}.json?access_token=${import.meta.env.VITE_MAPBOX_TOKEN}&autocomplete=true&types=poi,address,place&limit=6`;

        const url =
          userLocation?.lng && userLocation?.lat
            ? `${baseUrl}&proximity=${userLocation.lng},${userLocation.lat}`
            : baseUrl;

        const res = await fetch(url);
        const data = await res.json();

        const feats = data.features || [];
        setResults(feats);

        // Only show dropdown if not suppressed
        if (!suppressDropdown && feats.length > 0) {
          setShowDropdown(true);
        } else {
          setShowDropdown(false);
        }
      } catch {
        setResults([]);
        setShowDropdown(false);
      }
    }, 300);

    return () => clearTimeout(timeout);
  }, [query, userLocation, suppressDropdown]);

  const getLabel = (place) => {
    if (place.place_type?.includes("poi")) return place.text;
    return place.place_name;
  };

  const handleSelect = (place) => {
    onSelect(place);
    setQuery(getLabel(place));
    setShowDropdown(false);
    setResults([]); // ✅ prevents re-open with old results
    inputRef.current?.blur();
  };

  return (
    <div ref={wrapperRef} className="relative w-[320px] pointer-events-auto">
      <input
        ref={inputRef}
        type="text"
        value={query}
        onChange={(e) => {
          setQuery(e.target.value);
          // If user starts typing again, allow dropdown unless suppressed
          if (!suppressDropdown) setShowDropdown(true);
        }}
        onFocus={() => {
          if (!suppressDropdown && results.length > 0 && query.trim()) {
            setShowDropdown(true);
          }
        }}
        onBlur={() => {
          // Delay allows clicking a result before dropdown disappears
          setTimeout(() => setShowDropdown(false), 120);
        }}
        placeholder="Search location..."
        className={`w-full px-4 py-2.5 rounded-full text-sm shadow-lg outline-none border transition-all duration-300 ${
          darkMode
            ? "bg-gray-800 text-gray-100 placeholder-gray-400 border-gray-700 focus:ring-2 focus:ring-blue-500"
            : "bg-white/90 text-gray-900 placeholder-gray-500 border-gray-200 focus:ring-2 focus:ring-blue-500"
        }`}
      />

      {!suppressDropdown && showDropdown && results.length > 0 && (
        <div
          className={`absolute top-full mt-2 left-0 w-full rounded-xl shadow-xl overflow-hidden z-50 border transition-colors duration-300 ${
            darkMode ? "bg-gray-800 border-gray-700" : "bg-white/95 border-gray-200"
          }`}
        >
          {results.map((place) => (
            <div
              key={place.id}
              onMouseDown={(e) => e.preventDefault()} // ✅ prevents blur-before-click
              onClick={() => handleSelect(place)}
              className={`px-4 py-2.5 cursor-pointer transition ${
                darkMode ? "hover:bg-gray-700 text-gray-100" : "hover:bg-gray-100 text-gray-900"
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
