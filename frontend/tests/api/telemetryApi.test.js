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

  });

  describe('getCarHealth', () => {
    it('should fetch car health data', async () => {
      const mockHealth = { fuelLevel: 75, oilTemp: 200 };
      auth.authFetch.mockResolvedValue(mockHealth);

      const result = await telemetryApi.getCarHealth('ESP32-ABC123');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/telemetry/device/ESP32-ABC123/health');
      expect(result).toEqual(mockHealth);
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
  });
});
