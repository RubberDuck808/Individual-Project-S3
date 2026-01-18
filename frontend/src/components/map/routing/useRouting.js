const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_TOKEN;

function isValidCoordinate(coord) {
  return typeof coord === "number" && !Number.isNaN(coord) && Number.isFinite(coord);
}

export async function fetchRoute(origin, destination) {
  validateCoordinates(origin, destination);
  validateToken();
  
  const url = buildRouteUrl(origin, destination);
  
  try {
    const data = await fetchRouteData(url);
    return extractRouteInfo(data);
  } catch (error) {
    throw handleRouteError(error);
  }
}

function validateCoordinates(origin, destination) {
  const [oLng, oLat] = origin;
  const [dLng, dLat] = destination;

  if (!isValidCoordinate(oLng) || !isValidCoordinate(oLat) || 
      !isValidCoordinate(dLng) || !isValidCoordinate(dLat)) {
    throw new Error("Invalid coordinates provided for route calculation");
  }

  if (oLng < -180 || oLng > 180 || dLng < -180 || dLng > 180) {
    throw new Error("Longitude must be between -180 and 180");
  }
  if (oLat < -90 || oLat > 90 || dLat < -90 || dLat > 90) {
    throw new Error("Latitude must be between -90 and 90");
  }
}

function validateToken() {
  if (MAPBOX_TOKEN && MAPBOX_TOKEN.trim() !== "") {
    return;
  }
  throw new Error("Mapbox access token is not configured");
}

function buildRouteUrl(origin, destination) {
  const [oLng, oLat] = origin;
  const [dLng, dLat] = destination;
  
  return `https://api.mapbox.com/directions/v5/mapbox/driving/` +
    `${oLng},${oLat};${dLng},${dLat}` +
    `?geometries=geojson&overview=full&steps=true&annotations=distance,duration` +
    `&access_token=${MAPBOX_TOKEN}`;
}

async function fetchRouteData(url) {
  const res = await fetch(url);
  
  if (!res.ok) {
    let errorText = "Unknown error";
    try {
      errorText = await res.text();
    } catch (textError) {
      console.debug("Failed to read error response:", textError);
    }
    if (res.status === 401) {
      throw new Error("Invalid Mapbox access token");
    } else if (res.status === 422) {
      throw new Error("Unable to calculate route. Please check your coordinates.");
    } else if (res.status === 429) {
      throw new Error("Rate limit exceeded. Please try again later.");
    } else {
      throw new Error(`Failed to fetch route: ${res.status} ${errorText}`);
    }
  }

  const data = await res.json();
  
  if (data.code && data.message) {
    throw new Error(`Mapbox API error: ${data.message}`);
  }
  
  return data;
}

function extractRouteInfo(data) {
  const route = data?.routes?.[0];
  const leg = route?.legs?.[0];

  if (route && leg) {
    return {
      geometry: route.geometry,
      distance: route.distance, 
      duration: route.duration,
      steps: leg.steps || [],
    };
  }
  throw new Error("No route found between the specified locations");
}

function handleRouteError(error) {
  if (error instanceof TypeError && error.message.includes("fetch")) {
    return new Error("Network error: Unable to connect to routing service");
  }
  return error;
}
