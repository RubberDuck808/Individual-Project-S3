import React from "react";
import { useEffect, useState } from "react";
import { getAllHazards } from "../../api/hazardApi";
import { useTheme } from "../../context/ThemeContext";

export default function HazardListPanel({ onSelect, onClose }) {
  const [hazards, setHazards] = useState([]);
  const { darkMode } = useTheme();

  useEffect(() => {
    loadHazards();
  }, []);

  const loadHazards = async () => {
    try {
      const data = await getAllHazards();
      setHazards(data);
    } catch (err) {
      console.error(err);
      alert("Failed to load hazards");
    }
  };

  return (
    <div
      className={`fixed bottom-40 right-7 w-72 max-h-96 overflow-y-auto rounded-xl p-4 shadow-xl z-50 transition-colors duration-300 ${
        darkMode ? "bg-gray-800 text-white" : "bg-white text-gray-900"
      }`}
    >
      <div className="flex justify-between items-center mb-3">
        <h3 className="font-bold text-lg">Select a Hazard</h3>
        <button onClick={onClose} className="text-xl">✕</button>
      </div>

      {hazards.length === 0 ? (
        <p>No hazards found.</p>
      ) : (
        hazards.map((h) => (
          <div
            key={h.id}
            className={`p-3 rounded-lg mb-2 cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-700`}
            onClick={() => onSelect(h.id)}
          >
            <p className="font-semibold">{h.title || "Unnamed Hazard"}</p>
            <p className="text-sm opacity-75">{h.description}</p>
          </div>
        ))
      )}
    </div>
  );
}
