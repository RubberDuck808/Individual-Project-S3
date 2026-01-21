import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter, MemoryRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from '../../src/components/ProtectedRoute';
import { fetchCurrentUser } from '../../src/api/userApi';

// Mock the API
vi.mock('../../src/api/userApi', () => ({
  fetchCurrentUser: vi.fn(),
}));

// Mock auth module used by userApi
vi.mock('../../src/api/auth', () => ({
  authFetch: vi.fn(),
}));

const TestComponent = () => React.createElement('div', null, 'Protected Content');
const PublicComponent = () => React.createElement('div', null, 'Public Content');

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should show loading spinner while fetching user', async () => {
    // Arrange
    fetchCurrentUser.mockImplementation(() => new Promise(() => {})); // Never resolves

    // Act
    render(
      <MemoryRouter initialEntries={['/test']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/test" element={<TestComponent />} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    // Assert - Wait for component to mount and start loading
    await waitFor(() => {
      expect(fetchCurrentUser).toHaveBeenCalled();
    }, { timeout: 1000 });

    // Loading state should be shown while fetching
    expect(screen.getByText(/Loading/i)).toBeInTheDocument();
  });

  it('should render protected content when user is authenticated', async () => {
    // Arrange
    const mockUser = { id: 1, username: 'testuser', roleName: 'USER' };
    fetchCurrentUser.mockResolvedValue(mockUser);

    // Act
    render(
      <MemoryRouter initialEntries={['/test']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/test" element={<TestComponent />} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(fetchCurrentUser).toHaveBeenCalled();
    });

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should redirect to login when user is not authenticated', async () => {
    // Arrange
    fetchCurrentUser.mockRejectedValue(new Error('Not authenticated'));

    // Act
    render(
      <MemoryRouter initialEntries={['/test']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/test" element={<TestComponent />} />
          </Route>
          <Route path="/login" element={<PublicComponent />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(fetchCurrentUser).toHaveBeenCalled();
    });

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Public Content')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should allow access when user role matches required role', async () => {
    // Arrange
    const mockAdmin = { id: 1, username: 'admin', roleName: 'ADMIN' };
    fetchCurrentUser.mockResolvedValue(mockAdmin);

    // Act
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<ProtectedRoute requireRole="ADMIN" />}>
            <Route path="/admin" element={<TestComponent />} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(fetchCurrentUser).toHaveBeenCalled();
    });

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should redirect when user role does not match required role', async () => {
    // Arrange
    const mockUser = { id: 1, username: 'user', roleName: 'USER' };
    fetchCurrentUser.mockResolvedValue(mockUser);

    // Act
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<ProtectedRoute requireRole="ADMIN" />}>
            <Route path="/admin" element={<TestComponent />} />
          </Route>
          <Route path="/" element={<PublicComponent />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(fetchCurrentUser).toHaveBeenCalled();
    });

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Public Content')).toBeInTheDocument();
    }, { timeout: 3000 });
  });

  it('should handle role comparison case-insensitively', async () => {
    // Arrange
    const mockAdmin = { id: 1, username: 'admin', roleName: 'admin' };
    fetchCurrentUser.mockResolvedValue(mockAdmin);

    // Act
    render(
      <MemoryRouter initialEntries={['/admin']}>
        <Routes>
          <Route element={<ProtectedRoute requireRole="ADMIN" />}>
            <Route path="/admin" element={<TestComponent />} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(fetchCurrentUser).toHaveBeenCalled();
    });

    // Assert
    await waitFor(() => {
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    }, { timeout: 3000 });
  });
});
