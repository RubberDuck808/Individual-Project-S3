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
      // Arrange
      const mockResponse = {
        token: 'mock-jwt-token',
        user: { id: 1, username: 'testuser', email: 'test@example.com' }
      };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      // Act
      const result = await auth.login('test@example.com', 'password123');

      // Assert
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
      // Arrange
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ error: 'Invalid credentials' }),
        headers: { get: () => 'application/json' },
      });

      // Act & Assert
      await expect(auth.login('test@example.com', 'wrong')).rejects.toThrow();
    });
  });

  describe('register', () => {
    it('should successfully register a user', async () => {
      // Arrange
      const mockResponse = { id: 1, username: 'newuser', email: 'new@example.com' };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      // Act
      const result = await auth.register('newuser', 'new@example.com', 'password123', 'New User');

      // Assert
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
      // Arrange
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ error: 'Email already exists' }),
        headers: { get: () => 'application/json' },
      });

      // Act & Assert
      await expect(
        auth.register('newuser', 'existing@example.com', 'password123', 'New User')
      ).rejects.toThrow();
    });
  });

  describe('authFetch', () => {
    it('should include Authorization header with token', async () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');
      mockFetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: vi.fn(() => 'application/json'),
        },
        json: async () => ({ data: 'test' }),
      });

      // Act
      await auth.authFetch('/api/test');

      // Assert
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
      // Arrange
      localStorage.removeItem('token');

      // Act & Assert
      await expect(auth.authFetch('/api/test')).rejects.toThrow('Not authenticated');
    });

    it('should handle 204 No Content response', async () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: {
          get: vi.fn(() => null),
        },
      });

      // Act
      const result = await auth.authFetch('/api/test');
      
      // Assert
      expect(result).toBeNull();
    });
  });

  describe('fetchCurrentUser', () => {
    it('should fetch and store current user', async () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');
      const mockUser = { id: 1, username: 'testuser' };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: vi.fn(() => 'application/json'),
        },
        json: async () => mockUser,
      });

      // Act
      const result = await auth.fetchCurrentUser();

      // Assert
      expect(result).toEqual(mockUser);
      expect(JSON.parse(localStorage.getItem('user'))).toEqual(mockUser);
    });
  });

  describe('logout', () => {
    it('should clear token and user from localStorage', () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({ id: 1 }));

      // Act
      auth.logout();

      // Assert
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('getStoredUser', () => {
    it('should return parsed user from localStorage', () => {
      // Arrange
      const mockUser = { id: 1, username: 'testuser' };
      localStorage.setItem('user', JSON.stringify(mockUser));

      // Act
      const result = auth.getStoredUser();

      // Assert
      expect(result).toEqual(mockUser);
    });

    it('should return null when user is not stored', () => {
      // Arrange & Act
      const result = auth.getStoredUser();

      // Assert
      expect(result).toBeNull();
    });

    it('should return null when stored user is invalid JSON', () => {
      // Arrange
      localStorage.setItem('user', 'invalid-json');

      // Act
      const result = auth.getStoredUser();

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('isLoggedIn', () => {
    it('should return true when token exists', () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');

      // Act
      const result = auth.isLoggedIn();

      // Assert
      expect(result).toBe(true);
    });

    it('should return false when token does not exist', () => {
      // Arrange
      localStorage.removeItem('token');

      // Act
      const result = auth.isLoggedIn();

      // Assert
      expect(result).toBe(false);
    });
  });

  describe('getAuthHeader', () => {
    it('should return Authorization header with token', () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');

      // Act
      const result = auth.getAuthHeader();

      // Assert
      expect(result).toEqual({ Authorization: 'Bearer mock-token' });
    });

    it('should throw error when not authenticated', () => {
      // Arrange
      localStorage.removeItem('token');

      // Act & Assert
      expect(() => auth.getAuthHeader()).toThrow('Not authenticated');
    });
  });

  describe('getStoredUserId', () => {
    it('should return user id from stored user', () => {
      // Arrange
      localStorage.setItem('user', JSON.stringify({ id: 123 }));

      // Act
      const result = auth.getStoredUserId();

      // Assert
      expect(result).toBe(123);
    });

    it('should return userId from stored user', () => {
      // Arrange
      localStorage.setItem('user', JSON.stringify({ userId: 456 }));

      // Act
      const result = auth.getStoredUserId();

      // Assert
      expect(result).toBe(456);
    });

    it('should return null when user is not stored', () => {
      // Arrange & Act
      const result = auth.getStoredUserId();

      // Assert
      expect(result).toBeNull();
    });
  });
});
