import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import Footer from '../../../src/components/home/Footer';

describe('Footer', () => {
  it('should render footer heading', () => {
    // Arrange & Act
    render(<Footer />);
    
    // Assert
    expect(screen.getByText(/don't drive/i)).toBeInTheDocument();
    expect(screen.getByText(/alone/i)).toBeInTheDocument();
  });

  it('should render social media buttons', () => {
    // Arrange & Act
    render(<Footer />);
    
    // Assert
    expect(screen.getByText(/discord/i)).toBeInTheDocument();
    expect(screen.getByText(/instagram/i)).toBeInTheDocument();
    expect(screen.getByText(/twitter/i)).toBeInTheDocument();
  });

  it('should render footer links', () => {
    // Arrange & Act
    render(<Footer />);
    
    // Assert
    expect(screen.getByText(/dashboard/i)).toBeInTheDocument();
    expect(screen.getByText(/leaderboard/i)).toBeInTheDocument();
    // Telemetry and Privacy appear multiple times, so use getAllByText
    expect(screen.getAllByText(/telemetry/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText(/privacy/i).length).toBeGreaterThan(0);
  });

  it('should render copyright text', () => {
    // Arrange & Act
    render(<Footer />);
    
    // Assert
    expect(screen.getByText(/© 2026 tripwire platforms inc/i)).toBeInTheDocument();
  });

  it('should render system status', () => {
    // Arrange & Act
    render(<Footer />);
    
    // Assert
    expect(screen.getByText(/system status: 100% groovy/i)).toBeInTheDocument();
  });
});
