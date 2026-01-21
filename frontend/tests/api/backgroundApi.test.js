import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as backgroundApi from '../../src/api/backgroundApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('backgroundApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('fetchBackgrounds', () => {
    it('should fetch backgrounds', async () => {
      // Arrange
      const mockBackgrounds = [{ name: 'bg1' }, { name: 'bg2' }];
      auth.authFetch.mockResolvedValue(mockBackgrounds);

      // Act
      const result = await backgroundApi.fetchBackgrounds();

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/backgrounds');
      expect(result).toEqual(mockBackgrounds);
    });
  });

  describe('changeMyBackground', () => {
    it('should change user background', async () => {
      // Arrange
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      // Act
      const result = await backgroundApi.changeMyBackground('new-background');

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/me/background', {
        method: 'PUT',
        body: JSON.stringify({ backgroundName: 'new-background' }),
      });
      expect(result).toEqual(mockResponse);
    });
  });
});
