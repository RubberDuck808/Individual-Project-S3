import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as voteApi from '../../src/api/voteApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('voteApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('submitVote', () => {
    it('should submit a vote', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await voteApi.submitVote(1, 'UP');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/votes', {
        method: 'POST',
        body: JSON.stringify({ hazardId: 1, voteType: 'UP' }),
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getVoteCounts', () => {
    it('should fetch vote counts for a hazard', async () => {
      const mockCounts = { up: 10, down: 2 };
      auth.authFetch.mockResolvedValue(mockCounts);

      const result = await voteApi.getVoteCounts(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/votes/1/count');
      expect(result).toEqual(mockCounts);
    });
  });

  describe('getTotalVotesCastForUser', () => {
    it('should fetch total votes cast by user', async () => {
      const mockCount = 42;
      auth.authFetch.mockResolvedValue(mockCount);

      const result = await voteApi.getTotalVotesCastForUser('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/votes/user/testuser/cast');
      expect(result).toEqual(mockCount);
    });

    it('should encode username in URL', async () => {
      auth.authFetch.mockResolvedValue(0);

      await voteApi.getTotalVotesCastForUser('test user');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/votes/user/test%20user/cast');
    });
  });
});
