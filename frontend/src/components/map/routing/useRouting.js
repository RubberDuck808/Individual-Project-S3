const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_TOKEN;

export async function fetchRoute(origin, destination) {
  const [oLng, oLat] = origin;
  const [dLng, dLat] = destination;

  const url =
    `https://api.mapbox.com/directions/v5/mapbox/driving/` +
    `${oLng},${oLat};${dLng},${dLat}` +
    `?geometries=geojson&overview=full&steps=true&annotations=distance,duration` +
    `&access_token=${MAPBOX_TOKEN}`;

  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch route");
  const data = await res.json();

  const route = data?.routes?.[0];
  const leg = route?.legs?.[0];

  if (!route || !leg) return null;

  return {
    geometry: route.geometry,
    distance: route.distance, 
    duration: route.duration,
    steps: leg.steps || [],
  };
}
