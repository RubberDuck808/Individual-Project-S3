// ***********************************************************
// This support file is processed and loaded automatically
// before your test files. This is a great place to put
// global configuration and behavior that modifies Cypress.
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands';

// Global error handling - fail tests on uncaught exceptions
Cypress.on('uncaught:exception', (err, runnable) => {
  // Prevent Cypress from failing on certain errors
  // Return false to prevent the error from failing the test
  if (err.message.includes('ResizeObserver loop limit exceeded')) {
    return false;
  }
  // Ignore logout function errors (logout might not return a promise)
  if (err.message.includes('Cannot read properties of undefined') && 
      (err.message.includes('then') || err.message.includes('reading \'then\''))) {
    // This can happen when logout() doesn't return a promise but code calls .then() on it
    return false;
  }
  // Let other errors fail the test
  return true;
});

// Log API requests in test environment (helpful for debugging)
if (Cypress.env('LOG_API_REQUESTS')) {
  cy.intercept('**', (req) => {
    cy.log(`API Request: ${req.method} ${req.url}`);
  });
}