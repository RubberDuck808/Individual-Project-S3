import { describe, it, expect } from "vitest";
import { getThumbnailUrl } from "../../src/utils/imageUtils";

describe("imageUtils", () => {
  describe("getThumbnailUrl", () => {
    it("should return null for null input", () => {
      expect(getThumbnailUrl(null)).toBeNull();
    });

    it("should return null for undefined input", () => {
      expect(getThumbnailUrl(undefined)).toBeNull();
    });

    it("should return null for empty string", () => {
      expect(getThumbnailUrl("")).toBeNull();
    });

    it("should return original URL for valid input", () => {
      const url = "https://storage.googleapis.com/bucket/image.jpg";
      expect(getThumbnailUrl(url)).toBe(url);
    });

    it("should return original URL for any valid URL string", () => {
      const url = "https://example.com/path/to/image.png";
      expect(getThumbnailUrl(url)).toBe(url);
    });
  });
});
