// Custom commands for e2e tests

/**
 * Login command - logs in a user via the UI
 * @param {string} email - User email
 * @param {string} password - User password
 */
Cypress.Commands.add('login', (email, password) => {
  cy.visit('/login');
  cy.get('#login-email').type(email);
  cy.get('#login-password').type(password);
  cy.get('form').submit();
});

/**
 * Logout command - clears localStorage to simulate logout
 */
Cypress.Commands.add('logout', () => {
  cy.window().then((win) => {
    win.localStorage.removeItem('token');
    win.localStorage.removeItem('user');
  });
});

/**
 * Set authenticated state - directly sets token and user in localStorage
 * Useful for testing protected routes without going through login flow
 */
Cypress.Commands.add('setAuth', (token, user) => {
  cy.window().then((win) => {
    win.localStorage.setItem('token', token);
    win.localStorage.setItem('user', JSON.stringify(user));
  });
});

/**
 * Clear auth state - clears authentication from localStorage
 */
Cypress.Commands.add('clearAuth', () => {
  cy.window().then((win) => {
    win.localStorage.removeItem('token');
    win.localStorage.removeItem('user');
  });
});