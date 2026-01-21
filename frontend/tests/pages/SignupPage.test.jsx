import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import SignupPage from '../../src/pages/SignupPage';
import * as authApi from '../../src/api/auth';

// Mock the auth API
vi.mock('../../src/api/auth', () => ({
  register: vi.fn(),
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

describe('SignupPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
  });

  it('should render signup form', () => {
    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/codename/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/real name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirm/i)).toBeInTheDocument();
  });

  it('should show error when fields are empty', async () => {
    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    const form = document.querySelector('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/fill in all the blanks/i)).toBeInTheDocument();
    });
  });

  it('should show error when email is invalid', async () => {
    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    const usernameInput = screen.getByLabelText(/codename/i);
    const nameInput = screen.getByLabelText(/real name/i);
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const confirmPasswordInput = screen.getByLabelText(/confirm/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(nameInput, { target: { value: 'Test User' } });
    fireEvent.change(emailInput, { target: { value: 'invalid-email' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

    const form = document.querySelector('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/that email looks/i)).toBeInTheDocument();
    });
  });

  it('should show error when passwords do not match', async () => {
    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    const usernameInput = screen.getByLabelText(/codename/i);
    const nameInput = screen.getByLabelText(/real name/i);
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const confirmPasswordInput = screen.getByLabelText(/confirm/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(nameInput, { target: { value: 'Test User' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'different' } });

    const form = document.querySelector('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/passwords aren't matching/i)).toBeInTheDocument();
    });
  });

  it('should successfully register and navigate to login', async () => {
    authApi.register.mockResolvedValue({ id: 1, username: 'testuser' });

    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    const usernameInput = screen.getByLabelText(/codename/i);
    const nameInput = screen.getByLabelText(/real name/i);
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const confirmPasswordInput = screen.getByLabelText(/confirm/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(nameInput, { target: { value: 'Test User' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

    const form = document.querySelector('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith('testuser', 'test@example.com', 'password123', 'Test User');
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('should show error message on registration failure', async () => {
    authApi.register.mockRejectedValue(new Error('Registration failed'));

    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    const usernameInput = screen.getByLabelText(/codename/i);
    const nameInput = screen.getByLabelText(/real name/i);
    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const confirmPasswordInput = screen.getByLabelText(/confirm/i);

    fireEvent.change(usernameInput, { target: { value: 'testuser' } });
    fireEvent.change(nameInput, { target: { value: 'Test User' } });
    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'password123' } });

    const form = document.querySelector('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/registration failed|the engine stalled/i)).toBeInTheDocument();
    });
  });

  it('should navigate to login when clicking back to login', () => {
    render(
      <BrowserRouter>
        <SignupPage />
      </BrowserRouter>
    );

    const backButton = screen.getByText(/back to login/i);
    fireEvent.click(backButton);

    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });
});
