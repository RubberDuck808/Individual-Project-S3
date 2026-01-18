import React, { useMemo, useState } from "react";
import PropTypes from "prop-types";
import CarSketch from "./CarSketch";

const MOCK_PLATE_DB = {
  "12-AB-34": { make: "Volkswagen", model: "Golf", year: 2019, fuelType: "Petrol", maxSpeedKmh: 210, massKg: 1280 },
  "99-ZZ-99": { make: "Toyota", model: "Yaris", year: 2016, fuelType: "Hybrid", maxSpeedKmh: 165, massKg: 1090 },
  "EV-2025": { make: "Tesla", model: "Model 3", year: 2024, fuelType: "Electric", maxSpeedKmh: 233, massKg: 1765 },
};

const ERROR_MAP = {
  1: { title: "Low oil pressure", detail: "Oil pressure below threshold.", severity: "high" },
  2: { title: "Engine temperature high", detail: "Coolant temp elevated.", severity: "high" },
  3: { title: "Battery voltage low", detail: "Charging system underperforming.", severity: "medium" },
  7: { title: "Misfire detected", detail: "Misfire detected in one cylinder.", severity: "medium" },
};

function translateErrors(errorCodes = []) {
  return errorCodes.map((code) => ({
    code,
    ...(ERROR_MAP[code] || { title: "Unknown issue", detail: "This error code is not mapped yet.", severity: "low" }),
  }));
}

