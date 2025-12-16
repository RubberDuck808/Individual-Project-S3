// tests/MapPage.test.jsx
import React from "react";
import { expect, test, vi } from "vitest";
import "@testing-library/jest-dom";
import { render, screen } from "@testing-library/react";
import MapPage from "../src/pages/MapPage";
import { ThemeProvider } from "../src/context/ThemeContext";
import LocationContext from "../src/context/LocationContext";

// Mock hazard API
vi.mock("../src/api/hazardApi", () => ({
  getCategoriesCached: vi.fn().mockResolvedValue([]),
  getAllHazards: vi.fn().mockResolvedValue([]),
}));

// Mock Mapbox GL
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
    NavigationControl: class {},
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

test("renders report button", async () => {
  render(
    <ThemeProvider>
      <LocationContext.Provider value={{ lat: 51.9, lng: 5.2 }}>
        <MapPage />
      </LocationContext.Provider>
    </ThemeProvider>
  );

  const reportButton = await screen.findByText(/Report/i);
  expect(reportButton).toBeInTheDocument();
});
