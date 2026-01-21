import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as userApi from '../../src/api/userApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('userApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('fetchCurrentUser', () => {
    it('should fetch current user', async () => {
      const mockUser = { id: 1, username: 'testuser' };
      auth.authFetch.mockResolvedValue(mockUser);

      const result = await userApi.fetchCurrentUser();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/me');
      expect(result).toEqual(mockUser);
    });
  });

  describe('fetchUserByUsername', () => {
    it('should fetch user by username', async () => {
      const mockUser = { id: 1, username: 'testuser' };
      auth.authFetch.mockResolvedValue(mockUser);

      const result = await userApi.fetchUserByUsername('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/testuser');
      expect(result).toEqual(mockUser);
    });

    it('should encode username in URL', async () => {
      const mockUser = { id: 1, username: 'test user' };
      auth.authFetch.mockResolvedValue(mockUser);

      await userApi.fetchUserByUsername('test user');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/test%20user');
    });
  });

  describe('updateCurrentUser', () => {
    it('should update current user', async () => {
      const updates = { name: 'New Name' };
      const mockUser = { id: 1, name: 'New Name' };
      auth.authFetch.mockResolvedValue(mockUser);

      const result = await userApi.updateCurrentUser(updates);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/me', {
        method: 'PUT',
        body: JSON.stringify(updates),
      });
      expect(result).toEqual(mockUser);
    });
  });

  describe('getStoredUser', () => {
    it('should return parsed user from localStorage', () => {
      const mockUser = { id: 1, username: 'testuser' };
      localStorage.setItem('user', JSON.stringify(mockUser));

      const result = userApi.getStoredUser();

      expect(result).toEqual(mockUser);
    });

    it('should return null when user is not stored', () => {
      const result = userApi.getStoredUser();
      expect(result).toBeNull();
    });

    it('should return null when stored user is invalid JSON', () => {
      localStorage.setItem('user', 'invalid-json');

      const result = userApi.getStoredUser();

      expect(result).toBeNull();
    });
  });
});
