import { describe, it, expect } from 'vitest';
import { getIconUrl } from '../../src/utils/getIconUrl';

describe('getIconUrl', () => {
  it('should return icon URL for valid filename', () => {
    const result = getIconUrl('test-icon.svg');
    expect(result).toBe('/icons/test-icon.svg');
  });

  it('should return null for empty string', () => {
    const result = getIconUrl('');
    expect(result).toBeNull();
  });

  it('should return null for null', () => {
    const result = getIconUrl(null);
    expect(result).toBeNull();
  });

  it('should return null for undefined', () => {
    const result = getIconUrl(undefined);
    expect(result).toBeNull();
  });

  it('should handle filenames with paths', () => {
    const result = getIconUrl('folder/icon.png');
    expect(result).toBe('/icons/folder/icon.png');
  });
});
