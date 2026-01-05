vi.mock("mapbox-gl", () => ({
  __esModule: true,
  default: {
    Map: class {
      constructor() {
        this.on = vi.fn();
        this.remove = vi.fn();
        this.flyTo = vi.fn();
        this.getCenter = vi.fn().mockReturnValue({ lng: 0, lat: 0 });
        this.addLayer = vi.fn();
        this.addSource = vi.fn();
        this.setLayoutProperty = vi.fn();
      }
    },
    NavigationControl: vi.fn(),
    Marker: class {
      setLngLat() { return this; }
      addTo() { return this; }
      remove() { return this; }
    },
    Popup: class {
      setLngLat() { return this; }
      setHTML() { return this; }
      addTo() { return this; }
      remove() { return this; }
    },
    accessToken: "",
  },
}));
