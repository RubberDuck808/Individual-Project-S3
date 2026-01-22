import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as avatarApi from '../../src/api/avatarApi';
import * as auth from '../../src/api/auth';

// Mock auth module
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

// Mock fetch
global.fetch = vi.fn();
const mockFetch = global.fetch;

describe('avatarApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('fetchAvatars', () => {
    it('should fetch avatars', async () => {
      // Arrange
      const mockAvatars = [{ name: 'avatar1' }, { name: 'avatar2' }];
      auth.authFetch.mockResolvedValue(mockAvatars);

      // Act
      const result = await avatarApi.fetchAvatars();

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/avatars');
      expect(result).toEqual(mockAvatars);
    });
  });

  describe('changeMyAvatar', () => {
    it('should change user avatar', async () => {
      // Arrange
      const mockResponse = { success: true };
      auth.authFetch.mockResolvedValue(mockResponse);

      // Act
      const result = await avatarApi.changeMyAvatar('new-avatar');

      // Assert
      expect(auth.authFetch).toHaveBeenCalledWith('/api/users/me/avatar', {
        method: 'PUT',
        body: JSON.stringify({ avatarName: 'new-avatar' }),
      });
      expect(result).toEqual(mockResponse);
    });
  });

  describe('fetchAvatarBlobByPath', () => {
    it('should fetch avatar blob when authenticated', async () => {
      // Arrange
      localStorage.setItem('token', 'mock-token');
      const mockBlob = new Blob(['test'], { type: 'image/png' });
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        blob: async () => mockBlob,
      });

      // Act
      const result = await avatarApi.fetchAvatarBlobByPath('path/to/avatar.png');

      // Assert
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/storage/object?path=path%2Fto%2Favatar.png'),
        expect.objectContaining({
          method: 'GET',
          headers: { Authorization: 'Bearer mock-token' },
        })
      );
      expect(result).toBeInstanceOf(Blob);
    });

  });
});
