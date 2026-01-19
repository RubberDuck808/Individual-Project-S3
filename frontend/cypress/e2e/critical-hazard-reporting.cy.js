/**
 * Critical E2E Test: Hazard Reporting
 * Tests the complete hazard reporting flow end-to-end
 */

describe('Critical: Hazard Reporting Flow', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    email: 'testuser@example.com',
    roleName: 'USER'
  };

  beforeEach(() => {
    // Set up authenticated user
    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: mockUser
    }).as('getUser');

    cy.intercept('GET', '**/api/hazard-categories**', {
      statusCode: 200,
      body: [
        { id: 1, name: 'Pothole', iconUrl: null },
        { id: 2, name: 'Debris', iconUrl: null },
        { id: 3, name: 'Accident', iconUrl: null }
      ]
    }).as('getCategories');

    // Intercept WebSocket info requests to prevent connection failures
    // SockJS tries to connect to http://backend-e2e:8080/ws/info which doesn't resolve from browser
    // Intercept these to prevent hanging and delays
    cy.intercept({
      method: 'GET',
      url: '**/ws/info**',
    }, {
      statusCode: 200,
      body: '{"entropy":1234567890,"origins":["*:*"],"cookie_needed":false,"websocket":true}',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Intercept any other WebSocket connection attempts
    cy.intercept({
      method: 'GET',
      url: '**/ws/**',
    }, {
      statusCode: 503,
      body: 'Service temporarily unavailable'
    });

    cy.setAuth('mock-token', mockUser);
  });

  it('should successfully report a hazard when authenticated', () => {
    // Mock hazard creation
    cy.intercept('POST', '**/api/hazards', {
      statusCode: 201,
      body: {
        id: 1,
        latitude: 51.5074,
        longitude: -0.1278,
        categoryId: 1,
        category: { id: 1, name: 'Pothole' }
      }
    }).as('createHazard');

    // Mock location using watchPosition - stub BEFORE visiting the page
    cy.visit('/map', {
      onBeforeLoad(win) {
        // Stub watchPosition using plain JavaScript (cy.stub doesn't work in onBeforeLoad)
        const originalWatchPosition = win.navigator.geolocation.watchPosition.bind(win.navigator.geolocation);
        win.navigator.geolocation.watchPosition = (successCallback) => {
          setTimeout(() => {
            successCallback({
              coords: {
                latitude: 51.5074,
                longitude: -0.1278
              }
            });
          }, 100);
          return 1;
        };
        // Also stub clearWatch
        win.navigator.geolocation.clearWatch = () => {};
      }
    });

    // Wait for map to load
    cy.wait('@getUser');

    // Wait for location to be set and map to initialize (location context needs time to update)
    cy.wait(3000);

    // Click report button - it's a fixed position button with aria-label "Report Hazard"
    cy.get('button[aria-label="Report Hazard"]', { timeout: 10000 }).should('be.visible').click({ force: true });

    // Wait for hazard form panel to appear - look for the heading "Drop a Marker"
    cy.contains(/drop a.*marker/i, { timeout: 15000 }).should('be.visible');
    
    // Wait for categories to load
    cy.wait('@getCategories', { timeout: 15000 });

    // Wait a bit for categories to render and location status to update
    cy.wait(2000);

    // Verify location is ready (should show "Ready to report" badge)
    cy.contains(/ready to report|📍/i, { timeout: 5000 }).should('exist');

    // Select a category button - categories are displayed as buttons in a grid
    cy.get('button').contains(/pothole/i, { timeout: 10000 }).first().click({ force: true });

    // Wait for hazard to be created
    cy.wait('@createHazard', { timeout: 10000 });

    // Should show success message
    cy.contains(/hazard logged in the system/i, { timeout: 5000 }).should('be.visible');
  });

  it('should require authentication to report hazards', () => {
    cy.clearAuth();
    cy.visit('/map');

    // Should redirect to login
    cy.url().should('include', '/login');
  });

  it('should show error if location is not available', () => {
    // Mock location as unavailable - watchPosition doesn't call success callback
    cy.visit('/map', {
      onBeforeLoad(win) {
        // Stub watchPosition to not call success (simulates location not available)
        win.navigator.geolocation.watchPosition = () => {
          // Don't call callback - simulates location not available
          return 1;
        };
        win.navigator.geolocation.clearWatch = () => {};
        // Stub window.alert to capture the alert message
        win.alert = cy.stub().as('alert');
      }
    });
    cy.wait('@getUser');

    // Wait for map to initialize
    cy.wait(3000);

    // Try to report without location - click report button
    // When location is null, openHazardForm shows an alert and returns early
    cy.get('button[aria-label="Report Hazard"]', { timeout: 10000 }).should('be.visible').click({ force: true });

    // Should show alert message (the component shows alert when location is not available)
    cy.get('@alert').should('have.been.calledWith', 'Still determining your location…');
    
    // Form should not open when location is unavailable
    cy.contains(/drop a.*marker/i).should('not.exist');
  });
});
