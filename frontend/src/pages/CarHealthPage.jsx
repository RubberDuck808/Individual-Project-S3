import React, { useMemo, useState } from "react";
import { useTheme } from "../context/ThemeContext";
import CarHealthPanel from "../components/car/CarHealthPanel";

export default function CarHealthPage() {
  const { darkMode } = useTheme();

  // Mock telemetry (toggleable)
  const healthyTelemetry = useMemo(
    () => ({
      connected: true,
      speedKmh: 94,
      rpm: 2300,
      coolantC: 90,
      batteryV: 13.8,
      oilTempC: 96,
      fuelPct: 61,
      errorCodes: [],
    }),
    []
  );

  const issueTelemetry = useMemo(
    () => ({
      connected: true,
      speedKmh: 88,
      rpm: 2700,
      coolantC: 104,
      batteryV: 12.1,
      oilTempC: 118,
      fuelPct: 22,
      errorCodes: [1, 3, 7],
    }),
    []
  );

  const [plate, setPlate] = useState("12-AB-34");
  const [telemetry, setTelemetry] = useState(issueTelemetry);

  return (
    <div
      className={[
        "min-h-screen w-full transition-colors duration-300",
        darkMode
          ? "bg-gray-900 text-white"
          : "bg-gradient-to-br from-gray-50 via-gray-100 to-gray-50 text-gray-900",
      ].join(" ")}
    >
      <div className="mx-auto max-w-7xl px-6 pt-28 pb-16">
        <div className="flex items-end justify-between gap-6 flex-wrap">
          <div>
            <h1 className="text-3xl md:text-4xl font-bold">Car Health</h1>
            <p className={darkMode ? "mt-2 text-gray-300" : "mt-2 text-gray-600"}>
              Placeholder demo: plate lookup + telemetry device data + translated error codes.
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <button
              onClick={() => setTelemetry(healthyTelemetry)}
              className={[
                "px-4 py-2 rounded-xl text-sm font-semibold transition",
                darkMode
                  ? "bg-white/10 hover:bg-white/15 border border-white/10"
                  : "bg-white/70 hover:bg-white border border-black/10",
              ].join(" ")}
            >
              Healthy demo
            </button>

            <button
              onClick={() => setTelemetry(issueTelemetry)}
              className="px-4 py-2 rounded-xl text-sm font-semibold bg-gradient-to-r from-blue-600 to-cyan-500 text-white hover:from-blue-500 hover:to-cyan-400 transition"
            >
              Issues demo
            </button>

            <button
              onClick={() =>
                setTelemetry((t) => ({
                  ...t,
                  connected: false,
                  errorCodes: [],
                }))
              }
              className={[
                "px-4 py-2 rounded-xl text-sm font-semibold transition",
                darkMode
                  ? "bg-white/10 hover:bg-white/15 border border-white/10"
                  : "bg-white/70 hover:bg-white border border-black/10",
              ].join(" ")}
            >
              Device offline
            </button>
          </div>
        </div>

        <div className="mt-10">
          <CarHealthPanel
            plate={plate}
            setPlate={setPlate}
            telemetry={telemetry}
            darkMode={darkMode}
          />
        </div>
      </div>
    </div>
  );
}
