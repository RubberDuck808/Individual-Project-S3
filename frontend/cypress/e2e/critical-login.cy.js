/**
 * Critical E2E Test: User Login
 * Tests the complete login flow end-to-end with backend integration
 */

describe('Critical: User Login Flow', () => {
  beforeEach(() => {
    cy.clearAuth();
    cy.visit('/login');
  });

  it('should successfully login with valid credentials', () => {
    // Mock successful login
    cy.intercept('POST', '**/api/users/login', {
      statusCode: 200,
      body: {
        token: 'mock-jwt-token',
        user: {
          id: 1,
          username: 'testuser',
          email: 'testuser@example.com',
          roleName: 'USER'
        }
      }
    }).as('login');

    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'testuser',
        email: 'testuser@example.com',
        roleName: 'USER'
      }
    }).as('getUser');

    // Fill in login form
    cy.get('#login-email').type('testuser@example.com');
    cy.get('#login-password').type('testpassword123');
    cy.get('form').submit();

    // Wait for login API call
    cy.wait('@login');

    // Should redirect to map page for regular users
    cy.url({ timeout: 5000 }).should('include', '/map');

    // Verify token is stored
    cy.window().then((win) => {
      expect(win.localStorage.getItem('token')).to.equal('mock-jwt-token');
    });
  });

  it('should show error for invalid credentials', () => {
    // Mock failed login
    cy.intercept('POST', '**/api/users/login', {
      statusCode: 401,
      body: { error: 'Invalid credentials' }
    }).as('failedLogin');

    cy.get('#login-email').type('wrong@example.com');
    cy.get('#login-password').type('wrongpassword');
    cy.get('form').submit();

    cy.wait('@failedLogin');

    // Should show error message
    cy.contains(/wrong email or password.*try again/i, { timeout: 5000 }).should('be.visible');
    
    // Should remain on login page
    cy.url().should('include', '/login');
  });

  it('should validate form inputs', () => {
    // Empty form
    cy.get('form').submit();
    cy.contains(/enter your credentials.*driver/i, { timeout: 5000 }).should('be.visible');

    // Invalid email
    cy.get('#login-email').type('invalid-email');
    cy.get('#login-password').type('password');
    cy.get('form').submit();
    cy.contains(/email looks.*funky|that email looks a bit funky/i, { timeout: 5000 }).should('be.visible');
  });
});
