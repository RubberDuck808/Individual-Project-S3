import { authFetch } from "./auth";

// ========== STATISTICS ==========

/**
 * Get admin dashboard statistics
 */
export async function getAdminStatistics() {
  return authFetch("/api/admin/statistics");
}

// ========== USER MANAGEMENT ==========

/**
 * Get all users (paginated)
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Page size
 */
export async function getAllUsers(page = 0, size = 20) {
  return authFetch(`/api/admin/users?page=${page}&size=${size}`);
}

/**
 * Get user by ID
 */
export async function getUserById(id) {
  return authFetch(`/api/admin/users/${id}`);
}

/**
 * Update user role
 */
export async function updateUserRole(userId, roleName) {
  return authFetch(`/api/admin/users/${userId}/role?roleName=${encodeURIComponent(roleName)}`, {
    method: "PUT",
  });
}

/**
 * Deactivate user
 */
export async function deactivateUser(userId) {
  return authFetch(`/api/admin/users/${userId}`, {
    method: "DELETE",
  });
}

// ========== DEVICE MANAGEMENT ==========

/**
 * Get all devices (paginated)
 */
export async function getAllDevices(page = 0, size = 20) {
  return authFetch(`/api/admin/devices?page=${page}&size=${size}`);
}

/**
 * Get device by ID
 */
export async function getDeviceById(id) {
  return authFetch(`/api/admin/devices/${id}`);
}

/**
 * Get device by deviceId
 */
export async function getDeviceByDeviceId(deviceId) {
  return authFetch(`/api/admin/devices/device-id/${encodeURIComponent(deviceId)}`);
}

/**
 * Activate device
 */
export async function activateDevice(id) {
  return authFetch(`/api/admin/devices/${id}/activate`, {
    method: "PUT",
  });
}

/**
 * Deactivate device
 */
export async function deactivateDevice(id) {
  return authFetch(`/api/admin/devices/${id}/deactivate`, {
    method: "PUT",
  });
}

/**
 * Update device description
 */
export async function updateDeviceDescription(id, description) {
  return authFetch(`/api/admin/devices/${id}/description?description=${encodeURIComponent(description)}`, {
    method: "PUT",
  });
}

// ========== ASSET MANAGEMENT (AVATARS) ==========

/**
 * Get all avatars (admin view with usage counts)
 */
export async function getAllAvatars() {
  return authFetch("/api/admin/assets/avatars");
}

/**
 * Get avatar by ID
 */
export async function getAvatarById(id) {
  return authFetch(`/api/admin/assets/avatars/${id}`);
}

/**
 * Create new avatar
 */
export async function createAvatar(name, imagePath) {
  return authFetch("/api/admin/assets/avatars", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, imagePath }),
  });
}

/**
 * Update avatar
 */
export async function updateAvatar(id, name, imagePath, active) {
  return authFetch(`/api/admin/assets/avatars/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, imagePath, active }),
  });
}

/**
 * Delete avatar
 */
export async function deleteAvatar(id) {
  return authFetch(`/api/admin/assets/avatars/${id}`, {
    method: "DELETE",
  });
}

/**
 * Deactivate avatar
 */
export async function deactivateAvatar(id) {
  return authFetch(`/api/admin/assets/avatars/${id}/deactivate`, {
    method: "PUT",
  });
}

// ========== ASSET MANAGEMENT (BACKGROUNDS) ==========

/**
 * Get all backgrounds (admin view with usage counts)
 */
export async function getAllBackgrounds() {
  return authFetch("/api/admin/assets/backgrounds");
}

/**
 * Get background by ID
 */
export async function getBackgroundById(id) {
  return authFetch(`/api/admin/assets/backgrounds/${id}`);
}

/**
 * Create new background
 */
export async function createBackground(name, imagePath) {
  return authFetch("/api/admin/assets/backgrounds", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, imagePath }),
  });
}

/**
 * Update background
 */
export async function updateBackground(id, name, imagePath, active) {
  return authFetch(`/api/admin/assets/backgrounds/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, imagePath, active }),
  });
}

/**
 * Delete background
 */
export async function deleteBackground(id) {
  return authFetch(`/api/admin/assets/backgrounds/${id}`, {
    method: "DELETE",
  });
}

/**
 * Deactivate background
 */
export async function deactivateBackground(id) {
  return authFetch(`/api/admin/assets/backgrounds/${id}/deactivate`, {
    method: "PUT",
  });
}
