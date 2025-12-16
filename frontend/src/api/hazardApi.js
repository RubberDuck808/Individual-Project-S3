import { authFetch } from "./auth";

let cachedCategories = null;

// Open hazards are hazards that the user can vote on
export async function getAllHazards() {
  return authFetch("/api/hazards/open");
}

// Categories show when making a report
export async function getCategories() {
  return authFetch("/api/hazard-categories");
}

export async function getCategoriesCached() {
  if (cachedCategories) return cachedCategories;

  cachedCategories = await authFetch("/api/hazard-categories");
  return cachedCategories;
}

export async function createHazard({ latitude, longitude, categoryId, userId }) {
  const payload = {
    latitude,
    longitude,
    categoryId,
    createdBy: userId,
  };

  return authFetch("/api/hazards", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
