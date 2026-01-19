/**
 * Critical E2E Test: Login Page
 * Tests login page functionality and validation
 */
describe('Critical: Login Page', () => {
  beforeEach(() => {
    cy.clearAuth();
    cy.visit('/login');
  });

  it('should display login form', () => {
    cy.get('#login-email').should('be.visible');
    cy.get('#login-password').should('be.visible');
    cy.get('button[type="submit"]').should('be.visible');
    cy.contains(/welcome back|ready for your next trip/i).should('exist');
  });

  it('should show error when submitting empty form', () => {
    cy.get('form').submit();
    cy.contains(/enter your credentials.*driver/i, { timeout: 5000 }).should('be.visible');
  });

  it('should show error when email is invalid', () => {
    cy.get('#login-email').type('invalid-email');
    cy.get('#login-password').type('password123');
    cy.get('form').submit();
    cy.contains(/email looks.*funky|that email looks a bit funky/i, { timeout: 5000 }).should('be.visible');
  });

  it('should show error when password is missing', () => {
    cy.get('#login-email').type('test@example.com');
    cy.get('form').submit();
    cy.contains(/don't forget your password/i, { timeout: 5000 }).should('be.visible');
  });

  it('should navigate to signup page when clicking create account', () => {
    // Find the "Create Account" button - it's in a button element
    cy.get('button').contains(/create account/i).first().click({ force: true });
    cy.url({ timeout: 5000 }).should('include', '/signup');
  });

  it('should navigate to home page when clicking logo', () => {
    cy.get('button[aria-label*="Return to home"], a[href="/"]').first().click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');
  });

  it('should handle failed login attempt with backend integration', () => {
    // Intercept the login API call to simulate backend response
    cy.intercept('POST', '**/api/users/login', {
      statusCode: 401,
      body: { error: 'Invalid credentials' }
    }).as('failedLogin');

    cy.get('#login-email').type('wrong@example.com');
    cy.get('#login-password').type('wrongpassword');
    cy.get('form').submit();
    
    // Wait for API call
    cy.wait('@failedLogin');
    
    // Should show error message from backend
    cy.contains(/wrong email or password.*try again/i, { timeout: 5000 }).should('exist');
    
    // Should remain on login page
    cy.url().should('include', '/login');
  });

  it('should successfully login and redirect when credentials are valid', () => {
    // Mock successful login response
    cy.intercept('POST', '**/api/users/login', {
      statusCode: 200,
      body: {
        token: 'mock-jwt-token',
        user: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          roleName: 'USER'
        }
      }
    }).as('successfulLogin');

    // Mock the subsequent user fetch for protected routes
    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        roleName: 'USER'
      }
    }).as('getUser');

    cy.get('#login-email').type('test@example.com');
    cy.get('#login-password').type('password123');
    cy.get('form').submit();
    
    // Wait for login API call
    cy.wait('@successfulLogin');
    
    // Should redirect to map page (for regular users)
    cy.url({ timeout: 5000 }).should('include', '/map');
    
    // Verify token is stored
    cy.window().then((win) => {
      expect(win.localStorage.getItem('token')).to.equal('mock-jwt-token');
    });
  });

  it('should redirect admin users to admin panel after login', () => {
    // Mock admin login response
    cy.intercept('POST', '**/api/users/login', {
      statusCode: 200,
      body: {
        token: 'mock-admin-token',
        user: {
          id: 1,
          username: 'admin',
          email: 'admin@example.com',
          roleName: 'ADMIN'
        }
      }
    }).as('adminLogin');

    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        roleName: 'ADMIN'
      }
    }).as('getAdminUser');

    cy.get('#login-email').type('admin@example.com');
    cy.get('#login-password').type('adminpassword');
    cy.get('form').submit();
    
    cy.wait('@adminLogin');
    
    // Should redirect to admin panel for admins
    cy.url({ timeout: 5000 }).should('include', '/admin');
  });
});
