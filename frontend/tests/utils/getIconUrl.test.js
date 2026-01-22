import { describe, it, expect } from 'vitest';
import { getIconUrl } from '../../src/utils/getIconUrl';

describe('getIconUrl', () => {
  it('should return icon URL for valid filename', () => {
    // Arrange & Act
    const result = getIconUrl('test-icon.svg');
    
    // Assert
    expect(result).toBe('/icons/test-icon.svg');
  });
});
