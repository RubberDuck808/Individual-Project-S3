/**
 * Critical E2E Test: User Signup
 * Tests the complete signup flow end-to-end
 */

describe('Critical: User Signup Flow', () => {
  beforeEach(() => {
    cy.clearAuth();
    cy.visit('/signup');
  });

  it('should successfully create a new user account', () => {
    // Intercept registration API call
    cy.intercept('POST', '**/api/users/register', {
      statusCode: 200,
      body: {
        message: 'User registered successfully'
      }
    }).as('registerUser');

    // Fill in signup form
    cy.get('#signup-username').type('newuser' + Date.now());
    cy.get('#signup-name').type('New User');
    cy.get('#signup-email').type(`test${Date.now()}@example.com`);
    cy.get('#signup-password').type('password123');
    cy.get('#signup-confirm-password').type('password123');

    // Submit form
    cy.get('form').submit();

    // Wait for API call
    cy.wait('@registerUser');

    // Should redirect to login page after successful registration
    cy.url({ timeout: 5000 }).should('include', '/login');
  });

  it('should show validation errors for invalid input', () => {
    // Try to submit empty form
    cy.get('form').submit();
    cy.contains(/fill in all the blanks.*scout/i, { timeout: 5000 }).should('be.visible');

    // Fill required fields and try invalid email
    cy.get('#signup-username').type('testuser');
    cy.get('#signup-name').type('Test User');
    cy.get('#signup-email').type('invalid-email');
    cy.get('#signup-password').type('pass');
    cy.get('#signup-confirm-password').type('pass');
    cy.get('form').submit();
    cy.contains(/email looks.*wobbly|that email looks a bit wobbly/i, { timeout: 5000 }).should('be.visible');

    // Try mismatched passwords
    cy.get('#signup-email').clear().type('test@example.com');
    cy.get('#signup-password').clear().type('password123');
    cy.get('#signup-confirm-password').clear().type('different');
    cy.get('form').submit();
    cy.contains(/passwords.*aren't matching|passwords aren't matching up/i, { timeout: 5000 }).should('be.visible');
  });

  it('should navigate to login page from signup', () => {
    // Find the "Back to Login" button - it's in a button element
    cy.get('button').contains(/back to login/i).first().click({ force: true });
    cy.url({ timeout: 5000 }).should('include', '/login');
  });
});
