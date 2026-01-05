import mapboxgl from "mapbox-gl";

export async function fetchRoute(origin, destination) {
  const distKm = Math.hypot(
    origin[0] - destination[0],
    origin[1] - destination[1]
  ) * 111;

  const base = distKm < 3 ? "matching" : "directions";

  const url =
    base === "directions"
      ? `https://api.mapbox.com/directions/v5/mapbox/driving/${origin.join(
          ","
        )};${destination.join(",")}?geometries=geojson&overview=full&steps=true&access_token=${
          mapboxgl.accessToken
        }`
      : `https://api.mapbox.com/matching/v5/mapbox/driving/${origin.join(
          ","
        )};${destination.join(
          ","
        )}?geometries=geojson&overview=full&steps=true&radiuses=50;50&access_token=${
          mapboxgl.accessToken
        }`;

  const json = await fetch(url).then((r) => r.json());

  const route =
    base === "directions" ? json.routes?.[0] : json.matchings?.[0];

  if (!route) return null;

  return {
    geometry: route.geometry,
    distance: route.distance,
    duration: route.duration,
  };
}
