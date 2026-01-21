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
      const mockBackgrounds = [{ name: 'bg1' }, { name: 'bg2' }];
      auth.authFetch.mockResolvedValue(mockBackgrounds);

      const result = await backgroundApi.fetchBackgrounds();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/backgrounds');
      expect(result).toEqual(mockBackgrounds);
    });
  });

  describe('changeMyBackground', () => {
    it('should change user background', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await backgroundApi.changeMyBackground('new-background');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/me/background', {
        method: 'PUT',
        body: JSON.stringify({ backgroundName: 'new-background' }),
      });
      expect(result).toEqual(mockResponse);
    });
  });
});
