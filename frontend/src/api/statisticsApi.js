import { authFetch } from "./auth";

/**
 * Profile summary stats (lifetime)
 * GET /api/users/{username}/stats
 */
export async function getUserStats(username) {
  return authFetch(`/api/users/${encodeURIComponent(username)}/stats`);
}
