import React, { useMemo, useState } from "react";
import CarSketch from "./CarSketch";

// Placeholder “RDW-like” database
const MOCK_PLATE_DB = {
  "12-AB-34": {
    make: "Volkswagen",
    model: "Golf",
    year: 2019,
    fuelType: "Petrol",
    maxSpeedKmh: 210,
    massKg: 1280,
  },
  "99-ZZ-99": {
    make: "Toyota",
    model: "Yaris",
    year: 2016,
    fuelType: "Hybrid",
    maxSpeedKmh: 165,
    massKg: 1090,
  },
  "EV-2025": {
    make: "Tesla",
    model: "Model 3",
    year: 2024,
    fuelType: "Electric",
    maxSpeedKmh: 233,
    massKg: 1765,
  },
};

// Placeholder error mapping
const ERROR_MAP = {
  1: { title: "Low oil pressure", detail: "Oil pressure below threshold.", severity: "high" },
  2: { title: "Engine temperature high", detail: "Coolant temp elevated.", severity: "high" },
  3: { title: "Battery voltage low", detail: "Charging system underperforming.", severity: "medium" },
  7: { title: "Misfire detected", detail: "Misfire detected in one cylinder.", severity: "medium" },
};

function translateErrors(errorCodes = []) {
  return errorCodes.map((code) => ({
    code,
    ...(ERROR_MAP[code] || {
      title: "Unknown issue",
      detail: "This error code is not mapped yet.",
      severity: "low",
    }),
  }));
}

