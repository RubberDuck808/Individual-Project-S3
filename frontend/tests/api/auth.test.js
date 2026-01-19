import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as auth from '../../src/api/auth';

// Mock fetch
global.fetch = vi.fn();
const mockFetch = global.fetch;

describe('auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('login', () => {
    it('should successfully login and store token and user', async () => {
      const mockResponse = {
        token: 'mock-jwt-token',
        user: { id: 1, username: 'testuser', email: 'test@example.com' }
      };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await auth.login('test@example.com', 'password123');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/users/login'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
        })
      );

      expect(result).toEqual(mockResponse);
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
      expect(JSON.parse(localStorage.getItem('user'))).toEqual(mockResponse.user);
    });

    it('should throw error on login failure', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ error: 'Invalid credentials' }),
        headers: { get: () => 'application/json' },
      });

      await expect(auth.login('test@example.com', 'wrong')).rejects.toThrow();
    });
  });

  describe('register', () => {
    it('should successfully register a user', async () => {
      const mockResponse = { id: 1, username: 'newuser', email: 'new@example.com' };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await auth.register('newuser', 'new@example.com', 'password123', 'New User');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/users/register'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
        })
      );

      expect(result).toEqual(mockResponse);
    });

    it('should throw error on registration failure', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ error: 'Email already exists' }),
        headers: { get: () => 'application/json' },
      });

      await expect(
        auth.register('newuser', 'existing@example.com', 'password123', 'New User')
      ).rejects.toThrow();
    });
  });

  describe('authFetch', () => {
    it('should include Authorization header with token', async () => {
      localStorage.setItem('token', 'mock-token');
      mockFetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: vi.fn(() => 'application/json'),
        },
        json: async () => ({ data: 'test' }),
      });

      await auth.authFetch('/api/test');

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            Authorization: 'Bearer mock-token',
          }),
        })
      );
    });

    it('should throw error when not authenticated', async () => {
      localStorage.removeItem('token');

      await expect(auth.authFetch('/api/test')).rejects.toThrow('Not authenticated');
    });

    it('should handle 204 No Content response', async () => {
      localStorage.setItem('token', 'mock-token');
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: {
          get: vi.fn(() => null),
        },
      });

      const result = await auth.authFetch('/api/test');
      expect(result).toBeNull();
    });
  });

  describe('fetchCurrentUser', () => {
    it('should fetch and store current user', async () => {
      localStorage.setItem('token', 'mock-token');
      const mockUser = { id: 1, username: 'testuser' };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: vi.fn(() => 'application/json'),
        },
        json: async () => mockUser,
      });

      const result = await auth.fetchCurrentUser();

      expect(result).toEqual(mockUser);
      expect(JSON.parse(localStorage.getItem('user'))).toEqual(mockUser);
    });
  });

  describe('logout', () => {
    it('should clear token and user from localStorage', () => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({ id: 1 }));

      auth.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('getStoredUser', () => {
    it('should return parsed user from localStorage', () => {
      const mockUser = { id: 1, username: 'testuser' };
      localStorage.setItem('user', JSON.stringify(mockUser));

      expect(auth.getStoredUser()).toEqual(mockUser);
    });

    it('should return null when user is not stored', () => {
      expect(auth.getStoredUser()).toBeNull();
    });

    it('should return null when stored user is invalid JSON', () => {
      localStorage.setItem('user', 'invalid-json');

      expect(auth.getStoredUser()).toBeNull();
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('token', 'mock-token');
      expect(auth.isLoggedIn()).toBe(true);
    });

    it('should return false when token does not exist', () => {
      localStorage.removeItem('token');
      expect(auth.isLoggedIn()).toBe(false);
    });
  });

  describe('getAuthHeader', () => {
    it('should return Authorization header with token', () => {
      localStorage.setItem('token', 'mock-token');
      expect(auth.getAuthHeader()).toEqual({ Authorization: 'Bearer mock-token' });
    });

    it('should throw error when not authenticated', () => {
      localStorage.removeItem('token');
      expect(() => auth.getAuthHeader()).toThrow('Not authenticated');
    });
  });

  describe('getStoredUserId', () => {
    it('should return user id from stored user', () => {
      localStorage.setItem('user', JSON.stringify({ id: 123 }));
      expect(auth.getStoredUserId()).toBe(123);
    });

    it('should return userId from stored user', () => {
      localStorage.setItem('user', JSON.stringify({ userId: 456 }));
      expect(auth.getStoredUserId()).toBe(456);
    });

    it('should return null when user is not stored', () => {
      expect(auth.getStoredUserId()).toBeNull();
    });
  });
});
