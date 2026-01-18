import mapboxgl from "mapbox-gl";

// Validate Mapbox token
const MAPBOX_TOKEN = import.meta.env.VITE_MAPBOX_TOKEN;
if (!MAPBOX_TOKEN || MAPBOX_TOKEN.trim() === "") {
  console.error("VITE_MAPBOX_TOKEN is not set. Map functionality will not work.");
  throw new Error("Mapbox access token is required. Please set VITE_MAPBOX_TOKEN in your environment variables.");
}

mapboxgl.accessToken = MAPBOX_TOKEN;

export class MapAdapter {
  mapLoaded = false;

  constructor(container, style) {

    this.map = new mapboxgl.Map({
      container,
      style,
      zoom: 15,
      center: [5.5, 52],
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

    const extendBoundsWithCoordinates = (coords) => {
      for (const c of coords) {
        bounds.extend(c);
      }
    };

    if (g.type === "LineString") {
      extendBoundsWithCoordinates(g.coordinates);
    } else if (g.type === "Polygon") {
      if (g.coordinates?.[0]) {
        extendBoundsWithCoordinates(g.coordinates[0]);
      }
    } else if (g.type === "Point") {
      bounds.extend(g.coordinates);
    } else if (g.coordinates) {
      extendBoundsWithCoordinates(g.coordinates);
    }

    this.map.fitBounds(bounds, { padding, duration });
  }

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
    } catch (error) {
      console.debug("Failed to remove route layer:", error);
    }
    try {
      if (this.map.getSource(routeId)) this.map.removeSource(routeId);
    } catch (error) {
      console.debug("Failed to remove route source:", error);
    }
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

  setStyle(style) {
    if (!this.map) return;

    if (this._styleChanging) {
      return;
    }

    this._styleChanging = true;

    const applyStyle = () => {
      try {
        this.map.setStyle(style);
        this.map.once("style.load", () => {
          this._styleChanging = false;
        });
      } catch (e) {
        console.error("Failed to set map style:", e);
        this._styleChanging = false;
      }
    };

    if (this.map.isStyleLoaded()) {
      applyStyle();
    } else {
      this.map.once("load", applyStyle);
    }
  }
}
