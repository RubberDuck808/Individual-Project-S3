import React from "react";
import SearchBar from "./SearchBar";
import { useTheme } from "../context/ThemeContext";

export default function Navbar({ onSearchSelect, userLocation }) {
  const { darkMode } = useTheme();

  return (
    <nav
      className="w-full fixed top-0 left-0 z-50 px-4 py-3 pointer-events-none bg-transparent"
    >
      {/* Search bar visible, aligned to left */}
      <div className="pointer-events-auto w-fit">
        <SearchBar
          onSelect={onSearchSelect}
          userLocation={userLocation}
          darkMode={darkMode}
        />
      </div>
    </nav>
  );
}
