import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as statisticsApi from '../../src/api/statisticsApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('statisticsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getUserStats', () => {
    it('should fetch user statistics', async () => {
      const mockStats = { hazardsReported: 10, votesCast: 5 };
      auth.authFetch.mockResolvedValue(mockStats);

      const result = await statisticsApi.getUserStats('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/testuser/stats');
      expect(result).toEqual(mockStats);
    });

    it('should encode username in URL', async () => {
      auth.authFetch.mockResolvedValue({});

      await statisticsApi.getUserStats('test user');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/test%20user/stats');
    });
  });
});
