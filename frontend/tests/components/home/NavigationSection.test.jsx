import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import NavigationSection from '../../../src/components/home/NavigationSection';

describe('NavigationSection', () => {
  it('should render navigation section', () => {
    render(<NavigationSection />);
    
    expect(screen.getByText(/routes that/i)).toBeInTheDocument();
    expect(screen.getByText(/talk back/i)).toBeInTheDocument();
  });

  it('should render live navigation badge', () => {
    render(<NavigationSection />);
    
    expect(screen.getByText(/live navigation/i)).toBeInTheDocument();
  });

  it('should render hazard cards', () => {
    render(<NavigationSection />);
    
    expect(screen.getByText(/pothole/i)).toBeInTheDocument();
    expect(screen.getByText(/accident/i)).toBeInTheDocument();
    expect(screen.getByText(/debris/i)).toBeInTheDocument();
  });

  it('should render report hazard button', () => {
    render(<NavigationSection />);
    
    expect(screen.getByText(/report hazard/i)).toBeInTheDocument();
  });
});
