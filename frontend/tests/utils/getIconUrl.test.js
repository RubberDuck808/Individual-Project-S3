import { describe, it, expect } from 'vitest';
import { getIconUrl } from '../../src/utils/getIconUrl';

describe('getIconUrl', () => {
  it('should return icon URL for valid filename', () => {
    // Arrange & Act
    const result = getIconUrl('test-icon.svg');
    
    // Assert
    expect(result).toBe('/icons/test-icon.svg');
  });

  it('should return null for empty string', () => {
    // Arrange & Act
    const result = getIconUrl('');
    
    // Assert
    expect(result).toBeNull();
  });

  it('should return null for null', () => {
    // Arrange & Act
    const result = getIconUrl(null);
    
    // Assert
    expect(result).toBeNull();
  });

  it('should return null for undefined', () => {
    // Arrange & Act
    const result = getIconUrl(undefined);
    
    // Assert
    expect(result).toBeNull();
  });

  it('should handle filenames with paths', () => {
    // Arrange & Act
    const result = getIconUrl('folder/icon.png');
    
    // Assert
    expect(result).toBe('/icons/folder/icon.png');
  });
});
