import mapboxgl from "mapbox-gl";

mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

export class MapAdapter {
  constructor(container, style) {
    this.mapLoaded = false;

    this.map = new mapboxgl.Map({
      container,
      style,
      zoom: 14,
      center: [0, 0],
    });

    this.map.on("load", () => {
      this.mapLoaded = true;
    });
  }

  // Check if underlying Mapbox map has finished loading
  isLoaded() {
    return this.mapLoaded;
  }

  onLoad(cb) {
    this.map.on("load", cb);
  }

  onClick(handler) {
    this.map.on("click", (e) => handler(e.lngLat));
  }

  onDragStart(handler) {
    this.map.on("dragstart", handler);
  }

  onZoomStart(handler) {
    this.map.on("zoomstart", handler);
  }

  onUserInteraction(cb) {
    this.map.on("dragstart", cb);
    this.map.on("zoomstart", cb);
    this.map.on("pitchstart", cb);
    this.map.on("rotatestart", cb);

    return () => {
      this.map.off("dragstart", cb);
      this.map.off("zoomstart", cb);
      this.map.off("pitchstart", cb);
      this.map.off("rotatestart", cb);
    };
  }

  // Markers
  addHTMLMarker(element, coords) {
    if (!this.isLoaded()) return null;
    return new mapboxgl.Marker({ element }).setLngLat(coords).addTo(this.map);
  }

  addColorMarker(color, coords) {
    if (!this.isLoaded()) return null;
    return new mapboxgl.Marker({ color }).setLngLat(coords).addTo(this.map);
  }

  // Camera
  easeToCenter(coords, duration = 600) {
    if (!this.isLoaded()) return;
    this.map.easeTo({
      center: coords,
      duration,
      essential: true,
    });
  }

  // EaseTo for camera movement
  easeTo(options) {
    if (!this.isLoaded()) return;
    this.map.easeTo(options);
  }

  // Camera setters
  setZoom(zoom) {
    if (!this.isLoaded()) return;
    this.map.setZoom(zoom);
  }

  setPitch(pitch) {
    if (!this.isLoaded()) return;
    this.map.setPitch(pitch);
  }

  setBearing(bearing) {
    if (!this.isLoaded()) return;
    this.map.setBearing(bearing);
  }

  fitGeoJSON(geometry, padding = 80, duration = 900) {
    if (!this.isLoaded()) return;

    const bounds = new mapboxgl.LngLatBounds();

    const g = geometry?.type === "Feature" ? geometry.geometry : geometry;
    if (!g) return;

    if (g.type === "LineString") {
      g.coordinates.forEach((c) => bounds.extend(c));
    } else if (g.type === "Polygon") {
      g.coordinates?.[0]?.forEach((c) => bounds.extend(c));
    } else if (g.type === "Point") {
      bounds.extend(g.coordinates);
    } else {
      g.coordinates?.forEach?.((c) => bounds.extend(c));
    }

    this.map.fitBounds(bounds, { padding, duration });
  }

  // Route Layer
  addRouteLayer(routeId, geometry) {
    if (!this.isLoaded()) return;

    this.removeRouteLayer(routeId);

    this.map.addSource(routeId, {
      type: "geojson",
      data: {
        type: "Feature",
        geometry,
      },
    });

    this.map.addLayer({
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
  }

  removeRouteLayer(routeId) {
    if (!this.isLoaded()) return;

    try {
      if (this.map.getLayer(routeId)) this.map.removeLayer(routeId);
    } catch {}
    try {
      if (this.map.getSource(routeId)) this.map.removeSource(routeId);
    } catch {}
  }

  getLayer(id) {
    return this.map.getLayer(id);
  }
  getSource(id) {
    return this.map.getSource(id);
  }
  removeLayer(id) {
    return this.map.removeLayer(id);
  }
  removeSource(id) {
    return this.map.removeSource(id);
  }

  // --- STYLE SWITCH ---
  setStyle(style) {
    if (!this.map) return;

    if (this.map.isStyleLoaded()) {
      this.map.setStyle(style);
    } else {
      this.map.once("load", () => this.map.setStyle(style));
    }
  }
}
