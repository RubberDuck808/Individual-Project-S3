import { authFetch } from "./auth";

let cachedCategories = null;

// Open hazards
export async function getAllHazards() {
  return authFetch("/api/hazards/open");
}

// Categories
export async function getCategories() {
  return authFetch("/api/hazard-categories");
}

export async function getCategoriesCached() {
  if (cachedCategories) return cachedCategories;

  cachedCategories = await authFetch("/api/hazard-categories");
  return cachedCategories;
}

export async function createHazard({ latitude, longitude, categoryId }) {
  const payload = {
    latitude,
    longitude,
    categoryId,
  };

  return authFetch("/api/hazards", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function getHazardsByUsername(username) {
  return authFetch(`/api/hazards/by-user/${encodeURIComponent(username)}`);
}
