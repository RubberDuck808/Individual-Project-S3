import { authFetch } from "./auth";

/**
 * Fetch current user from backend (/me)
 */
export async function fetchCurrentUser() {
  return authFetch("/api/users/me");
}

/**
 * Fetch a user's public profile by username
 * Backend: GET /api/users/{username}
 */
export async function fetchUserByUsername(username) {
  return authFetch(`/api/users/${encodeURIComponent(username)}`);
}

/**
 * Update profile info
 */
export async function updateCurrentUser(updates) {
  return authFetch("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(updates),
  });
}

/**
 * Local user helpers
 */
export function getStoredUser() {
  const raw = localStorage.getItem("user");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
