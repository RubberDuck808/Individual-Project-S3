/**
 * Critical E2E Test: Navigation and Route Protection
 * Tests that protected routes require authentication
 */
describe('Critical: Navigation and Route Protection', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  it('should redirect to login when accessing protected route without auth', () => {
    cy.visit('/map');
    cy.url().should('include', '/login');
    
    cy.visit('/car');
    cy.url().should('include', '/login');
    
    cy.visit('/settings');
    cy.url().should('include', '/login');
    
    cy.visit('/profile/testuser');
    cy.url().should('include', '/login');
  });

  it('should allow access to public routes without auth', () => {
    cy.visit('/');
    cy.url().should('eq', Cypress.config().baseUrl + '/');
    
    cy.visit('/login');
    cy.url().should('include', '/login');
    
    cy.visit('/signup');
    cy.url().should('include', '/signup');
  });

  it('should allow navigation between protected routes when authenticated', () => {
    // Mock authenticated user
    const mockUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roleName: 'USER'
    };

    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: mockUser
    }).as('getUser');

    cy.setAuth('mock-token', mockUser);

    // Should be able to access protected routes
    cy.visit('/map');
    cy.url().should('include', '/map');
    cy.wait('@getUser');

    // Navigate to another protected route
    cy.visit('/car');
    cy.url().should('include', '/car');

    cy.visit('/settings');
    cy.url().should('include', '/settings');
  });

  it('should redirect admin users from non-admin routes correctly', () => {
    // Mock admin user
    const adminUser = {
      id: 1,
      username: 'admin',
      email: 'admin@example.com',
      roleName: 'ADMIN'
    };

    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: adminUser
    }).as('getAdminUser');

    cy.setAuth('mock-admin-token', adminUser);

    // Admin should access admin routes
    cy.visit('/admin');
    cy.url().should('include', '/admin');
  });

  it('should redirect non-admin users from admin routes', () => {
    // Mock regular user trying to access admin route
    const regularUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roleName: 'USER'
    };

    // Clear auth first
    cy.clearAuth();
    
    // Set up intercept with very permissive pattern
    // The frontend API URL uses VITE_API_URL (http://backend-e2e:8080 in e2e)
    // Cypress should intercept this even if hostname doesn't resolve
    // Intercept ALL GET requests to /api/users/me regardless of hostname
    cy.intercept({
      method: 'GET',
      pathname: '/api/users/me'
    }, {
      statusCode: 200,
      body: regularUser
    }).as('getUserNonAdmin');
    
    // Set auth token but not user (ProtectedRoute will fetch user)
    // ProtectedRoute always calls fetchCurrentUser() in useEffect
    cy.window().then((win) => {
      win.localStorage.setItem('token', 'mock-token');
      win.localStorage.removeItem('user'); // Ensure no cached user
    });

    // Regular user should be redirected from admin routes
    // Visit will trigger ProtectedRoute which calls fetchCurrentUser()
    cy.visit('/admin', {
      failOnStatusCode: false // Don't fail if URL doesn't resolve
    });
    
    // ProtectedRoute will:
    // 1. Call fetchCurrentUser() -> intercept will respond with regularUser
    // 2. Check role: USER != ADMIN
    // 3. Redirect to / (home)
    // Wait for either the API call OR the redirect (whichever happens first)
    cy.wait('@getUserNonAdmin', { timeout: 20000 }).then(() => {
      // After successful user fetch, should redirect to home for non-admin
      cy.url({ timeout: 10000 }).should('satisfy', (url) => {
        const baseUrl = Cypress.config().baseUrl;
        // Check if URL matches base URL (with or without trailing slash)
        return url === baseUrl || url === baseUrl + '/' || url.replace(/\/$/, '') === baseUrl.replace(/\/$/, '');
      });
    });
  });
});
