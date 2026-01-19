/**
 * Critical E2E Test: Map Page Access
 * Tests that map page loads correctly when authenticated
 */
describe('Critical: Map Page Access', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  it('should redirect to login when not authenticated', () => {
    cy.visit('/map');
    cy.url().should('include', '/login');
  });

  it('should load map page when authenticated', () => {
    const mockUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roleName: 'USER'
    };

    // Mock API calls
    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: mockUser
    }).as('getUser');

    cy.intercept('GET', '**/api/hazards**', {
      statusCode: 200,
      body: []
    }).as('getHazards');

    cy.intercept('GET', '**/api/categories**', {
      statusCode: 200,
      body: []
    }).as('getCategories');

    cy.setAuth('mock-token', mockUser);
    cy.visit('/map');
    
    // Should be on map page
    cy.url().should('include', '/map');
    cy.wait('@getUser');
    
    // Page should load (map container may take time to initialize)
    cy.get('body').should('be.visible');
    
    // Navigation should be present
    cy.get('nav, [class*="nav"]').should('exist');
  });

  it('should display map elements when page loads', () => {
    const mockUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roleName: 'USER'
    };

    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: mockUser
    });

    cy.intercept('GET', '**/api/hazards**', {
      statusCode: 200,
      body: []
    });

    cy.intercept('GET', '**/api/categories**', {
      statusCode: 200,
      body: []
    });

    cy.setAuth('mock-token', mockUser);
    cy.visit('/map');
    
    // Wait for page to load
    cy.get('body', { timeout: 5000 }).should('be.visible');
    
    // Map container should exist (may be canvas or div)
    // Note: Mapbox map initialization may take time
    cy.get('[class*="map"], canvas, [id*="map"]', { timeout: 10000 }).should('exist');
  });

  it('should show loading state while fetching user data', () => {
    // Delay the API response to see loading state
    cy.intercept('GET', '**/api/users/me', {
      delay: 1000,
      statusCode: 200,
      body: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        roleName: 'USER'
      }
    }).as('getUser');

    cy.setAuth('mock-token', { id: 1, username: 'testuser' });
    cy.visit('/map');
    
    // Should show loading spinner
    cy.contains(/loading/i).should('exist');
    
    // After loading, should be on map page
    cy.wait('@getUser');
    cy.url().should('include', '/map');
  });
});
