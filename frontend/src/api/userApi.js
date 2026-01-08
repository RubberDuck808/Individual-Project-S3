import { authFetch } from "./auth";

// Fetch current user
export async function fetchCurrentUser() {
  return authFetch("/api/users/me");
}

// Fetch public info
export async function fetchUserByUsername(username) {
  return authFetch(`/api/users/${encodeURIComponent(username)}`);
}

// Update profile
export async function updateCurrentUser(updates) {
  return authFetch("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(updates),
  });
}

// Local user helper
export function getStoredUser() {
  const raw = localStorage.getItem("user");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
