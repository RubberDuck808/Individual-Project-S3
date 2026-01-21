import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as tripApi from '../../src/api/tripApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('tripApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('completeTrip', () => {
    it('should complete a trip', async () => {
      const payload = { deviceId: 'ESP32-ABC123', distance: 10.5 };
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await tripApi.completeTrip(payload);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/trips/complete', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      expect(result).toEqual(mockResponse);
    });
  });
});
