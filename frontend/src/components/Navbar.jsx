import React from "react";
import SearchBar from "./map/SearchBar";
import { useTheme } from "../context/ThemeContext";

export default function Navbar({
  onSearchSelect,
  userLocation,
  closeSearchSignal = 0,
  suppressSearchDropdown = false,
}) {
  const { darkMode } = useTheme();

  return (
    <nav className="w-full fixed top-0 left-0 z-50 px-4 py-3 pointer-events-none bg-transparent">
      <div className="pointer-events-auto w-fit">
        <SearchBar
          onSelect={onSearchSelect}
          userLocation={userLocation}
          darkMode={darkMode}
          closeDropdownSignal={closeSearchSignal}
          suppressDropdown={suppressSearchDropdown}
        />
      </div>
    </nav>
  );
}
