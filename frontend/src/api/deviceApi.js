import { authFetch } from "./auth";

/**
 * Register a new device
 * @param {string} deviceId - The device ID (e.g., "ESP32-ABC123")
 * @param {string} description - Optional device description
 * @returns {Promise<Object>} Device registration response with API key
 */
export async function registerDevice(deviceId, description) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  return authFetch("/api/devices/register", {
    method: "POST",
    body: JSON.stringify({
      deviceId,
      description: description || null,
    }),
  });
}

/**
 * Get all devices owned by the current user
 * @returns {Promise<Array>} Array of device ownership records
 */
export async function getMyDevices() {
  return authFetch("/api/devices/my-devices");
}

/**
 * Assign a device to the current user
 * @param {string} deviceId - The device ID to assign
 * @param {string} notes - Optional notes
 * @returns {Promise<Object>} Device ownership record
 */
export async function assignDevice(deviceId, notes) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  const url = notes
    ? `/api/devices/${encodeURIComponent(deviceId)}/assign?notes=${encodeURIComponent(notes)}`
    : `/api/devices/${encodeURIComponent(deviceId)}/assign`;
  return authFetch(url, {
    method: "POST",
  });
}

/**
 * Unassign a device from the current user
 * @param {string} deviceId - The device ID to unassign
 * @param {string} notes - Optional notes
 * @returns {Promise<void>}
 */
export async function unassignDevice(deviceId, notes) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  const url = notes
    ? `/api/devices/${encodeURIComponent(deviceId)}/unassign?notes=${encodeURIComponent(notes)}`
    : `/api/devices/${encodeURIComponent(deviceId)}/unassign`;
  return authFetch(url, {
    method: "DELETE",
  });
}
