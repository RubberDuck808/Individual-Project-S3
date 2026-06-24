import React, { useEffect, useState } from "react";
import { getAllDevices, activateDevice, deactivateDevice, updateDeviceDescription } from "../../api/adminApi";
import { Cpu, Power, PowerOff, Edit, Activity, Clock } from "lucide-react";
import { Link } from "react-router-dom";
import { useToast } from "../../context/ToastContext";

export default function AdminDevicesPage() {
  const toast = useToast();
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [editingDescription, setEditingDescription] = useState(null);
  const [newDescription, setNewDescription] = useState("");

  useEffect(() => {
    fetchDevices();
  }, [page]);

  const fetchDevices = async () => {
    try {
      setLoading(true);
      const response = await getAllDevices(page, 20);
      setDevices(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      setError(err.message || "Failed to load devices");
    } finally {
      setLoading(false);
    }
  };

  const handleToggleActive = async (device, activate) => {
    try {
      if (activate) {
        await activateDevice(device.id);
      } else {
        await deactivateDevice(device.id);
      }
      fetchDevices();
    } catch (err) {
      toast.error("Failed to update device: " + err.message);
    }
  };

  const handleUpdateDescription = async (deviceId) => {
    try {
      await updateDeviceDescription(deviceId, newDescription);
      setEditingDescription(null);
      fetchDevices();
    } catch (err) {
      toast.error("Failed to update description: " + err.message);
    }
  };

  const getLastSeenStatus = (lastSeenAt) => {
    if (!lastSeenAt) return { text: "Never", color: "text-gray-500" };
    
    const lastSeen = new Date(lastSeenAt);
    const now = new Date();
    const hoursAgo = (now - lastSeen) / (1000 * 60 * 60);
    
    if (hoursAgo < 1) return { text: "Just now", color: "text-green-600" };
    if (hoursAgo < 24) return { text: `${Math.floor(hoursAgo)}h ago`, color: "text-green-600" };
    if (hoursAgo < 168) return { text: `${Math.floor(hoursAgo / 24)}d ago`, color: "text-yellow-600" };
    return { text: `${Math.floor(hoursAgo / 24)}d ago`, color: "text-red-600" };
  };

  if (loading && devices.length === 0) {
    return <div className="text-center py-12">Loading devices...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-black text-3xl mb-2">Device Management</h1>
        <p className="text-gray-600">Manage all ESP32 devices and monitor their performance</p>
      </div>

      {error && (
        <div className="bg-red-100 border-2 border-red-500 rounded-lg p-4 text-red-700">
          {error}
        </div>
      )}

      {/* Devices Table */}
      <div className="bg-white border-2 border-black rounded-2xl shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-100 border-b-2 border-black">
              <tr>
                <th className="px-6 py-4 text-left font-black">Device ID</th>
                <th className="px-6 py-4 text-left font-black">Description</th>
                <th className="px-6 py-4 text-left font-black">Owner</th>
                <th className="px-6 py-4 text-left font-black">Status</th>
                <th className="px-6 py-4 text-left font-black">Performance</th>
                <th className="px-6 py-4 text-left font-black">Last Seen</th>
                <th className="px-6 py-4 text-left font-black">Actions</th>
              </tr>
            </thead>
            <tbody>
              {devices.map((device) => {
                const lastSeenStatus = getLastSeenStatus(device.lastSeenAt);
                return (
                  <tr key={device.id} className="border-b border-gray-200 hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <Cpu size={20} />
                        <span className="font-bold">{device.deviceId}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      {editingDescription === device.id ? (
                        <div className="flex items-center gap-2">
                          <input
                            type="text"
                            value={newDescription}
                            onChange={(e) => setNewDescription(e.target.value)}
                            className="border-2 border-black rounded-lg px-3 py-1 flex-1"
                            placeholder="Device description"
                          />
                          <button
                            onClick={() => handleUpdateDescription(device.id)}
                            className="bg-green-500 text-white px-3 py-1 rounded-lg font-bold hover:bg-green-600"
                          >
                            Save
                          </button>
                          <button
                            onClick={() => setEditingDescription(null)}
                            className="bg-gray-500 text-white px-3 py-1 rounded-lg font-bold hover:bg-gray-600"
                          >
                            Cancel
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center gap-2">
                          <span>{device.description || "No description"}</span>
                          <button
                            onClick={() => {
                              setEditingDescription(device.id);
                              setNewDescription(device.description || "");
                            }}
                            className="text-blue-600 hover:text-blue-800"
                          >
                            <Edit size={16} />
                          </button>
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      {device.currentOwnerUsername ? (
                        <Link
                          to={`/profile/${device.currentOwnerUsername}`}
                          className="text-blue-600 hover:text-blue-800 font-bold"
                        >
                          @{device.currentOwnerUsername}
                        </Link>
                      ) : (
                        <span className="text-gray-400">Unassigned</span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {device.active ? (
                          <>
                            <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                            <span className="font-bold text-green-600">Active</span>
                          </>
                        ) : (
                          <>
                            <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                            <span className="font-bold text-red-600">Inactive</span>
                          </>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="space-y-1 text-sm">
                        {device.lastSpeedKph !== null && (
                          <div className="flex items-center gap-2">
                            <Activity size={14} />
                            <span>{device.lastSpeedKph.toFixed(1)} km/h</span>
                            {device.lastRpm !== null && <span>• {device.lastRpm.toFixed(0)} RPM</span>}
                          </div>
                        )}
                        <div className="text-gray-600">
                          {device.totalTelemetryCount || 0} records
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <Clock size={14} />
                        <span className={lastSeenStatus.color}>{lastSeenStatus.text}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {device.active ? (
                          <button
                            onClick={() => handleToggleActive(device, false)}
                            className="text-red-600 hover:text-red-800"
                            title="Deactivate device"
                          >
                            <PowerOff size={18} />
                          </button>
                        ) : (
                          <button
                            onClick={() => handleToggleActive(device, true)}
                            className="text-green-600 hover:text-green-800"
                            title="Activate device"
                          >
                            <Power size={18} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-6 py-4 border-t-2 border-black flex items-center justify-between">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-4 py-2 bg-gray-200 rounded-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
            >
              Previous
            </button>
            <span className="font-bold">
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 bg-gray-200 rounded-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
