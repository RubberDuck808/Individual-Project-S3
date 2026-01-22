import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../../src/pages/LoginPage';
import * as authApi from '../../src/api/auth';

// Mock the auth API
vi.mock('../../src/api/auth', () => ({
  login: vi.fn(),
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
    localStorage.clear();
  });

  it('should render login form', () => {
    // Arrange & Act
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    // Assert
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /initialize log in/i })).toBeInTheDocument();
  });


  it('should successfully login and redirect to map for regular users', async () => {
    // Arrange
    const mockUser = { id: 1, username: 'testuser', roleName: 'USER' };
    authApi.login.mockResolvedValue({ token: 'mock-token', user: mockUser });

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    // Act
    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/password/i);
    
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    
    const form = document.querySelector('form');
    fireEvent.submit(form);

    // Assert
    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith('test@example.com', 'password123');
      expect(mockNavigate).toHaveBeenCalledWith('/map');
    }, { timeout: 3000 });
  });

  it('should redirect to admin panel for admin users', async () => {
    // Arrange
    const mockAdmin = { id: 1, username: 'admin', roleName: 'ADMIN' };
    authApi.login.mockResolvedValue({ token: 'mock-token', user: mockAdmin });

    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>
    );

    // Act
    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/password/i);
    
    fireEvent.change(emailInput, { target: { value: 'admin@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    
    const form = document.querySelector('form');
    fireEvent.submit(form);

    // Assert
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/admin');
    }, { timeout: 3000 });
  });

});
