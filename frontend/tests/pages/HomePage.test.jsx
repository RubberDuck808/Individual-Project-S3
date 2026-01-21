import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import HomePage from '../../src/pages/HomePage';

describe('HomePage', () => {
  it('should render HomePage with all sections', () => {
    render(
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    );

    // Check that main sections are rendered
    expect(screen.getByText(/smarter/i)).toBeInTheDocument();
    expect(screen.getByText(/together/i)).toBeInTheDocument();
  });

  it('should render HomeNavbar', () => {
    render(
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    );

    expect(screen.getAllByText(/tripwire/i)[0]).toBeInTheDocument();
  });

  it('should render Footer', () => {
    render(
      <BrowserRouter>
        <HomePage />
      </BrowserRouter>
    );

    expect(screen.getByText(/don't drive/i)).toBeInTheDocument();
    expect(screen.getByText(/alone/i)).toBeInTheDocument();
  });
});
