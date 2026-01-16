import React from "react";
import PropTypes from "prop-types";
import CarIcon from "./CarIcon";

export default function CarSketch({ status = "ok" }) {
  let badge;
  if (status === "ok") {
    badge = "✅";
  } else if (status === "issues") {
    badge = "⚠️";
  } else {
    badge = "🔌";
  }

  let glow;
  if (status === "ok") {
    glow = "rgba(16,185,129,0.20)";
  } else if (status === "issues") {
    glow = "rgba(245,158,11,0.20)";
  } else {
    glow = "rgba(0,0,0,0.08)";
  }

  // Tailwind color for the SVG via currentColor
  let carColor;
  if (status === "ok") {
    carColor = "text-emerald-600";
  } else if (status === "issues") {
    carColor = "text-amber-600";
  } else {
    carColor = "text-gray-400";
  }

  return (
    <div className="relative w-full">
      <div className="absolute right-3 top-3 text-lg">{badge}</div>

      <div
        className="rounded-2xl p-4"
        style={{
          background: `radial-gradient(60% 80% at 50% 30%, ${glow} 0%, rgba(255,255,255,0) 70%)`,
        }}
      >
        <div className="w-full flex items-center justify-center">
          {/* control size here */}
          <CarIcon className={`w-full max-w-[520px] h-auto ${carColor} opacity-90`} />
        </div>

        <div className="mt-2 text-xs text-gray-600">
          {status === "ok" && "System status: normal. No active codes."}
          {status === "issues" && "System status: warnings detected. Review issues below."}
          {status === "disconnected" && "System status: device offline. Connect to read live health."}
        </div>
      </div>
    </div>
  );
}

CarSketch.propTypes = {
  status: PropTypes.oneOf(["ok", "issues", "disconnected"]),
};
