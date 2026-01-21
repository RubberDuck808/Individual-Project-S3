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
      const mockDevice = { deviceId: 'ESP32-ABC123', apiKey: 'key123' };
      auth.authFetch.mockResolvedValue(mockDevice);

      const result = await deviceApi.registerDevice('ESP32-ABC123', 'Test device');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/register', {
        method: 'POST',
        body: JSON.stringify({
          deviceId: 'ESP32-ABC123',
          description: 'Test device',
        }),
      });
      expect(result).toEqual(mockDevice);
    });

    it('should register device without description', async () => {
      const mockDevice = { deviceId: 'ESP32-ABC123', apiKey: 'key123' };
      auth.authFetch.mockResolvedValue(mockDevice);

      await deviceApi.registerDevice('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/register', {
        method: 'POST',
        body: JSON.stringify({
          deviceId: 'ESP32-ABC123',
          description: null,
        }),
      });
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(deviceApi.registerDevice()).rejects.toThrow('deviceId is required');
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

    it('should assign device with notes', async () => {
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      await deviceApi.assignDevice('ESP32-ABC123', 'My notes');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/ESP32-ABC123/assign?notes=My%20notes', {
        method: 'POST',
      });
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(deviceApi.assignDevice()).rejects.toThrow('deviceId is required');
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

    it('should unassign device with notes', async () => {
      auth.authFetch.mockResolvedValue(null);

      await deviceApi.unassignDevice('ESP32-ABC123', 'Unassign notes');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/devices/ESP32-ABC123/unassign?notes=Unassign%20notes', {
        method: 'DELETE',
      });
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(deviceApi.unassignDevice()).rejects.toThrow('deviceId is required');
    });
  });
});