export default function CarHealthPanel({ plate, setPlate, telemetry }) {
  const [isEditingPlate, setIsEditingPlate] = useState(false);
  const [draftPlate, setDraftPlate] = useState(plate);
  const [plateMsg, setPlateMsg] = useState("");

  const vehicle = useMemo(() => {
    const key = (plate || "").trim().toUpperCase();
    return MOCK_PLATE_DB[key] || null;
  }, [plate]);

  const issues = useMemo(() => translateErrors(telemetry?.errorCodes ?? []), [telemetry]);

  const status = useMemo(() => {
    if (!telemetry?.connected) return "disconnected";
    if ((telemetry?.errorCodes?.length ?? 0) > 0) return "issues";
    return "ok";
  }, [telemetry]);

  const brutalCard = "bg-white border-[4px] border-black rounded-[2.5rem] p-8 shadow-[10px_10px_0px_0px_rgba(0,0,0,1)]";
  const innerCard = "bg-[#F8F9FA] border-[3px] border-black rounded-2xl p-4";

  function saveEdit() {
    const normalized = (draftPlate || "").trim().toUpperCase();
    if (!MOCK_PLATE_DB[normalized]) {
      setPlateMsg("Unknown plate. Try 12-AB-34.");
      return;
    }
    setPlate(normalized);
    setIsEditingPlate(false);
    setPlateMsg("");
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
      {/* LEFT COLUMN */}
      <div className="lg:col-span-5 flex flex-col gap-10">
        <div className={brutalCard}>
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-[1000] uppercase italic text-2xl tracking-tighter">System Status</h3>
            {(() => {
              let statusColor = 'bg-slate-300';
              if (status === 'ok') {
                statusColor = 'bg-[#00D1FF]';
              } else if (status === 'issues') {
                statusColor = 'bg-[#FFD600]';
              }
              return (
                <div className={`px-4 py-1 border-[3px] border-black rounded-full font-black text-xs uppercase ${statusColor}`}>
                  {status}
                </div>
              );
            })()}
          </div>

          <div className="bg-white border-[3px] border-black rounded-[2rem] p-6 mb-8 shadow-[6px_6px_0px_0px_rgba(0,0,0,0.05)]">
            <CarSketch status={status} />
          </div>

          <div className="space-y-4">
            <h4 className="font-black uppercase text-sm tracking-widest text-slate-400">Translated Issues</h4>
            {issues.length === 0 ? (
              <p className="font-bold text-slate-500 italic">No issues detected. Drive on.</p>
            ) : (
              issues.map(it => (
                <div key={it.code} className={innerCard}>
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-black uppercase text-sm">{it.title}</span>
                    <span className="font-mono text-xs bg-black text-white px-2 py-0.5 rounded">#{it.code}</span>
                  </div>
                  <p className="text-sm font-bold text-slate-500">{it.detail}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* RIGHT COLUMN */}
      <div className="lg:col-span-7 flex flex-col gap-10">
        <div className={brutalCard}>
          <div className="flex justify-between items-start mb-8 gap-4 flex-wrap">
             <div>
                <h3 className="font-[1000] uppercase italic text-2xl tracking-tighter text-[#0066FF]">Vehicle Telemetry</h3>
                <div className="mt-2 flex items-center gap-2">
                    <span className="px-3 py-1 bg-black text-[#FFD600] font-black rounded-lg text-sm">{plate}</span>
                    <button onClick={() => setIsEditingPlate(!isEditingPlate)} className="text-xs font-black uppercase underline decoration-2 underline-offset-4 hover:text-[#0066FF]">
                        {isEditingPlate ? "Cancel" : "Edit Plate"}
                    </button>
                </div>
             </div>
             <div className={`text-[10px] font-black uppercase tracking-[0.2em] px-3 py-1 border-2 border-black rounded-md ${telemetry?.connected ? 'bg-emerald-400' : 'bg-red-400'}`}>
                {telemetry?.connected ? "Live Data" : "Offline"}
             </div>
          </div>

          {isEditingPlate && (
            <div className="mb-8 flex flex-col gap-3 animate-in fade-in slide-in-from-top-2">
                <div className="flex gap-3">
                    <input 
                        value={draftPlate} 
                        onChange={(e) => setDraftPlate(e.target.value)}
                        className="bg-white border-[3px] border-black px-4 py-2 rounded-xl font-bold uppercase w-40 outline-none focus:ring-4 ring-[#0066FF]/20"
                    />
                    <button onClick={saveEdit} className="bg-black text-white px-6 py-2 rounded-xl font-black uppercase text-xs">Update</button>
                </div>
                {plateMsg && (
                    <p className="text-xs font-black text-red-500 uppercase">{plateMsg}</p>
                )}
            </div>
          )}

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <MetricCard label="Speed" value={telemetry?.speedKmh ?? "—"} unit="KM/H" color="bg-[#00D1FF]" />
            <MetricCard label="RPM" value={telemetry?.rpm ?? "—"} unit="RPM" color="bg-[#FFD600]" />
            <MetricCard label="Coolant" value={telemetry?.coolantC ?? "—"} unit="°C" color="bg-white" />
            <MetricCard label="Battery" value={telemetry?.batteryV ?? "—"} unit="V" color="bg-white" />
            <MetricCard label="Oil Temp" value={telemetry?.oilTempC ?? "—"} unit="°C" color="bg-white" />
            <MetricCard label="Fuel" value={telemetry?.fuelPct ?? "—"} unit="%" color="bg-[#FF6AC1]" />
          </div>

          <div className="mt-12 pt-8 border-t-[3px] border-black border-dashed flex justify-between items-center">
             <div>
                <span className="text-[10px] font-black uppercase text-slate-400 block mb-1">Active Crew Vehicle</span>
                <span className="text-2xl font-[1000] italic uppercase">{vehicle?.make} {vehicle?.model} <span className="text-slate-300">/</span> {vehicle?.year}</span>
             </div>
             <div className="text-right">
                <span className="text-[10px] font-black uppercase text-slate-400 block mb-1">Fuel Type</span>
                <span className="font-black uppercase text-sm">{vehicle?.fuelType}</span>
             </div>
          </div>
        </div>
      </div>
    </div>
  );
}

CarHealthPanel.propTypes = {
  plate: PropTypes.string,
  setPlate: PropTypes.func.isRequired,
  telemetry: PropTypes.shape({
    connected: PropTypes.bool,
    errorCodes: PropTypes.arrayOf(PropTypes.number),
    speedKmh: PropTypes.number,
    rpm: PropTypes.number,
    coolantC: PropTypes.number,
    batteryV: PropTypes.number,
    oilTempC: PropTypes.number,
    fuelPct: PropTypes.number,
  }),
};

function MetricCard({ label, value, unit, color }) {
  return (
    <div className={`border-[3px] border-black rounded-2xl p-4 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] ${color}`}>
      <div className="text-[10px] font-black uppercase tracking-widest mb-1">{label}</div>
      <div className="flex items-baseline gap-1">
        <span className="text-2xl font-[1000] tracking-tighter">{value}</span>
        <span className="text-[10px] font-black opacity-60">{unit}</span>
      </div>
    </div>
  );
}

MetricCard.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  unit: PropTypes.string.isRequired,
  color: PropTypes.string.isRequired,
};