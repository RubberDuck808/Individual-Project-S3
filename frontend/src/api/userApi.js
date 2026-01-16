import { authFetch } from "./auth";

export async function fetchCurrentUser() {
  return authFetch("/api/users/me");
}

export async function fetchUserByUsername(username) {
  return authFetch(`/api/users/${encodeURIComponent(username)}`);
}

export async function updateCurrentUser(updates) {
  return authFetch("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(updates),
  });
}

export function getStoredUser() {
  const raw = localStorage.getItem("user");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}
