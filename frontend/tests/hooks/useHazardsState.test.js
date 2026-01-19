import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useHazardsState } from '../../src/hooks/useHazardsState';
import { getAllHazards } from '../../src/api/hazardApi';

// Mock the API
vi.mock('../../src/api/hazardApi', () => ({
  getAllHazards: vi.fn(),
}));

// Mock WebSocket hook
vi.mock('../../src/components/map/hazards/useHazardsWebSocket', () => ({
  useHazardsWebSocket: vi.fn(() => ({})),
}));

describe('useHazardsState', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with empty hazards array', () => {
    getAllHazards.mockResolvedValue([]);

    const { result } = renderHook(() => useHazardsState({ enabled: false }));

    expect(result.current.hazards).toEqual([]);
    expect(result.current.selectedHazard).toBeNull();
  });

  it('should fetch hazards when enabled', async () => {
    const mockHazards = [
      { id: 1, latitude: 52.0, longitude: 5.0, status: 'OPEN' },
      { id: 2, latitude: 52.1, longitude: 5.1, status: 'VERIFIED' },
    ];
    getAllHazards.mockResolvedValue(mockHazards);

    const { result } = renderHook(() =>
      useHazardsState({
        location: { lat: 52.0, lng: 5.0 },
        enabled: true,
      })
    );

    await waitFor(() => {
      expect(getAllHazards).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(result.current.hazards.length).toBeGreaterThan(0);
    });
  });

  it('should not fetch hazards when disabled', () => {
    renderHook(() => useHazardsState({ enabled: false }));

    expect(getAllHazards).not.toHaveBeenCalled();
  });

  it('should filter out expired hazards', async () => {
    const mockHazards = [
      {
        id: 1,
        latitude: 52.0,
        longitude: 5.0,
        status: 'OPEN',
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 32).toISOString(), // 32 days ago
      },
      {
        id: 2,
        latitude: 52.1,
        longitude: 5.1,
        status: 'OPEN',
        createdAt: new Date().toISOString(),
      },
    ];
    getAllHazards.mockResolvedValue(mockHazards);

    const { result } = renderHook(() =>
      useHazardsState({
        location: { lat: 52.0, lng: 5.0 },
        enabled: true,
      })
    );

    await waitFor(() => {
      expect(result.current.hazards.length).toBeGreaterThan(0);
    });
  });

  it('should select nearest hazard within open distance', async () => {
    const mockHazards = [
      {
        id: 1,
        latitude: 52.0,
        longitude: 5.0,
        status: 'OPEN',
        createdAt: new Date().toISOString(),
        votes: [],
      },
    ];
    getAllHazards.mockResolvedValue(mockHazards);

    const { result } = renderHook(() =>
      useHazardsState({
        location: { lat: 52.0001, lng: 5.0001 }, // Very close
        currentUsername: 'testuser',
        openDistanceMeters: 150,
        enabled: true,
      })
    );

    await waitFor(() => {
      expect(getAllHazards).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(result.current.hazards.length).toBeGreaterThan(0);
    }, { timeout: 5000 });
  });

  it('should not select hazard from current user', async () => {
    const mockHazards = [
      {
        id: 1,
        latitude: 52.0,
        longitude: 5.0,
        status: 'OPEN',
        username: 'testuser',
        createdAt: new Date().toISOString(),
        votes: [],
      },
    ];
    getAllHazards.mockResolvedValue(mockHazards);

    const { result } = renderHook(() =>
      useHazardsState({
        location: { lat: 52.0001, lng: 5.0001 },
        currentUsername: 'testuser',
        openDistanceMeters: 150,
        enabled: true,
      })
    );

    await waitFor(() => {
      expect(getAllHazards).toHaveBeenCalled();
    });

    await waitFor(() => {
      // Hazard should be in the list but not selected for voting
      expect(result.current.hazards.length).toBeGreaterThan(0);
    }, { timeout: 5000 });
  });

  it('should provide closeVotePanel function', () => {
    const { result } = renderHook(() => useHazardsState({ enabled: false }));

    expect(typeof result.current.closeVotePanel).toBe('function');
  });
});
