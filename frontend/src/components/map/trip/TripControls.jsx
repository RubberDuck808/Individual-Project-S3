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
        className={[
          "w-full mt-3 py-2.5 rounded-xl transition font-semibold",
          "focus:outline-none focus:ring-2 focus:ring-blue-500/60",
          canStart
            ? "bg-blue-600 text-white hover:bg-blue-700"
            : "bg-gray-300 text-gray-600 dark:bg-gray-700 dark:text-gray-300 cursor-not-allowed",
        ].join(" ")}
      >
        Start Trip
      </button>
    );
  }

  return (
    <div className="mt-3 space-y-3">
      <div className="flex items-center justify-between">
        <div className="text-sm opacity-80">Tracked distance</div>
        <div className="text-sm font-semibold px-3 py-1 rounded-full bg-black/5 dark:bg-white/10">
          {Number.isFinite(distanceKm) ? distanceKm.toFixed(2) : "0.00"} km
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2">
        <button
          disabled={submitting}
          onClick={onEnd}
          className={[
            "w-full py-2.5 rounded-xl transition font-semibold",
            "focus:outline-none focus:ring-2 focus:ring-red-500/60",
            submitting
              ? "bg-gray-300 text-gray-600 dark:bg-gray-700 dark:text-gray-300"
              : "bg-red-600 text-white hover:bg-red-700",
          ].join(" ")}
        >
          {submitting ? "Saving…" : "End"}
        </button>

        <button
          disabled={submitting}
          onClick={onCancel}
          className={[
            "w-full py-2.5 rounded-xl transition font-semibold",
            "focus:outline-none focus:ring-2 focus:ring-gray-500/40",
            "border border-black/10 dark:border-white/10",
            submitting
              ? "opacity-60 cursor-not-allowed"
              : "hover:bg-black/5 dark:hover:bg-white/10",
          ].join(" ")}
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
