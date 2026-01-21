import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as hazardApi from '../../src/api/hazardApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

describe('hazardApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAllHazards', () => {
    it('should fetch all open hazards', async () => {
      const mockHazards = [{ id: 1, latitude: 40.7128, longitude: -74.0060 }];
      auth.authFetch.mockResolvedValue(mockHazards);

      const result = await hazardApi.getAllHazards();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/hazards/open');
      expect(result).toEqual(mockHazards);
    });
  });

  describe('getCategories', () => {
    it('should fetch hazard categories', async () => {
      const mockCategories = [{ id: 1, name: 'Pothole' }];
      auth.authFetch.mockResolvedValue(mockCategories);

      const result = await hazardApi.getCategories();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/hazard-categories');
      expect(result).toEqual(mockCategories);
    });
  });

  describe('getCategoriesCached', () => {
    it('should fetch and cache categories', async () => {
      const mockCategories = [{ id: 1, name: 'Pothole' }];
      auth.authFetch.mockResolvedValue(mockCategories);

      const result = await hazardApi.getCategoriesCached();

      expect(auth.authFetch).toHaveBeenCalledWith('/api/hazard-categories');
      expect(result).toEqual(mockCategories);
    });
  });

  describe('createHazard', () => {
    it('should create a new hazard', async () => {
      const mockHazard = { id: 1, latitude: 40.7128, longitude: -74.0060, categoryId: 1 };
      auth.authFetch.mockResolvedValue(mockHazard);

      const result = await hazardApi.createHazard({
        latitude: 40.7128,
        longitude: -74.0060,
        categoryId: 1,
      });

      expect(auth.authFetch).toHaveBeenCalledWith('/api/hazards', {
        method: 'POST',
        body: JSON.stringify({
          latitude: 40.7128,
          longitude: -74.0060,
          categoryId: 1,
        }),
      });
      expect(result).toEqual(mockHazard);
    });
  });

  describe('getHazardsByUsername', () => {
    it('should fetch hazards by username', async () => {
      const mockHazards = [{ id: 1 }];
      auth.authFetch.mockResolvedValue(mockHazards);

      const result = await hazardApi.getHazardsByUsername('testuser');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/hazards/by-user/testuser');
      expect(result).toEqual(mockHazards);
    });

    it('should encode username in URL', async () => {
      const mockHazards = [{ id: 1 }];
      auth.authFetch.mockResolvedValue(mockHazards);

      await hazardApi.getHazardsByUsername('test user');

      expect(auth.authFetch).toHaveBeenCalledWith('/api/hazards/by-user/test%20user');
    });
  });
});
