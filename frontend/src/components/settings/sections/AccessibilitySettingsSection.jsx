import React from "react";
import PropTypes from "prop-types";
import SectionShell from "./SectionShell";

export default function AccessibilitySettingsSection({ darkMode, setDarkMode }) {
  return (
    <SectionShell>
      <h2 className="text-lg font-semibold mb-3">Accessibility</h2>

      <div className="flex items-center justify-between">
        <span>Dark Mode</span>
        <button
          onClick={() => setDarkMode(!darkMode)}
          className={`w-12 h-6 rounded-full relative transition ${
            darkMode ? "bg-blue-600" : "bg-gray-300"
          }`}
          type="button"
        >
          <div
            className={`absolute top-1 left-1 w-4 h-4 bg-white rounded-full shadow transition-transform ${
              darkMode ? "translate-x-6" : "translate-x-0"
            }`}
          />
        </button>
      </div>

      <p className="text-sm opacity-70 mt-3">
        More accessibility options will be added.
      </p>
    </SectionShell>
  );
}

AccessibilitySettingsSection.propTypes = {
  darkMode: PropTypes.bool.isRequired,
  setDarkMode: PropTypes.func.isRequired,
};
