import { describe, it, expect } from "vitest";
import { getThumbnailUrl } from "../../src/utils/imageUtils";

describe("imageUtils", () => {
  describe("getThumbnailUrl", () => {
    it("should return null for null input", () => {
      // Arrange & Act
      const result = getThumbnailUrl(null);
      
      // Assert
      expect(result).toBeNull();
    });

    it("should return null for undefined input", () => {
      // Arrange & Act
      const result = getThumbnailUrl(undefined);
      
      // Assert
      expect(result).toBeNull();
    });

    it("should return null for empty string", () => {
      // Arrange & Act
      const result = getThumbnailUrl("");
      
      // Assert
      expect(result).toBeNull();
    });

    it("should return original URL for valid input", () => {
      // Arrange
      const url = "https://storage.googleapis.com/bucket/image.jpg";
      
      // Act
      const result = getThumbnailUrl(url);
      
      // Assert
      expect(result).toBe(url);
    });

    it("should return original URL for any valid URL string", () => {
      // Arrange
      const url = "https://example.com/path/to/image.png";
      
      // Act
      const result = getThumbnailUrl(url);
      
      // Assert
      expect(result).toBe(url);
    });
  });
});
