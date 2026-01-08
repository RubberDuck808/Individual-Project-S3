import { authFetch } from "./auth";

export async function fetchBackgrounds() {
  return authFetch("/api/backgrounds");
}

export async function changeMyBackground(backgroundName) {
  return authFetch("/api/users/me/background", {
    method: "PUT",
    body: JSON.stringify({ backgroundName }),
  });
}
