// Load all open hazards
export async function getAllHazards() {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/hazards/open`);
  if (!res.ok) throw new Error("Failed to load hazards");
  return res.json();
}

// Load all hazard categories
export async function getCategories() {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/hazard-categories`);
  if (!res.ok) throw new Error("Failed to load categories");
  return res.json();
}

// Create a new hazard
export async function createHazard({ latitude, longitude, categoryId, userId }) {

  const payload = {
    latitude,
    longitude,
    categoryId,
    createdByUserId: userId,
  };

  const res = await fetch(`${import.meta.env.VITE_API_URL}/api/hazards`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error(await res.text());
  }

  return res.json();
}
