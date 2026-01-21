import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import SocialSection from '../../../src/components/home/SocialSection';

describe('SocialSection', () => {
  it('should render social section', () => {
    render(<SocialSection />);
    
    // "climb the" appears in both heading and description, so check for leaderboard heading
    expect(screen.getByText(/leaderboard\./i)).toBeInTheDocument();
    expect(screen.getByText(/hall of fame/i)).toBeInTheDocument();
  });

  it('should render leaderboard', () => {
    render(<SocialSection />);
    
    expect(screen.getByText(/hall of fame/i)).toBeInTheDocument();
    expect(screen.getByText(/amina/i)).toBeInTheDocument();
    expect(screen.getByText(/leo/i)).toBeInTheDocument();
    expect(screen.getByText(/maya/i)).toBeInTheDocument();
  });

  it('should render join rankings button', () => {
    render(<SocialSection />);
    
    expect(screen.getByText(/join the rankings/i)).toBeInTheDocument();
  });
});
