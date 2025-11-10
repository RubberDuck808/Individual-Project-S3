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

  // Initialize map
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

    // Start GPS tracking
    if (navigator.geolocation) {
      const watchId = navigator.geolocation.watchPosition(
        (pos) => {
          const { latitude, longitude, accuracy } = pos.coords;
          const coords = [longitude, latitude];
          userLocationRef.current = { lat: latitude, lng: longitude };

          // Add or update user marker
          if (!userMarkerRef.current) {
            userMarkerRef.current = new mapboxgl.Marker({ color: "red" })
              .setLngLat(coords)
              .addTo(map);
          } else {
            userMarkerRef.current.setLngLat(coords);
          }

          // Auto-follow user
          map.flyTo({
            center: coords,
            zoom: 15,
            speed: 1.5,
            essential: true,
          });

          onUserLocation?.({ lat: latitude, lng: longitude, accuracy });
        },
        (err) => console.error("GPS Error:", err),
        { enableHighAccuracy: true, maximumAge: 0, timeout: 5000 }
      );

      return () => navigator.geolocation.clearWatch(watchId);
    }
  }, []); // Only run once on mount

  // Watch for theme changes and update style dynamically
  useEffect(() => {
    if (mapRef.current) {
      const newStyle = darkMode
        ? "mapbox://styles/mapbox/navigation-night-v1"
        : "mapbox://styles/mapbox/streets-v11";
      mapRef.current.setStyle(newStyle);
    }
  }, [darkMode]);

  // --- Routing helpers ---
  const getDistanceKm = (a, b) => Math.hypot(a[0] - b[0], a[1] - b[1]) * 111;

  const fetchDirections = (origin, destination) =>
    fetch(
      `https://api.mapbox.com/directions/v5/mapbox/driving/${origin.join(
        ","
      )};${destination.join(
        ","
      )}?geometries=geojson&overview=full&steps=true&access_token=${
        mapboxgl.accessToken
      }`
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
      )}?geometries=geojson&overview=full&steps=true&radiuses=50;50&access_token=${
        mapboxgl.accessToken
      }`
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

  // Expose routing method to parent
  useImperativeHandle(ref, () => ({
    goToAndRoute(lng, lat) {
      if (!mapRef.current || !userLocationRef.current) return;

      const origin = [userLocationRef.current.lng, userLocationRef.current.lat];
      const destination = [lng, lat];

      if (destinationMarkerRef.current)
        destinationMarkerRef.current.remove();

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

        // Remove old route
        if (mapRef.current.getSource(routeId)) {
          mapRef.current.removeLayer(routeId);
          mapRef.current.removeSource(routeId);
        }

        // Add new route
        mapRef.current.addSource(routeId, {
          type: "geojson",
          data: {
            type: "Feature",
            properties: {},
            geometry,
          },
        });

        mapRef.current.addLayer({
          id: routeId,
          type: "line",
          source: routeId,
          layout: {
            "line-join": "round",
            "line-cap": "round",
          },
          paint: {
            "line-color": "#1d4ed8",
            "line-width": 5,
          },
        });

        // Fit to bounds
        const bounds = new mapboxgl.LngLatBounds();
        geometry.coordinates.forEach((c) => bounds.extend(c));
        mapRef.current.fitBounds(bounds, { padding: 80, duration: 900 });

        onRoutePreview?.({ distance, duration });
      });
    },
  }));

  return <div ref={mapContainerRef} className="w-full h-full" />;
});

export default UserLocationMap;
