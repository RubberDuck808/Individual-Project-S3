import { authFetch } from "./auth";

export async function completeTrip(payload) {
  return authFetch("/api/trips/complete", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
