import { describe, it, expect } from "vitest";
import { getThumbnailUrl } from "../../src/utils/imageUtils";

describe("imageUtils", () => {
  describe("getThumbnailUrl", () => {
    it("should return original URL for valid input", () => {
      // Arrange
      const url = "https://storage.googleapis.com/bucket/image.jpg";
      
      // Act
      const result = getThumbnailUrl(url);
      
      // Assert
      expect(result).toBe(url);
    });
  });
});
