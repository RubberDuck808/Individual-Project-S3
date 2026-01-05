import React from "react";

export default function TripControls({
  isActive,
  distanceKm,
  submitting,
  onStart,
  onEnd,
  onCancel,
  canStart,
}) {
  if (!isActive) {
    return (
      <button
        disabled={!canStart}
        onClick={onStart}
        className={`w-full mt-3 py-2 rounded-lg transition ${
          canStart
            ? "bg-blue-600 text-white hover:bg-blue-700"
            : "bg-gray-400 text-white cursor-not-allowed"
        }`}
      >
        Start Trip
      </button>
    );
  }

  return (
    <div className="mt-3 space-y-2">
      <div className="text-sm opacity-80">
        Tracked distance: <span className="font-bold">{distanceKm.toFixed(2)} km</span>
      </div>

      <button
        disabled={submitting}
        onClick={onEnd}
        className={`w-full py-2 rounded-lg transition ${
          submitting ? "bg-gray-400 text-white" : "bg-red-600 text-white hover:bg-red-700"
        }`}
      >
        {submitting ? "Saving…" : "End Trip"}
      </button>

      <button
        disabled={submitting}
        onClick={onCancel}
        className="w-full py-2 rounded-lg border border-gray-300 dark:border-gray-700"
      >
        Cancel
      </button>
    </div>
  );
}
