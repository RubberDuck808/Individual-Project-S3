import { useRef, useEffect, useImperativeHandle, forwardRef } from "react";
import mapboxgl from "mapbox-gl";
import { useTheme } from "../context/ThemeContext";

mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

const UserLocationMap = forwardRef(({ onUserLocation, onRoutePreview }, ref) => {
  const mapRef = useRef(null);
  const mapContainerRef = useRef(null);
  const userMarkerRef = useRef(null);
  const destinationMarkerRef = useRef(null);
  const userLocationRef = useRef(null);
  const { darkMode } = useTheme();

  const routeId = "route-line";

  // Distance helper
  const getDistanceKm = (a, b) => Math.hypot(a[0] - b[0], a[1] - b[1]) * 111;

  // Routing endpoints
  const fetchDirections = (origin, destination) =>
    fetch(
      `https://api.mapbox.com/directions/v5/mapbox/driving/${origin.join(
        ","
      )};${destination.join(
        ","
      )}?geometries=geojson&overview=full&steps=true&access_token=${mapboxgl.accessToken}`
    )
      .then((res) => res.json())
      .then((data) =>
        data.routes?.[0]
          ? {
              geometry: data.routes[0].geometry,
              distance: data.routes[0].distance,
              duration: data.routes[0].duration,
            }
          : null
      );

  const fetchMatchedRoute = (origin, destination) =>
    fetch(
      `https://api.mapbox.com/matching/v5/mapbox/driving/${origin.join(
        ","
      )};${destination.join(
        ","
      )}?geometries=geojson&overview=full&steps=true&radiuses=50;50&access_token=${mapboxgl.accessToken}`
    )
      .then((res) => res.json())
      .then((data) =>
        data.matchings?.[0]
          ? {
              geometry: data.matchings[0].geometry,
              distance: data.matchings[0].distance,
              duration: data.matchings[0].duration,
            }
          : null
      );

  // Shared routing logic
  const internalGoToAndRoute = (lng, lat) => {
    if (!mapRef.current || !userLocationRef.current) return;

    const origin = [userLocationRef.current.lng, userLocationRef.current.lat];
    const destination = [lng, lat];

    if (destinationMarkerRef.current) {
      destinationMarkerRef.current.remove();
    }

    destinationMarkerRef.current = new mapboxgl.Marker({ color: "blue" })
      .setLngLat(destination)
      .addTo(mapRef.current);

    const distance = getDistanceKm(origin, destination);
    const routePromise =
      distance < 3
        ? fetchMatchedRoute(origin, destination)
        : fetchDirections(origin, destination);

    routePromise.then((routeData) => {
      if (!routeData) return;

      const { geometry, distance, duration } = routeData;

      // Remove previous route
      if (mapRef.current.getSource(routeId)) {
        mapRef.current.removeLayer(routeId);
        mapRef.current.removeSource(routeId);
      }

      mapRef.current.addSource(routeId, {
        type: "geojson",
        data: {
          type: "Feature",
          geometry,
        },
      });

      mapRef.current.addLayer({
        id: routeId,
        type: "line",
        source: routeId,
        paint: {
          "line-color": "#1d4ed8",
          "line-width": 5,
        },
        layout: {
          "line-join": "round",
          "line-cap": "round",
        },
      });

      // Fit map to route
      const bounds = new mapboxgl.LngLatBounds();
      geometry.coordinates.forEach((c) => bounds.extend(c));
      mapRef.current.fitBounds(bounds, { padding: 80, duration: 900 });

      onRoutePreview?.({ distance, duration });
    });
  };

  useImperativeHandle(ref, () => ({
    goToAndRoute: internalGoToAndRoute,
  }));

  // Init map + GPS tracking
  useEffect(() => {
    const map = new mapboxgl.Map({
      container: mapContainerRef.current,
      style: darkMode
        ? "mapbox://styles/mapbox/dark-v11"
        : "mapbox://styles/mapbox/streets-v11",
      center: [0, 0],
      zoom: 2,
    });

    mapRef.current = map;

    // Click → put marker + route
    map.on("click", (e) => {
      const { lng, lat } = e.lngLat;
      internalGoToAndRoute(lng, lat);
    });

    // GPS tracking (NO accuracy checks)
    if (navigator.geolocation) {
      const watchId = navigator.geolocation.watchPosition(
        (pos) => {
          const { latitude, longitude } = pos.coords;
          const coords = [longitude, latitude];

          userLocationRef.current = { lng: longitude, lat: latitude };

          // Create or update marker
          if (!userMarkerRef.current) {
            userMarkerRef.current = new mapboxgl.Marker({ color: "red" })
              .setLngLat(coords)
              .addTo(map);
          } else {
            userMarkerRef.current.setLngLat(coords);
          }

          // Always follow user
          map.flyTo({
            center: coords,
            zoom: 15,
            speed: 1.3,
            essential: true,
          });

          onUserLocation?.({ lat: latitude, lng: longitude });
        },
        (err) => console.error("GPS Error:", err),
        {
          enableHighAccuracy: true,
          maximumAge: 0,
          timeout: 8000,
        }
      );

      return () => navigator.geolocation.clearWatch(watchId);
    }
  }, []);

  // Theme switch updates map style
  useEffect(() => {
    if (mapRef.current) {
      mapRef.current.setStyle(
        darkMode
          ? "mapbox://styles/mapbox/navigation-night-v1"
          : "mapbox://styles/mapbox/streets-v11"
      );
    }
  }, [darkMode]);

  return <div ref={mapContainerRef} className="w-full h-full" />;
});

export default UserLocationMap;
