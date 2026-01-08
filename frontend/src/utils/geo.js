export function haversineMeters(lat1, lng1, lat2, lng2) {
  const R = 6371000;
  const toRad = (d) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;

  return 2 * R * Math.asin(Math.sqrt(a));
}

export function findNearestHazardWithin(hazards, location, maxMeters) {
  if (!location) return null;

  let best = null;
  let bestD = Infinity;

  for (const h of hazards) {
    if (!h || h.latitude == null || h.longitude == null) continue;

    const d = haversineMeters(location.lat, location.lng, h.latitude, h.longitude);
    if (d <= maxMeters && d < bestD) {
      best = h;
      bestD = d;
    }
  }

  return best ? { hazard: best, distance: bestD } : null;
}
