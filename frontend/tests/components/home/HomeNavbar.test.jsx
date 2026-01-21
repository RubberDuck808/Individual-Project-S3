import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import HomeNavbar from '../../../src/components/home/HomeNavbar';

describe('HomeNavbar', () => {
  it('should render Tripwire logo and brand name', () => {
    render(<HomeNavbar />);
    
    expect(screen.getByText(/tripwire/i)).toBeInTheDocument();
    // Check for the logo "T" by finding it within the logo container
    const logoContainer = screen.getByText(/tripwire/i).closest('a');
    expect(logoContainer).toBeInTheDocument();
    expect(logoContainer?.querySelector('span')?.textContent).toBe('T');
  });

  it('should render navigation links', () => {
    render(<HomeNavbar />);
    
    expect(screen.getByText(/navigation/i)).toBeInTheDocument();
    expect(screen.getByText(/social/i)).toBeInTheDocument();
    expect(screen.getByText(/telemetry/i)).toBeInTheDocument();
  });

  it('should render login and signup links', () => {
    render(<HomeNavbar />);
    
    expect(screen.getByText(/login/i)).toBeInTheDocument();
    expect(screen.getByText(/join now/i)).toBeInTheDocument();
  });

  it('should have correct href attributes', () => {
    render(<HomeNavbar />);
    
    const loginLink = screen.getByText(/login/i).closest('a');
    expect(loginLink).toHaveAttribute('href', '/login');
    
    const signupLink = screen.getByText(/join now/i).closest('a');
    expect(signupLink).toHaveAttribute('href', '/signup');
  });
});
