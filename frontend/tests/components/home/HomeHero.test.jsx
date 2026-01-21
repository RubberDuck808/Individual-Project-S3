import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import HomeHero from '../../../src/components/home/HomeHero';

describe('HomeHero', () => {
  it('should render hero heading', () => {
    render(<HomeHero />);
    
    expect(screen.getByText(/drive/i)).toBeInTheDocument();
    expect(screen.getByText(/smarter/i)).toBeInTheDocument();
    expect(screen.getByText(/together/i)).toBeInTheDocument();
  });

  it('should render community powered badge', () => {
    render(<HomeHero />);
    
    expect(screen.getByText(/100% community powered/i)).toBeInTheDocument();
  });

  it('should render hero description', () => {
    render(<HomeHero />);
    
    expect(screen.getByText(/the ultimate co-pilot/i)).toBeInTheDocument();
  });

  it('should render CTA button', () => {
    render(<HomeHero />);
    
    const ctaButton = screen.getByText(/get initialized/i);
    expect(ctaButton).toBeInTheDocument();
    expect(ctaButton.closest('a')).toHaveAttribute('href', '/signup');
  });

  it('should render avatar group', () => {
    render(<HomeHero />);
    
    expect(screen.getByText(/join 2,400\+ scouts/i)).toBeInTheDocument();
  });
});
