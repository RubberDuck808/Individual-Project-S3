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

    it('should fetch all users with custom pagination', async () => {
      // Arrange
      const mockUsers = [{ id: 1, username: 'user1' }];
      auth.authFetch.mockResolvedValue(mockUsers);

      // Act
      await adminApi.getAllUsers(1, 10);

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/users?page=1&size=10');
    });
  });

  describe('getUserById', () => {
    it('should fetch user by ID', async () => {
      // Arrange
      const mockUser = { id: 1, username: 'user1' };
      auth.authFetch.mockResolvedValue(mockUser);

      // Act
      const result = await adminApi.getUserById(1);

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/users/1');
      expect(result).toEqual(mockUser);
    });
  });

  describe('updateUserRole', () => {
    it('should update user role', async () => {
      // Arrange
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      // Act
      const result = await adminApi.updateUserRole(1, 'ADMIN');

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/users/1/role?roleName=ADMIN', {
        method: 'PUT',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deactivateUser', () => {
    it('should deactivate user', async () => {
      // Arrange
      auth.authFetch.mockResolvedValue(null);

      // Act
      await adminApi.deactivateUser(1);

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/users/1', {
        method: 'DELETE',
      });
    });
  });

  describe('getAllDevices', () => {
    it('should fetch all devices with default pagination', async () => {
      const mockDevices = [{ id: 1, deviceId: 'ESP32-ABC123' }];
      auth.authFetch.mockResolvedValue(mockDevices);

      const result = await adminApi.getAllDevices();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/devices?page=0&size=20');
      expect(result).toEqual(mockDevices);
    });
  });

  describe('getDeviceById', () => {
    it('should fetch device by ID', async () => {
      const mockDevice = { id: 1, deviceId: 'ESP32-ABC123' };
      auth.authFetch.mockResolvedValue(mockDevice);

      const result = await adminApi.getDeviceById(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/devices/1');
      expect(result).toEqual(mockDevice);
    });
  });

  describe('getDeviceByDeviceId', () => {
    it('should fetch device by deviceId', async () => {
      const mockDevice = { id: 1, deviceId: 'ESP32-ABC123' };
      auth.authFetch.mockResolvedValue(mockDevice);

      const result = await adminApi.getDeviceByDeviceId('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/devices/device-id/ESP32-ABC123');
      expect(result).toEqual(mockDevice);
    });
  });

  describe('activateDevice', () => {
    it('should activate device', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await adminApi.activateDevice(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/devices/1/activate', {
        method: 'PUT',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('deactivateDevice', () => {
    it('should deactivate device', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await adminApi.deactivateDevice(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/devices/1/deactivate', {
        method: 'PUT',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('updateDeviceDescription', () => {
    it('should update device description', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await adminApi.updateDeviceDescription(1, 'New description');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/devices/1/description?description=New%20description', {
        method: 'PUT',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getAllAvatars', () => {
    it('should fetch all avatars', async () => {
      const mockAvatars = [{ id: 1, name: 'avatar1' }];
      auth.authFetch.mockResolvedValue(mockAvatars);

      const result = await adminApi.getAllAvatars();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/avatars');
      expect(result).toEqual(mockAvatars);
    });
  });

  describe('getAvatarById', () => {
    it('should fetch avatar by ID', async () => {
      const mockAvatar = { id: 1, name: 'avatar1' };
      auth.authFetch.mockResolvedValue(mockAvatar);

      const result = await adminApi.getAvatarById(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/avatars/1');
      expect(result).toEqual(mockAvatar);
    });
  });

  describe('createAvatar', () => {
    it('should create new avatar', async () => {
      const mockAvatar = { id: 1, name: 'avatar1', imagePath: '/path/to/avatar.png' };
      auth.authFetch.mockResolvedValue(mockAvatar);

      const result = await adminApi.createAvatar('avatar1', '/path/to/avatar.png');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/avatars', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'avatar1', imagePath: '/path/to/avatar.png' }),
      });
      expect(result).toEqual(mockAvatar);
    });
  });

  describe('updateAvatar', () => {
    it('should update avatar', async () => {
      const mockAvatar = { id: 1, name: 'avatar1', imagePath: '/path/to/avatar.png', active: true };
      auth.authFetch.mockResolvedValue(mockAvatar);

      const result = await adminApi.updateAvatar(1, 'avatar1', '/path/to/avatar.png', true);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/avatars/1', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'avatar1', imagePath: '/path/to/avatar.png', active: true }),
      });
      expect(result).toEqual(mockAvatar);
    });
  });

  describe('deleteAvatar', () => {
    it('should delete avatar', async () => {
      auth.authFetch.mockResolvedValue(null);

      await adminApi.deleteAvatar(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/avatars/1', {
        method: 'DELETE',
      });
    });
  });

  describe('deactivateAvatar', () => {
    it('should deactivate avatar', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await adminApi.deactivateAvatar(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/avatars/1/deactivate', {
        method: 'PUT',
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('getAllBackgrounds', () => {
    it('should fetch all backgrounds', async () => {
      const mockBackgrounds = [{ id: 1, name: 'bg1' }];
      auth.authFetch.mockResolvedValue(mockBackgrounds);

      const result = await adminApi.getAllBackgrounds();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/backgrounds');
      expect(result).toEqual(mockBackgrounds);
    });
  });

  describe('getBackgroundById', () => {
    it('should fetch background by ID', async () => {
      const mockBackground = { id: 1, name: 'bg1' };
      auth.authFetch.mockResolvedValue(mockBackground);

      const result = await adminApi.getBackgroundById(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/backgrounds/1');
      expect(result).toEqual(mockBackground);
    });
  });

  describe('createBackground', () => {
    it('should create new background', async () => {
      const mockBackground = { id: 1, name: 'bg1', imagePath: '/path/to/bg.png' };
      auth.authFetch.mockResolvedValue(mockBackground);

      const result = await adminApi.createBackground('bg1', '/path/to/bg.png');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/backgrounds', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'bg1', imagePath: '/path/to/bg.png' }),
      });
      expect(result).toEqual(mockBackground);
    });
  });

  describe('updateBackground', () => {
    it('should update background', async () => {
      const mockBackground = { id: 1, name: 'bg1', imagePath: '/path/to/bg.png', active: true };
      auth.authFetch.mockResolvedValue(mockBackground);

      const result = await adminApi.updateBackground(1, 'bg1', '/path/to/bg.png', true);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/backgrounds/1', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: 'bg1', imagePath: '/path/to/bg.png', active: true }),
      });
      expect(result).toEqual(mockBackground);
    });
  });

  describe('deleteBackground', () => {
    it('should delete background', async () => {
      auth.authFetch.mockResolvedValue(null);

      await adminApi.deleteBackground(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/backgrounds/1', {
        method: 'DELETE',
      });
    });
  });

  describe('deactivateBackground', () => {
    it('should deactivate background', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await adminApi.deactivateBackground(1);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/admin/assets/backgrounds/1/deactivate', {
        method: 'PUT',
      });
      expect(result).toEqual(mockResponse);
    });
  });
});
