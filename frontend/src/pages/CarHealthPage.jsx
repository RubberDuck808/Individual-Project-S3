import React, { useEffect, useState, useCallback } from "react";
import CarHealthPanel from "../components/car/CarHealthPanel";
import { getCarHealth } from "../api/telemetryApi";
import { getMyDevices } from "../api/deviceApi";
import { useTelemetryWebSocket } from "../components/car/useTelemetryWebSocket";

export default function CarHealthPage() {
  const [plate, setPlate] = useState("12-AB-34");
  const [telemetry, setTelemetry] = useState(null);
  const [deviceId, setDeviceId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDevice = async () => {
      try {
        const devices = await getMyDevices();
        if (devices && devices.length > 0) {
          const activeDevice = devices.find(d => d.active) || devices[0];
          setDeviceId(activeDevice.deviceId);
        } else {
          setError("No device found. Please add a device in Settings.");
          setLoading(false);
        }
      } catch (err) {
        setError("Failed to load device: " + (err.message || "Unknown error"));
        setLoading(false);
      }
    };

    fetchDevice();
  }, []);

  useEffect(() => {
    if (!deviceId) return;

    const fetchTelemetry = async () => {
      try {
        setLoading(true);
        const health = await getCarHealth(deviceId);
        setTelemetry(health);
        setError(null);
      } catch (err) {
        setError("Failed to load telemetry: " + (err.message || "Unknown error"));
        setTelemetry({ connected: false });
      } finally {
        setLoading(false);
      }
    };

    fetchTelemetry();
  }, [deviceId]);

  const handleTelemetryUpdate = useCallback((carHealth) => {
    setTelemetry(carHealth);
    setError(null);
    setLoading(false);
  }, []);

  useTelemetryWebSocket({
    deviceId,
    enabled: !!deviceId,
    onEvent: handleTelemetryUpdate,
  });

  return (
    <div className="min-h-screen w-full bg-[#FFFDF5] text-black">
      <div className="mx-auto max-w-7xl px-6 pt-32 pb-16 relative z-10">
        <div className="flex items-end justify-between gap-6 flex-wrap mb-12">
          <div>
            <h1 className="text-5xl md:text-6xl font-[1000] uppercase italic tracking-tighter italic">
              Car <span className="text-[#0066FF]">Health</span>
            </h1>
            <p className="mt-4 text-lg font-bold text-slate-500 max-w-md uppercase tracking-tight">
              Real-time telemetry and diagnostic error translation.
            </p>
          </div>

          {error && (
            <div className="px-6 py-3 rounded-2xl border-[3px] border-red-500 bg-red-100 text-red-700 font-black uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
              {error}
            </div>
          )}
          {loading && !error && (
            <div className="px-6 py-3 rounded-2xl border-[3px] border-black bg-[#00D1FF] font-black uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
              Loading telemetry...
            </div>
          )}
        </div>

        {telemetry && (
          <CarHealthPanel
            plate={plate}
            setPlate={setPlate}
            telemetry={telemetry}
          />
        )}
        {!telemetry && !loading && (
          <div className="bg-white border-[4px] border-black rounded-[2.5rem] p-12 shadow-[10px_10px_0px_0px_rgba(0,0,0,1)] text-center">
            <p className="text-xl font-black uppercase text-slate-500">
              {error || "No device connected. Add a device in Settings to view telemetry."}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
