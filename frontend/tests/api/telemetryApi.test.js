import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as telemetryApi from '../../src/api/telemetryApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('telemetryApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getLiveTelemetry', () => {
    it('should fetch live telemetry', async () => {
      const mockTelemetry = { speed: 60, rpm: 2000 };
      auth.authFetch.mockResolvedValue(mockTelemetry);

      const result = await telemetryApi.getLiveTelemetry('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/live/ESP32-ABC123');
      expect(result).toEqual(mockTelemetry);
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(telemetryApi.getLiveTelemetry()).rejects.toThrow('deviceId is required');
    });
  });

  describe('getCarHealth', () => {
    it('should fetch car health data', async () => {
      const mockHealth = { fuelLevel: 75, oilTemp: 200 };
      auth.authFetch.mockResolvedValue(mockHealth);

      const result = await telemetryApi.getCarHealth('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/device/ESP32-ABC123/health');
      expect(result).toEqual(mockHealth);
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(telemetryApi.getCarHealth()).rejects.toThrow('deviceId is required');
    });
  });

  describe('getCarHealthHistory', () => {
    it('should fetch car health history with default limit', async () => {
      const mockHistory = [{ fuelLevel: 75 }];
      auth.authFetch.mockResolvedValue(mockHistory);

      const result = await telemetryApi.getCarHealthHistory('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/device/ESP32-ABC123/health/history?limit=50');
      expect(result).toEqual(mockHistory);
    });

    it('should fetch car health history with custom limit', async () => {
      const mockHistory = [{ fuelLevel: 75 }];
      auth.authFetch.mockResolvedValue(mockHistory);

      await telemetryApi.getCarHealthHistory('ESP32-ABC123', 100);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/device/ESP32-ABC123/health/history?limit=100');
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(telemetryApi.getCarHealthHistory()).rejects.toThrow('deviceId is required');
    });
  });

  describe('getTelemetryHistory', () => {
    it('should fetch telemetry history with default limit', async () => {
      const mockHistory = [{ speed: 60 }];
      auth.authFetch.mockResolvedValue(mockHistory);

      const result = await telemetryApi.getTelemetryHistory('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/history/ESP32-ABC123?limit=50');
      expect(result).toEqual(mockHistory);
    });

    it('should fetch telemetry history with custom limit', async () => {
      const mockHistory = [{ speed: 60 }];
      auth.authFetch.mockResolvedValue(mockHistory);

      await telemetryApi.getTelemetryHistory('ESP32-ABC123', 100);

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/history/ESP32-ABC123?limit=100');
    });

    it('should throw error when deviceId is missing', async () => {
      await expect(telemetryApi.getTelemetryHistory()).rejects.toThrow('deviceId is required');
    });
  });

  describe('getTelemetryHistoryRange', () => {
    it('should fetch telemetry history in range', async () => {
      const mockHistory = [{ speed: 60 }];
      auth.authFetch.mockResolvedValue(mockHistory);
      const start = new Date('2024-01-01');
      const end = new Date('2024-01-02');

      const result = await telemetryApi.getTelemetryHistoryRange('ESP32-ABC123', start, end);

      expect(auth.authFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/telemetry/history/ESP32-ABC123/range')
      );
      expect(result).toEqual(mockHistory);
    });

    it('should throw error when deviceId is missing', async () => {
      const start = new Date('2024-01-01');
      const end = new Date('2024-01-02');
      await expect(telemetryApi.getTelemetryHistoryRange(undefined, start, end)).rejects.toThrow('deviceId is required');
    });
  });
});