export default function CarHealthPanel({ linkedPlate, setLinkedPlate, telemetry, darkMode }) {
  const [isEditingPlate, setIsEditingPlate] = useState(false);
  const [draftPlate, setDraftPlate] = useState(linkedPlate);
  const [plateMsg, setPlateMsg] = useState("");

  const vehicle = useMemo(() => {
    const key = (linkedPlate || "").trim().toUpperCase();
    return MOCK_PLATE_DB[key] || null;
  }, [linkedPlate]);

  const issues = useMemo(() => translateErrors(telemetry?.errorCodes ?? []), [telemetry]);

  const status = useMemo(() => {
    if (!telemetry?.connected) return "disconnected";
    if ((telemetry?.errorCodes?.length ?? 0) > 0) return "issues";
    return "ok";
  }, [telemetry]);

  const cardBase = darkMode
    ? "bg-white/5 border border-white/10 text-white"
    : "bg-white/75 border border-black/10 text-gray-900";
  const subtleText = darkMode ? "text-gray-300" : "text-gray-600";

  function startEdit() {
    setPlateMsg("");
    setDraftPlate(linkedPlate);
    setIsEditingPlate(true);
  }

  function cancelEdit() {
    setPlateMsg("");
    setDraftPlate(linkedPlate);
    setIsEditingPlate(false);
  }

  function saveEdit() {
    const normalized = (draftPlate || "").trim().toUpperCase();
    if (!normalized) {
      setPlateMsg("Please enter a plate.");
      return;
    }
    // Placeholder rule: allow only plates that exist in mock DB for now
    if (!MOCK_PLATE_DB[normalized]) {
      setPlateMsg("Unknown plate (placeholder). Try 12-AB-34, 99-ZZ-99, EV-2025.");
      return;
    }
    setLinkedPlate(normalized);
    setIsEditingPlate(false);
    setPlateMsg("Linked car updated.");
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* LEFT */}
      <div className="lg:col-span-5">
        <div className={`rounded-3xl p-6 backdrop-blur-xl ${cardBase}`}>
          <div className="flex items-center justify-between gap-4">
            <div>
              <div className={`text-sm ${subtleText}`}>Status</div>
              <div className="text-2xl font-bold mt-1">
                {status === "ok" && "All Good"}
                {status === "issues" && "Attention Needed"}
                {status === "disconnected" && "Device Offline"}
              </div>
            </div>

            <div
              className={[
                "px-3 py-1.5 rounded-full text-xs font-semibold",
                status === "ok" &&
                  (darkMode
                    ? "bg-emerald-500/20 text-emerald-200"
                    : "bg-emerald-100 text-emerald-800"),
                status === "issues" &&
                  (darkMode
                    ? "bg-amber-500/20 text-amber-200"
                    : "bg-amber-100 text-amber-800"),
                status === "disconnected" &&
                  (darkMode
                    ? "bg-gray-500/20 text-gray-200"
                    : "bg-gray-200 text-gray-700"),
              ]
                .filter(Boolean)
                .join(" ")}
            >
              {status}
            </div>
          </div>

          <div className="mt-6 rounded-2xl border border-black/10 dark:border-white/10 bg-white/60 dark:bg-white/5 p-4">
            <CarSketch status={status} />
          </div>

          <div className="mt-6">
            <div className="text-sm font-semibold">Translated issues</div>

            {status === "ok" ? (
              <div className={`mt-2 text-sm ${subtleText}`}>
                No active error codes. Everything looks normal.
              </div>
            ) : status === "disconnected" ? (
              <div className={`mt-2 text-sm ${subtleText}`}>
                Connect the telemetry device to read live health + codes.
              </div>
            ) : (
              <ul className="mt-3 space-y-2">
                {issues.map((it) => (
                  <li
                    key={it.code}
                    className={[
                      "rounded-xl px-4 py-3 text-sm border",
                      darkMode ? "bg-white/5 border-white/10" : "bg-white border-black/10",
                    ].join(" ")}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="font-semibold">{it.title}</div>
                      <div className={darkMode ? "text-white/60" : "text-gray-500"}>#{it.code}</div>
                    </div>
                    <div className={`mt-1 ${subtleText}`}>{it.detail}</div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>

      {/* RIGHT */}
      <div className="lg:col-span-7">
        <div className={`rounded-3xl p-6 backdrop-blur-xl ${cardBase}`}>
          <div className="flex items-start justify-between gap-4 flex-wrap">
            <div>
              <div className={`text-sm ${subtleText}`}>Linked vehicle</div>
              <div className="text-xl font-bold mt-1">
                Plate: <span className={darkMode ? "text-white" : "text-gray-900"}>{linkedPlate}</span>
              </div>
              <div className={`mt-1 text-sm ${subtleText}`}>
                One car linked for now. Change only if the user switches cars.
              </div>
            </div>

            {!isEditingPlate ? (
              <button
                onClick={startEdit}
                className={[
                  "px-4 py-2 rounded-xl text-sm font-semibold transition border",
                  darkMode
                    ? "bg-white/10 hover:bg-white/15 border-white/10"
                    : "bg-white/70 hover:bg-white border-black/10",
                ].join(" ")}
              >
                Change car
              </button>
            ) : (
              <div className="flex gap-2">
                <button
                  onClick={cancelEdit}
                  className={[
                    "px-4 py-2 rounded-xl text-sm font-semibold transition border",
                    darkMode
                      ? "bg-white/10 hover:bg-white/15 border-white/10"
                      : "bg-white/70 hover:bg-white border-black/10",
                  ].join(" ")}
                >
                  Cancel
                </button>
                <button
                  onClick={saveEdit}
                  className="px-4 py-2 rounded-xl text-sm font-semibold bg-gradient-to-r from-blue-600 to-cyan-500 text-white hover:from-blue-500 hover:to-cyan-400 transition"
                >
                  Save
                </button>
              </div>
            )}
          </div>

          {isEditingPlate && (
            <div className="mt-4">
              <label className={`text-xs ${subtleText}`}>New plate</label>
              <div className="mt-2 flex gap-2">
                <input
                  value={draftPlate}
                  onChange={(e) => setDraftPlate(e.target.value)}
                  className={[
                    "px-4 py-2 rounded-xl text-sm outline-none w-56 border",
                    darkMode
                      ? "bg-gray-900/40 border-white/10 placeholder:text-white/30"
                      : "bg-white border-black/10 placeholder:text-gray-400",
                  ].join(" ")}
                  placeholder="12-AB-34"
                />
                <div className={`text-xs ${subtleText} flex items-center`}>
                  Try: 12-AB-34 / 99-ZZ-99 / EV-2025
                </div>
              </div>

              {plateMsg && (
                <div className={`mt-2 text-sm ${plateMsg.includes("updated") ? "text-emerald-400" : "text-amber-400"}`}>
                  {plateMsg}
                </div>
              )}
            </div>
          )}

          {/* vehicle info */}
          <div className="mt-6">
            <div className="text-lg font-bold">Vehicle details (placeholder)</div>
            <div className="mt-4">
              {vehicle ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <InfoTile darkMode={darkMode} label="Make" value={vehicle.make} />
                  <InfoTile darkMode={darkMode} label="Model" value={vehicle.model} />
                  <InfoTile darkMode={darkMode} label="Year" value={vehicle.year} />
                  <InfoTile darkMode={darkMode} label="Fuel type" value={vehicle.fuelType} />
                  <InfoTile darkMode={darkMode} label="Max speed" value={`${vehicle.maxSpeedKmh} km/h`} />
                  <InfoTile darkMode={darkMode} label="Mass" value={`${vehicle.massKg} kg`} />
                </div>
              ) : (
                <div className={subtleText}>No vehicle found for this plate (placeholder).</div>
              )}
            </div>
          </div>

          {/* telemetry */}
          <div className="mt-10">
            <div className="flex items-center justify-between">
              <div className="text-lg font-bold">Telemetry (placeholder)</div>
              <div className={`text-sm ${subtleText}`}>{telemetry?.connected ? "Device connected" : "No device"}</div>
            </div>

            <div className="mt-4 grid grid-cols-2 md:grid-cols-3 gap-4">
              <Metric darkMode={darkMode} label="Speed" value={telemetry?.speedKmh ?? "—"} unit="km/h" />
              <Metric darkMode={darkMode} label="RPM" value={telemetry?.rpm ?? "—"} unit="" />
              <Metric darkMode={darkMode} label="Coolant" value={telemetry?.coolantC ?? "—"} unit="°C" />
              <Metric darkMode={darkMode} label="Battery" value={telemetry?.batteryV ?? "—"} unit="V" />
              <Metric darkMode={darkMode} label="Oil temp" value={telemetry?.oilTempC ?? "—"} unit="°C" />
              <Metric darkMode={darkMode} label="Fuel" value={telemetry?.fuelPct ?? "—"} unit="%" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function InfoTile({ label, value, darkMode }) {
  return (
    <div className={["rounded-2xl p-4 border", darkMode ? "bg-white/5 border-white/10" : "bg-white border-black/10"].join(" ")}>
      <div className={darkMode ? "text-xs text-white/60" : "text-xs text-gray-500"}>{label}</div>
      <div className="mt-1 font-semibold">{value ?? "—"}</div>
    </div>
  );
}

function Metric({ label, value, unit, darkMode }) {
  return (
    <div className={["rounded-2xl p-4 border", darkMode ? "bg-white/5 border-white/10" : "bg-white border-black/10"].join(" ")}>
      <div className={darkMode ? "text-xs text-white/60" : "text-xs text-gray-500"}>{label}</div>
      <div className="mt-2 flex items-end gap-2">
        <div className="text-xl font-bold">{value}</div>
        <div className={darkMode ? "text-sm text-white/50" : "text-sm text-gray-500"}>{unit}</div>
      </div>
    </div>
  );
}
