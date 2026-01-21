import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as friendshipApi from '../../src/api/friendshipApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('friendshipApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('sendFriendRequest', () => {
    it('should send friend request', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await friendshipApi.sendFriendRequest('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/request', {
        method: 'POST',
        body: JSON.stringify({ username: 'testuser' }),
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('acceptFriendRequest', () => {
    it('should accept friend request', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await friendshipApi.acceptFriendRequest('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/accept/testuser', {
        method: 'POST',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('declineFriendRequest', () => {
    it('should decline friend request', async () => {
      auth.authFetch.mockResolvedValue(null);

      await friendshipApi.declineFriendRequest('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/decline/testuser', {
        method: 'DELETE',
      });
    });
  });

  describe('cancelFriendRequest', () => {
    it('should cancel friend request', async () => {
      auth.authFetch.mockResolvedValue(null);

      await friendshipApi.cancelFriendRequest('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/cancel/testuser', {
        method: 'DELETE',
      });
    });
  });

  describe('unfriend', () => {
    it('should unfriend a user', async () => {
      auth.authFetch.mockResolvedValue(null);

      await friendshipApi.unfriend('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/unfriend/testuser', {
        method: 'DELETE',
      });
    });
  });

  describe('getIncomingRequests', () => {
    it('should fetch incoming friend requests', async () => {
      const mockRequests = [{ id: 1, username: 'user1' }];
      auth.authFetch.mockResolvedValue(mockRequests);

      const result = await friendshipApi.getIncomingRequests();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/requests/incoming');
      expect(result).toEqual(mockRequests);
    });
  });

  describe('getOutgoingRequests', () => {
    it('should fetch outgoing friend requests', async () => {
      const mockRequests = [{ id: 1, username: 'user2' }];
      auth.authFetch.mockResolvedValue(mockRequests);

      const result = await friendshipApi.getOutgoingRequests();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/requests/outgoing');
      expect(result).toEqual(mockRequests);
    });
  });

  describe('getFriends', () => {
    it('should fetch friends list', async () => {
      const mockFriends = [{ id: 1, username: 'friend1' }];
      auth.authFetch.mockResolvedValue(mockFriends);

      const result = await friendshipApi.getFriends();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships');
      expect(result).toEqual(mockFriends);
    });
  });

  describe('getFriendsOfUser', () => {
    it('should fetch friends of a user', async () => {
      const mockFriends = [{ id: 1, username: 'friend1' }];
      auth.authFetch.mockResolvedValue(mockFriends);

      const result = await friendshipApi.getFriendsOfUser('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/friendships/user/testuser');
      expect(result).toEqual(mockFriends);
    });
  });
});
