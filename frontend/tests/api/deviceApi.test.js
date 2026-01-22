import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as deviceApi from '../../src/api/deviceApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('deviceApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('registerDevice', () => {
    it('should register a device', async () => {
      // Arrange
      const mockDevice = { deviceId: 'ESP32-ABC123', apiKey: 'key123' };
      auth.authFetch.mockResolvedValue(mockDevice);

      // Act
      const result = await deviceApi.registerDevice('ESP32-ABC123', 'Test device');

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/register', {
        method: 'POST',
        body: JSON.stringify({
          deviceId: 'ESP32-ABC123',
          description: 'Test device',
        }),
      });
      expect(result).toEqual(mockDevice);
    });

  });

  describe('getMyDevices', () => {
    it('should fetch user devices', async () => {
      const mockDevices = [{ deviceId: 'ESP32-ABC123' }];
      auth.authFetch.mockResolvedValue(mockDevices);

      const result = await deviceApi.getMyDevices();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/my-devices');
      expect(result).toEqual(mockDevices);
    });
  });

  describe('assignDevice', () => {
    it('should assign device without notes', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      const result = await deviceApi.assignDevice('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/ESP32-ABC123/assign', {
        method: 'POST',
      });
      expect(result).toEqual(mockResponse);
    });

  });

  describe('unassignDevice', () => {
    it('should unassign device without notes', async () => {
      auth.authFetch.mockResolvedValue(null);

      await deviceApi.unassignDevice('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/ESP32-ABC123/unassign', {
        method: 'DELETE',
      });
    });

  });
});
