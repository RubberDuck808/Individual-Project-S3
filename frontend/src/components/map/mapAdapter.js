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
        console.log("MAP LOADED");
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


  // --- MARKERS ---
  addHTMLMarker(element, coords) {
    if (!this.isLoaded()) return null;

    return new mapboxgl.Marker({ element })
      .setLngLat(coords)
      .addTo(this.map);
  }

  addColorMarker(color, coords) {
    if (!this.isLoaded()) return null;

    return new mapboxgl.Marker({ color })
      .setLngLat(coords)
      .addTo(this.map);
  }

  // --- CAMERA ---
  easeToCenter(coords, duration = 600) {
    if (!this.isLoaded()) return;

    this.map.easeTo({
      center: coords,
      duration,
      essential: true,
    });
  }

  fitGeoJSON(geometry, padding = 80, duration = 900) {
    if (!this.isLoaded()) return;

    const bounds = new mapboxgl.LngLatBounds();
    geometry.coordinates.forEach((c) => bounds.extend(c));

    this.map.fitBounds(bounds, { padding, duration });
  }

  // --- ROUTE LAYER ---
  addRouteLayer(routeId, geometry) {
    if (!this.isLoaded()) return;

    if (this.map.getSource(routeId)) {
      if (this.map.getLayer(routeId)) this.map.removeLayer(routeId);
      this.map.removeSource(routeId);
    }

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
