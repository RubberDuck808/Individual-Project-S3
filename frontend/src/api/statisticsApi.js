import { authFetch } from "./auth";

// Profile stats
export async function getUserStats(username) {
  return authFetch(`/api/users/${encodeURIComponent(username)}/stats`);
}
