import { describe, it, expect } from 'vitest';
import { renderHazardIcon } from '../../src/utils/renderHazardIcon';

describe('renderHazardIcon', () => {
  it('should create a div element with correct classes and styles', () => {
    // Arrange
    const hazard = { category: { name: 'Pothole' } };
    
    // Act
    const el = renderHazardIcon(hazard);

    // Assert
    expect(el.tagName).toBe('DIV');
    expect(el.className).toContain('hazard-marker');
    expect(el.style.width).toBe('25px');
    expect(el.style.height).toBe('25px');
    expect(el.style.borderRadius).toBe('50%');
  });

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
    expect(img.style.width).toBe('25px');
    expect(img.style.height).toBe('25px');
  });

  it('should render warning emoji when iconUrl is not provided', () => {
    // Arrange
    const hazard = { category: { name: 'Pothole' } };
    
    // Act
    const el = renderHazardIcon(hazard);

    // Assert
    expect(el.textContent).toBe('⚠️');
    expect(el.style.fontSize).toBe('10px');
    expect(el.querySelector('img')).toBeNull();
  });

  it('should handle hazard without category', () => {
    // Arrange
    const hazard = {};
    
    // Act
    const el = renderHazardIcon(hazard);

    // Assert
    expect(el.textContent).toBe('⚠️');
  });

  it('should handle null hazard', () => {
    // Arrange & Act
    const el = renderHazardIcon(null);

    // Assert
    expect(el.textContent).toBe('⚠️');
  });

  it('should use default alt text when category name is missing', () => {
    // Arrange
    const hazard = {
      category: {
        iconUrl: '/icons/pothole.png',
      },
    };
    
    // Act
    const el = renderHazardIcon(hazard);

    // Assert
    const img = el.querySelector('img');
    expect(img.alt).toBe('Hazard');
  });
});
