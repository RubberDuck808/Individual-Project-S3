/**
 * Critical E2E Test: Voting on Hazards
 * Tests the complete voting flow end-to-end
 */

describe('Critical: Voting on Hazards Flow', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    email: 'testuser@example.com',
    roleName: 'USER'
  };

  const mockHazard = {
    id: 1,
    latitude: 51.5074,
    longitude: -0.1278,
    category: { id: 1, name: 'Pothole' },
    username: 'otheruser'
  };

  beforeEach(() => {
    // Set up authenticated user
    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: mockUser
    }).as('getUser');

    cy.intercept('GET', '**/api/hazards/open**', {
      statusCode: 200,
      body: [mockHazard]
    }).as('getHazards');

    // Intercept WebSocket requests to prevent connection failures
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

    cy.intercept({
      method: 'GET',
      url: '**/ws/**',
    }, {
      statusCode: 503,
      body: 'Service temporarily unavailable'
    });

    cy.setAuth('mock-token', mockUser);
  });

  it('should successfully vote on a hazard', () => {
    // Mock vote submission
    cy.intercept('POST', '**/api/votes', {
      statusCode: 200,
      body: { message: 'Vote submitted' }
    }).as('submitVote');

    cy.intercept('GET', '**/api/votes/1/count', {
      statusCode: 200,
      body: { upvotes: 1, downvotes: 0 }
    }).as('getVotes');

    // Mock location near the hazard using watchPosition - stub BEFORE visiting
    cy.visit('/map', {
      onBeforeLoad(win) {
        win.navigator.geolocation.watchPosition = (successCallback) => {
          // Call success with a slight delay to allow context to initialize
          setTimeout(() => {
            successCallback({
              coords: {
                latitude: 51.5074, // Same as hazard (within 150m proximity)
                longitude: -0.1278
              }
            });
          }, 100);
          return 1;
        };
        win.navigator.geolocation.clearWatch = () => {};
      }
    });
    cy.wait('@getUser');
    
    // Wait for hazards to load
    cy.wait('@getHazards', { timeout: 10000 });

    // Wait for location context to update and proximity detection to trigger vote panel (within 150m)
    cy.wait(3000);

    // Vote panel should appear automatically when within proximity
    // Look for "Still there?" heading which is the vote panel title
    cy.contains(/still there/i, { timeout: 15000 }).should('be.visible');

    // Click the upvote button (shows "Keep" text)
    cy.contains(/keep/i, { timeout: 5000 }).first().click({ force: true });
    
    cy.wait('@submitVote', { timeout: 10000 });
    
    // Should show success message
    cy.contains(/vote cast/i, { timeout: 5000 }).should('be.visible');
  });

  it('should show vote counts', () => {
    cy.intercept('GET', '**/api/votes/1/count', {
      statusCode: 200,
      body: { upvotes: 5, downvotes: 2 }
    }).as('getVotes');

    // Mock location near hazard - stub BEFORE visiting
    cy.visit('/map', {
      onBeforeLoad(win) {
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
        win.navigator.geolocation.clearWatch = () => {};
      }
    });
    cy.wait('@getUser');
    cy.wait('@getHazards', { timeout: 10000 });

    // Wait for location context to update and proximity detection to trigger vote panel
    cy.wait(3000);

    // Vote panel should appear and load vote counts
    cy.contains(/still there/i, { timeout: 15000 }).should('be.visible');
    
    // Wait for vote counts API call
    cy.wait('@getVotes', { timeout: 10000 });

    // Verify vote counts are displayed (should show numbers in the buttons)
    cy.contains(/5|2/i, { timeout: 5000 }).should('be.visible');
  });

  it('should require authentication to vote', () => {
    cy.clearAuth();
    cy.visit('/map');

    // Should redirect to login
    cy.url().should('include', '/login');
  });

  it('should prevent voting on own hazards', () => {
    // Mock hazard created by current user
    cy.intercept('GET', '**/api/hazards/open**', {
      statusCode: 200,
      body: [{
        ...mockHazard,
        username: 'testuser' // Same as current user
      }]
    }).as('getOwnHazards');

    // Mock location - stub BEFORE visiting
    cy.visit('/map', {
      onBeforeLoad(win) {
        win.navigator.geolocation.watchPosition = (successCallback) => {
          successCallback({
            coords: {
              latitude: 51.5074,
              longitude: -0.1278
            }
          });
          return 1;
        };
        win.navigator.geolocation.clearWatch = () => {};
      }
    });
    cy.wait('@getUser');
    cy.wait('@getOwnHazards', { timeout: 10000 });

    // Wait a bit for proximity detection
    cy.wait(2000);

    // Vote panel should not appear for own hazards (they are filtered out)
    cy.contains(/still there|vote|keep|gone/i).should('not.exist');
  });
});
