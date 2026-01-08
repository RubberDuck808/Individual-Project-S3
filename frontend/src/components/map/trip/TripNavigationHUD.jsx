import React, { useMemo } from "react";
import { haversineMeters } from "../../../utils/geo";
import { Check } from 'lucide-react';

function roundToNearest50(m) {
  if (m == null) return null;
  return Math.max(0, Math.round(m / 50) * 50);
}

function formatMeters(m) {
  if (m == null) return null;
  if (m < 30) return "NOW";
  if (m >= 1000) return `${(m / 1000).toFixed(1)} KM`;
  return `${m} M`;
}

function iconForManeuver(maneuver) {
  const type = maneuver?.type || "";
  const mod = maneuver?.modifier || "";

  if (type === "roundabout" || type === "rotary") return "⟳";
  if (type === "uturn" || mod === "uturn") return "↩︎";
  if (mod.includes("left")) return "←";
  if (mod.includes("right")) return "→";
  if (mod.includes("straight")) return "↑";
  if (type === "depart") return "⬆︎";
  if (type === "arrive") return "🏁";

  return "➜";
}

export default function TripNavigationHUD({
  steps,
  activeStepIndex,
  userLocation,
  submitting,
  onEnd,
  onCancel,
}) {
  if (!steps?.length) return null;

  const step = steps[activeStepIndex] ?? steps[0];
  const instruction = step?.maneuver?.instruction ?? "Continue";
  const icon = iconForManeuver(step?.maneuver);
  const maneuverLngLat = step?.maneuver?.location;

  const distanceToManeuver = useMemo(() => {
    if (userLocation?.lat == null || userLocation?.lng == null) return null;
    if (!Array.isArray(maneuverLngLat) || maneuverLngLat.length < 2) return null;

    const [mLng, mLat] = maneuverLngLat;

    const d = haversineMeters(
      userLocation.lat,
      userLocation.lng,
      mLat,
      mLng
    );

    return roundToNearest50(d);
  }, [userLocation?.lat, userLocation?.lng, maneuverLngLat]);

  const distText = formatMeters(distanceToManeuver);

  return (
    <div
      className="
        fixed right-4 top-16 z-50
        w-[88vw]
        sm:w-[460px]
        md:w-[520px]
        lg:w-[560px]
        max-w-[88vw]
      "
    >
      <div className="relative overflow-hidden rounded-2xl border border-black/10 bg-white/85 p-4 shadow-2xl backdrop-blur-xl dark:border-white/10 dark:bg-gray-900/70">
        {/* glow */}
        <div className="pointer-events-none absolute -top-16 -right-16 h-40 w-40 rounded-full bg-[#2F88FF]/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-16 -left-16 h-40 w-40 rounded-full bg-emerald-500/15 blur-3xl" />

        {/* Cancel trip */}
        <button
          onClick={onCancel}
          disabled={submitting}
          className="absolute right-2 top-2 rounded-full p-2 text-gray-500 transition hover:bg-black/5 hover:text-gray-900 disabled:opacity-60 dark:text-gray-300 dark:hover:bg-white/10 dark:hover:text-white"
          aria-label="Cancel trip"
          title="Cancel trip"
        >
          ✕
        </button>

        {/* Finish trip */}
        <button
          onClick={onEnd}
          disabled={submitting}
          className="absolute right-2 top-12 rounded-full p-2 text-gray-600 transition hover:bg-black/5 hover:text-gray-900 disabled:opacity-60 dark:text-gray-200 dark:hover:bg-white/10 dark:hover:text-white"
          aria-label="Finish trip"
          title="Finish trip"
        >
          <span className="text-lg"><Check /></span>
        </button>

        <div className="pr-14">
          <div className="flex items-start gap-3">
            {/* Maneuver icon */}
            <div className="mt-0.5 shrink-0 rounded-xl bg-blue-600 text-white w-12 h-12 flex items-center justify-center">
              <span className="text-2xl leading-none">{icon}</span>
            </div>

            <div className="min-w-0 flex-1">
              {/* BIG distance */}
              <div className="text-2xl sm:text-3xl font-extrabold tracking-tight">
                {distText ? `IN ${distText}` : " "}
              </div>

              {/* Main instruction */}
              <div className="mt-1 text-base sm:text-lg font-semibold leading-snug break-words">
                {instruction}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
