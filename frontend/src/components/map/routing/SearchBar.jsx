import React, { useEffect, useRef, useState } from "react";

function cx(...classes) {
  return classes.filter(Boolean).join(" ");
}

export default function SearchBar({
  onSelect,
  onCleared,
  userLocation,
  darkMode, // Theme logic kept for props, but styling follows the Neo-Brutalist theme
  clearSignal = 0,
  closeDropdownSignal = 0,
  suppressDropdown = false,
}) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const inputRef = useRef(null);
  const wrapperRef = useRef(null);

  useEffect(() => {
    setQuery("");
    setResults([]);
    setShowDropdown(false);
    inputRef.current?.blur();
  }, [clearSignal]);

  useEffect(() => {
    setShowDropdown(false);
    setResults([]);
    inputRef.current?.blur();
  }, [closeDropdownSignal]);

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

  // Mapbox autocomplete logic
  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      setShowDropdown(false);
      onCleared?.();
      return;
    }

    const timeout = setTimeout(async () => {
      try {
        const baseUrl = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(
          query
        )}.json?access_token=${
          import.meta.env.VITE_MAPBOX_TOKEN
        }&autocomplete=true&types=poi,address,place&limit=6`;

        const url =
          userLocation?.lng && userLocation?.lat
            ? `${baseUrl}&proximity=${userLocation.lng},${userLocation.lat}`
            : baseUrl;

        const res = await fetch(url);
        const data = await res.json();
        const feats = data.features || [];
        setResults(feats);

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
  }, [query, userLocation, suppressDropdown, onCleared]);

  const getLabel = (place) => {
    if (place.place_type?.includes("poi")) return place.text;
    return place.place_name;
  };

  const handleSelect = (place) => {
    onSelect(place);
    setQuery(getLabel(place));
    setShowDropdown(false);
    setResults([]);
    inputRef.current?.blur();
  };

  const clearInput = () => {
    setQuery("");
    setResults([]);
    setShowDropdown(false);
    onCleared?.();
    inputRef.current?.blur();
  };

  return (
    <div ref={wrapperRef} className="relative w-[320px] pointer-events-auto group">
      <div className="relative">
        {/* Main Search Input Container */}
        <div className="relative transform transition-transform group-focus-within:-translate-y-1">
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              if (!suppressDropdown) setShowDropdown(true);
            }}
            onFocus={() => {
              if (!suppressDropdown && results.length > 0 && query.trim()) {
                setShowDropdown(true);
              }
            }}
            placeholder="Search destination..."
            className={cx(
              "w-full pr-12 px-6 py-4 rounded-3xl text-sm font-bold border-[4px] border-black outline-none transition-all",
              "bg-[#FFFDF5] text-black placeholder-slate-400",
              "shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] focus:shadow-[8px_8px_0px_0px_#00D1FF]"
            )}
          />

          {/* Search/Clear Icon */}
          <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center">
            {query.trim() ? (
              <button
                type="button"
                onMouseDown={(e) => e.preventDefault()}
                onClick={clearInput}
                className="w-8 h-8 rounded-xl border-2 border-black bg-[#FF6AC1] text-white flex items-center justify-center font-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] active:shadow-none active:translate-x-0.5 active:translate-y-0.5"
              >
                ✕
              </button>
            ) : (
              <div className="w-8 h-8 rounded-xl border-2 border-black bg-[#FFD600] flex items-center justify-center shadow-[2px_2px_0px_0px_rgba(0,0,0,1)]">
                🔍
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Results Dropdown */}
      {!suppressDropdown && showDropdown && results.length > 0 && (
        <div
          className={cx(
            "absolute top-full mt-4 left-0 w-full rounded-[2rem] border-[4px] border-black overflow-hidden z-[110]",
            "bg-white shadow-[10px_10px_0px_0px_rgba(0,0,0,1)] animate-in slide-in-from-top-2 duration-200"
          )}
        >
          {results.map((place, idx) => (
            <div
              key={place.id}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => handleSelect(place)}
              className={cx(
                "px-6 py-4 cursor-pointer transition-colors border-black flex items-center gap-3",
                idx !== results.length - 1 && "border-b-[3px]",
                "hover:bg-[#00D1FF] group/item"
              )}
            >
              <span className="text-lg opacity-40 group-hover/item:opacity-100 transition-opacity">📍</span>
              <div className="flex flex-col min-w-0">
                <div className="font-[1000] text-xs uppercase tracking-tight text-black truncate">
                  {getLabel(place)}
                </div>
                <div className="text-[10px] font-black uppercase text-black/40 truncate">
                  {place.place_name?.split(",").slice(1).join(",").trim() || "Location"}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}