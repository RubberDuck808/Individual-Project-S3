import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import TelemetrySection from '../../../src/components/home/TelemetrySection';

describe('TelemetrySection', () => {
  it('should render telemetry section', () => {
    render(<TelemetrySection />);
    
    expect(screen.getByText(/your car/i)).toBeInTheDocument();
    expect(screen.getByText(/unlocked/i)).toBeInTheDocument();
  });

  it('should render telemetry metrics', () => {
    render(<TelemetrySection />);
    
    expect(screen.getByText(/velocity/i)).toBeInTheDocument();
    expect(screen.getByText(/fuel level/i)).toBeInTheDocument();
    expect(screen.getByText(/oil temp/i)).toBeInTheDocument();
  });

  it('should render system diagnostics', () => {
    render(<TelemetrySection />);
    
    expect(screen.getByText(/system diagnostics/i)).toBeInTheDocument();
    expect(screen.getByText(/all systems/i)).toBeInTheDocument();
  });
});
