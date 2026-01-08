import React from "react";
import SectionShell from "./SectionShell";

export default function NotificationsSettingsSection({ enabled, setEnabled }) {
  return (
    <SectionShell>
      <h2 className="text-lg font-semibold mb-3">Notifications</h2>

      <div className="flex items-center justify-between">
        <span>Enable notifications</span>
        <button
          onClick={() => setEnabled(!enabled)}
          className={`w-12 h-6 rounded-full relative transition ${
            enabled ? "bg-green-500" : "bg-gray-300"
          }`}
          type="button"
        >
          <div
            className={`absolute top-1 left-1 w-4 h-4 bg-white rounded-full shadow transition-transform ${
              enabled ? "translate-x-6" : "translate-x-0"
            }`}
          />
        </button>
      </div>

      <p className="text-sm opacity-70 mt-3">
        This setting doesn't do anything yet.
      </p>
    </SectionShell>
  );
}
