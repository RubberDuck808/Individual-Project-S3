import { describe, it, expect } from 'vitest';
import { renderHazardIcon } from '../../src/utils/renderHazardIcon';

describe('renderHazardIcon', () => {
  it('should render image when iconUrl is provided', () => {
    // Arrange
    const hazard = {
      category: {
        name: 'Pothole',
        iconUrl: '/icons/pothole.png',
      },
    };
    
    // Act
    const el = renderHazardIcon(hazard);

    // Assert
    const img = el.querySelector('img');
    expect(img).toBeTruthy();
    expect(img.src).toContain('/icons/pothole.png');
    expect(img.alt).toBe('Pothole');
  });
});
