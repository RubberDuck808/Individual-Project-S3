import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as adminApi from '../../src/api/adminApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('adminApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAdminStatistics', () => {
    it('should fetch admin statistics', async () => {
      // Arrange
      const mockStats = { totalUsers: 100, totalDevices: 50 };
      auth.authFetch.mockResolvedValue(mockStats);

      // Act
      const result = await adminApi.getAdminStatistics();

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/statistics');
      expect(result).toEqual(mockStats);
    });
  });

  describe('getAllUsers', () => {
    it('should fetch all users with default pagination', async () => {
      // Arrange
      const mockUsers = [{ id: 1, username: 'user1' }];
      auth.authFetch.mockResolvedValue(mockUsers);

      // Act
      const result = await adminApi.getAllUsers();

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/users?page=0&size=20');
      expect(result).toEqual(mockUsers);
    });

  });
});
