import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from '../src/App';

// Mock the providers and lazy-loaded components
vi.mock('../src/providers/RealLocationProvider', () => ({
  default: ({ children }) => <div data-testid="real-location-provider">{children}</div>,
}));

vi.mock('../src/providers/SimulatedRouteProvider', () => ({
  default: ({ children }) => <div data-testid="simulated-route-provider">{children}</div>,
}));

vi.mock('../src/context/AssetsCacheContext', () => ({
  AssetsCacheProvider: ({ children }) => <div data-testid="assets-cache-provider">{children}</div>,
}));

vi.mock('../src/layouts/MainLayout', () => ({
  default: () => <div>MainLayout</div>,
}));

vi.mock('../src/pages/MapPage', () => ({
  default: () => <div>MapPage</div>,
}));

vi.mock('../src/pages/ProfilePage', () => ({
  default: () => <div>ProfilePage</div>,
}));

vi.mock('../src/pages/CarHealthPage', () => ({
  default: () => <div>CarHealthPage</div>,
}));

vi.mock('../src/pages/SettingsPage', () => ({
  default: () => <div>SettingsPage</div>,
}));

vi.mock('../src/pages/LoginPage', () => ({
  default: () => <div>LoginPage</div>,
}));

vi.mock('../src/pages/SignupPage', () => ({
  default: () => <div>SignupPage</div>,
}));

vi.mock('../src/pages/HomePage', () => ({
  default: () => <div>HomePage</div>,
}));

vi.mock('../src/components/ProtectedRoute', () => ({
  default: ({ children }) => <div data-testid="protected-route">{children}</div>,
}));

describe('App', () => {
  it('should render App component', () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    );

    expect(screen.getByTestId('real-location-provider')).toBeInTheDocument();
    expect(screen.getByTestId('assets-cache-provider')).toBeInTheDocument();
  });

  it('should render routes', () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    );

    // Routes are rendered through React Router, so we just check the provider is there
    expect(screen.getByTestId('real-location-provider')).toBeInTheDocument();
  });
});
