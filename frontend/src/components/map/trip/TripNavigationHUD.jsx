import React, { useMemo } from "react";
import PropTypes from "prop-types";
import { Check } from 'lucide-react';
import { haversineMeters } from "../../../utils/geo";

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
  // Ensure activeStepIndex is valid - calculate before early returns
  const safeIndex = Math.max(0, Math.min(activeStepIndex || 0, (steps?.length ?? 0) - 1));
  const step = steps?.[safeIndex];
  const maneuverLngLat = step?.maneuver?.location;

  // Calculate distance logic - must be called before early return
  const distanceToManeuver = useMemo(() => {
    if (!step) return null;
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
  }, [userLocation?.lat, userLocation?.lng, maneuverLngLat, step]);

  if (!steps?.length) return null;
  if (!step) return null;

  const instruction = step?.maneuver?.instruction ?? "Keep Moving";
  const icon = iconForManeuver(step?.maneuver);
  const distText = formatMeters(distanceToManeuver);

  return (
    <div className="fixed top-20 left-1/2 -translate-x-1/2 z-[100] w-[92vw] max-w-lg">
      <div className="bg-black border-[5px] border-black rounded-[2.5rem] shadow-[12px_12px_0px_0px_#FFD600] overflow-hidden">
        
        {/* Top Accent Bar */}
        <div className="h-2 w-full bg-[#00D1FF]" />

        <div className="p-6 bg-[#111] flex items-center gap-6">
          {/* Big Direction Icon */}
          <div className="shrink-0 w-20 h-20 bg-[#FFD600] border-4 border-black rounded-[2rem] flex items-center justify-center text-4xl shadow-[6px_6px_0px_0px_rgba(255,255,255,0.1)]">
            {icon}
          </div>

          <div className="flex-1 min-w-0">
            <div className="text-4xl font-[1000] text-[#00D1FF] italic tracking-tighter uppercase leading-none">
              {distText ? `IN ${distText}` : "NOW"}
            </div>
            <div className="mt-2 text-lg font-bold text-white leading-tight uppercase tracking-tight line-clamp-2">
              {instruction}
            </div>
          </div>

          {/* Side Controls */}
          <div className="flex flex-col gap-2">
            <button 
              onClick={onEnd}
              disabled={submitting}
              className="w-10 h-10 bg-emerald-400 border-[3px] border-black rounded-xl flex items-center justify-center shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:shadow-none active:translate-y-1 transition-all disabled:opacity-50"
            >
              <Check size={20} />
            </button>
            <button 
              onClick={onCancel}
              disabled={submitting}
              className="w-10 h-10 bg-white border-[3px] border-black rounded-xl flex items-center justify-center font-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] active:shadow-none active:translate-y-1 transition-all disabled:opacity-50"
            >
              ✕
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

TripNavigationHUD.propTypes = {
  steps: PropTypes.arrayOf(PropTypes.object),
  activeStepIndex: PropTypes.number.isRequired,
  userLocation: PropTypes.shape({
    lat: PropTypes.number,
    lng: PropTypes.number,
  }),
  submitting: PropTypes.bool.isRequired,
  onEnd: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};