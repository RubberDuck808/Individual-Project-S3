import { describe, it, expect } from "vitest";
import { haversineMeters, findNearestHazardWithin } from "../../src/utils/geo";

describe("geo utilities", () => {
  describe("haversineMeters", () => {
    it("should calculate distance between two points", () => {
      // Distance between Amsterdam and Utrecht (approximately 34km)
      const amsterdam = { lat: 52.3676, lng: 4.9041 };
      const utrecht = { lat: 52.0907, lng: 5.1214 };
      
      const distance = haversineMeters(
        amsterdam.lat,
        amsterdam.lng,
        utrecht.lat,
        utrecht.lng
      );
      
      // Should be approximately 34km (34000m), allow 5% tolerance
      expect(distance).toBeGreaterThan(32000);
      expect(distance).toBeLessThan(36000);
    });

    it("should return 0 for same coordinates", () => {
      const distance = haversineMeters(52.3676, 4.9041, 52.3676, 4.9041);
      expect(distance).toBe(0);
    });

    it("should handle negative coordinates", () => {
      const distance = haversineMeters(-52.3676, -4.9041, -52.0907, -5.1214);
      expect(distance).toBeGreaterThan(0);
    });
  });

  describe("findNearestHazardWithin", () => {
    it("should return null if location is not provided", () => {
      const hazards = [
        { id: 1, latitude: 52.3676, longitude: 4.9041 }
      ];
      const result = findNearestHazardWithin(hazards, null, 1000);
      expect(result).toBeNull();
    });

    it("should return null if no hazards are within range", () => {
      const location = { lat: 52.3676, lng: 4.9041 };
      const hazards = [
        { id: 1, latitude: 53.0, longitude: 5.0 } // Far away
      ];
      const result = findNearestHazardWithin(hazards, location, 1000);
      expect(result).toBeNull();
    });

    it("should find nearest hazard within range", () => {
      const location = { lat: 52.3676, lng: 4.9041 };
      const hazards = [
        { id: 1, latitude: 52.3680, longitude: 4.9045 }, // Close (~50m)
        { id: 2, latitude: 52.3700, longitude: 4.9060 }, // Further (~200m)
        { id: 3, latitude: 53.0, longitude: 5.0 } // Too far
      ];
      const result = findNearestHazardWithin(hazards, location, 1000);
      
      expect(result).not.toBeNull();
      expect(result.hazard.id).toBe(1);
      expect(result.distance).toBeLessThan(100);
    });

    it("should return the closest hazard when multiple are within range", () => {
      const location = { lat: 52.3676, lng: 4.9041 };
      const hazards = [
        { id: 1, latitude: 52.3700, longitude: 4.9060 }, // ~200m
        { id: 2, latitude: 52.3680, longitude: 4.9045 }, // ~50m (closest)
        { id: 3, latitude: 52.3690, longitude: 4.9050 }  // ~150m
      ];
      const result = findNearestHazardWithin(hazards, location, 1000);
      
      expect(result).not.toBeNull();
      expect(result.hazard.id).toBe(2);
    });

    it("should skip hazards with missing coordinates", () => {
      const location = { lat: 52.3676, lng: 4.9041 };
      const hazards = [
        { id: 1, latitude: null, longitude: 4.9041 },
        { id: 2, latitude: 52.3676, longitude: null },
        { id: 3, latitude: 52.3680, longitude: 4.9045 } // Valid
      ];
      const result = findNearestHazardWithin(hazards, location, 1000);
      
      expect(result).not.toBeNull();
      expect(result.hazard.id).toBe(3);
    });

    it("should handle empty hazards array", () => {
      const location = { lat: 52.3676, lng: 4.9041 };
      const result = findNearestHazardWithin([], location, 1000);
      expect(result).toBeNull();
    });
  });
});
