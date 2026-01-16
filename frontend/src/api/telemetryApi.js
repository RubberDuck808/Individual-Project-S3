import { authFetch } from "./auth";

/**
 * Get latest live telemetry for a device (real-time map display)
 * @param {string} deviceId - The device ID
 * @returns {Promise<Object>} Live telemetry data (speed, rpm, location)
 */
export async function getLiveTelemetry(deviceId) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  return authFetch(`/api/telemetry/live/${encodeURIComponent(deviceId)}`);
}

/**
 * Get latest car health data for a device (from history)
 * @param {string} deviceId - The device ID
 * @returns {Promise<Object>} Car health data formatted for frontend
 */
export async function getCarHealth(deviceId) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  return authFetch(`/api/telemetry/device/${encodeURIComponent(deviceId)}/health`);
}

/**
 * Get car health history for a device
 * @param {string} deviceId - The device ID
 * @param {number} limit - Number of records to retrieve (default: 50)
 * @returns {Promise<Array>} Array of car health data points
 */
export async function getCarHealthHistory(deviceId, limit = 50) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  return authFetch(
    `/api/telemetry/device/${encodeURIComponent(deviceId)}/health/history?limit=${limit}`
  );
}

/**
 * Get historical telemetry data for a device
 * @param {string} deviceId - The device ID
 * @param {number} limit - Number of records to retrieve (default: 50)
 * @returns {Promise<Array>} Array of telemetry history DTOs
 */
export async function getTelemetryHistory(deviceId, limit = 50) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  return authFetch(
    `/api/telemetry/history/${encodeURIComponent(deviceId)}?limit=${limit}`
  );
}

/**
 * Get telemetry history within a time range
 * @param {string} deviceId - The device ID
 * @param {Date} start - Start time
 * @param {Date} end - End time
 * @param {number} limit - Number of records to retrieve (default: 100)
 * @returns {Promise<Array>} Array of telemetry history DTOs
 */
export async function getTelemetryHistoryRange(deviceId, start, end, limit = 100) {
  if (!deviceId) {
    throw new Error("deviceId is required");
  }
  const startISO = start.toISOString();
  const endISO = end.toISOString();
  return authFetch(
    `/api/telemetry/history/${encodeURIComponent(deviceId)}/range?start=${encodeURIComponent(startISO)}&end=${encodeURIComponent(endISO)}&limit=${limit}`
  );
}
