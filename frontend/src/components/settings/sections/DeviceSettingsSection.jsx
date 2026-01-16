import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { getMyDevices, registerDevice, unassignDevice } from "../../../api/deviceApi";

export default function DeviceSettingsSection({ me }) {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [showRegisterForm, setShowRegisterForm] = useState(false);
  const [registering, setRegistering] = useState(false);
  const [apiKey, setApiKey] = useState(null);
  
  const [registerForm, setRegisterForm] = useState({
    deviceId: "",
    description: "",
  });

  useEffect(() => {
    fetchDevices();
  }, []);

  const fetchDevices = async () => {
    try {
      setLoading(true);
      const myDevices = await getMyDevices();
      // Ensure we have an array
      if (Array.isArray(myDevices)) {
        setDevices(myDevices);
      } else {
        setDevices([]);
      }
      setError("");
    } catch (err) {
      console.error("Error fetching devices:", err);
      setError("Failed to load devices: " + (err.message || "Unknown error"));
      setDevices([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setRegistering(true);

    try {
      // Check if user already has a device
      if (devices.length > 0) {
        setError("You can only have 1 device. Please remove your current device first.");
        setRegistering(false);
        return;
      }

      const response = await registerDevice(
        registerForm.deviceId.trim(),
        registerForm.description.trim() || null
      );

      setApiKey(response.apiKey);
      setSuccess("Device registered successfully! Copy your API key - it won't be shown again.");
      setRegisterForm({ deviceId: "", description: "" });
      setShowRegisterForm(false);
      await fetchDevices();
    } catch (err) {
      setError(err?.message || "Failed to register device");
    } finally {
      setRegistering(false);
    }
  };

  const handleRemove = async (deviceId) => {
    if (!confirm("Are you sure you want to remove this device? You'll need to register it again to use it.")) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      await unassignDevice(deviceId, "Removed by user");
      setSuccess("Device removed successfully.");
      await fetchDevices();
    } catch (err) {
      setError("Failed to remove device: " + (err.message || "Unknown error"));
    }
  };

  const inputClass = "w-full p-4 rounded-2xl border-[3px] border-black bg-slate-50 font-bold focus:bg-white focus:outline-none transition-all focus:-translate-y-0.5 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] focus:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]";
  const labelClass = "text-[10px] font-black uppercase ml-2 mb-1 text-slate-500 tracking-widest";

  return (
    <div className="w-full space-y-6">
      <h2 className="text-xl font-[1000] uppercase italic tracking-tighter mb-2">ESP32 Device</h2>
      <p className="text-sm text-slate-600 font-bold mb-4">
        Connect your ESP32 device to view real-time car telemetry. Maximum 1 device per user.
      </p>

      {error && (
        <div className="p-3 bg-red-100 border-2 border-red-500 rounded-xl text-red-600 text-xs font-black uppercase">
          {error}
        </div>
      )}
      
      {success && (
        <div className="p-3 bg-green-100 border-2 border-green-500 rounded-xl text-green-600 text-xs font-black uppercase">
          {success}
        </div>
      )}

      {apiKey && (
        <div className="p-4 bg-yellow-100 border-[3px] border-yellow-500 rounded-xl">
          <p className="text-xs font-black uppercase text-yellow-800 mb-2">⚠️ API KEY (Copy this now - it won't be shown again!)</p>
          <div className="bg-white border-2 border-black p-3 rounded-lg font-mono text-sm break-all">
            {apiKey}
          </div>
          <button
            onClick={() => {
              navigator.clipboard.writeText(apiKey);
              setSuccess("API key copied to clipboard!");
            }}
            className="mt-2 px-4 py-2 bg-black text-white rounded-lg font-black uppercase text-xs hover:bg-slate-800"
          >
            Copy API Key
          </button>
        </div>
      )}

      {loading && (
        <div className="text-center py-8">
          <p className="text-slate-500 font-bold">Loading devices...</p>
        </div>
      )}
      {!loading && devices.length === 0 && (
        <div className="bg-white border-[3px] border-black rounded-2xl p-6 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
          <p className="text-sm font-bold text-slate-600 mb-4">No device connected.</p>
          {showRegisterForm ? (
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="flex flex-col">
                <label htmlFor="deviceId" className={labelClass}>Device ID</label>
                <input
                  id="deviceId"
                  type="text"
                  placeholder="e.g., ESP32-ABC123"
                  value={registerForm.deviceId}
                  onChange={(e) => setRegisterForm({ ...registerForm, deviceId: e.target.value })}
                  className={inputClass}
                  required
                />
                <p className="text-xs text-slate-500 mt-1">Unique identifier for your ESP32 device</p>
              </div>

              <div className="flex flex-col">
                <label htmlFor="deviceDescription" className={labelClass}>Description (Optional)</label>
                <input
                  id="deviceDescription"
                  type="text"
                  placeholder="e.g., My Car ESP32"
                  value={registerForm.description}
                  onChange={(e) => setRegisterForm({ ...registerForm, description: e.target.value })}
                  className={inputClass}
                />
              </div>

              <div className="flex gap-3">
                <button
                  type="submit"
                  disabled={registering}
                  className="flex-1 px-6 py-3 bg-[#00D1FF] border-[3px] border-black rounded-xl font-black uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {registering ? "Registering..." : "Register Device"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowRegisterForm(false);
                    setRegisterForm({ deviceId: "", description: "" });
                  }}
                  className="px-6 py-3 bg-slate-200 border-[3px] border-black rounded-xl font-black uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : (
            <button
              onClick={() => setShowRegisterForm(true)}
              className="px-6 py-3 bg-[#00D1FF] border-[3px] border-black rounded-xl font-black uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all"
            >
              Register Device
            </button>
          )}
        </div>
      )}
      {!loading && devices.length > 0 && (
        <div className="space-y-4">
          {devices.map((device) => (
            <div
              key={device.id}
              className="bg-white border-[3px] border-black rounded-2xl p-6 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]"
            >
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="font-[1000] uppercase text-lg tracking-tighter">
                    {device.deviceId}
                  </h3>
                  {device.notes && (
                    <p className="text-sm text-slate-600 font-bold mt-1">{device.notes}</p>
                  )}
                  <p className="text-xs text-slate-500 mt-2">
                    Registered: {new Date(device.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <div className={`px-3 py-1 rounded-lg border-2 border-black text-xs font-black uppercase ${
                  device.active ? "bg-green-400" : "bg-slate-200"
                }`}>
                  {device.active ? "Active" : "Inactive"}
                </div>
              </div>

              <button
                onClick={() => handleRemove(device.deviceId)}
                className="px-4 py-2 bg-red-500 text-white border-[3px] border-black rounded-xl font-black uppercase text-xs tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-y-0.5 hover:shadow-none transition-all"
              >
                Remove Device
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

DeviceSettingsSection.propTypes = {
  me: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  }),
};
