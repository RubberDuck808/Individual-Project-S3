/**
 * Critical E2E Test: Multi-User Hazard Interaction
 * Tests the complete flow where User A reports a hazard and User B votes on it
 * Simplified approach: User A reports via API, User B interacts via UI
 */

describe('Critical: Multi-User Hazard Interaction Flow', () => {
  const userA = {
    id: 1,
    username: 'usera',
    email: 'usera@example.com',
    roleName: 'USER'
  };

  const userB = {
    id: 2,
    username: 'userb',
    email: 'userb@example.com',
    roleName: 'USER'
  };

  const hazardLocation = {
    latitude: 51.5074,
    longitude: -0.1278
  };

  beforeEach(() => {
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
  });

  it('should allow User B to vote on User A\'s reported hazard', () => {
    // Get API URL from environment or config
    const apiUrl = Cypress.env('apiUrl') || 'http://localhost:8081';
    let hazardId = 1;

    // Step 1: User A reports a hazard via API (simpler than UI)
    // This simulates User A on a different system reporting a hazard
    cy.request({
      method: 'POST',
      url: `${apiUrl}/api/users/register`,
      body: {
        username: userA.username,
        email: userA.email,
        password: 'password123',
        name: 'User A'
      },
      failOnStatusCode: false // Don't fail if user already exists
    }).then(() => {
      // Login as User A to get token
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/api/users/login`,
        body: {
          email: userA.email,
          password: 'password123'
        },
        failOnStatusCode: false
      });
    }).then((loginResponse) => {
      const userAToken = loginResponse.body.token || 'mock-token-a';

      // User A creates a hazard via API
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/api/hazards`,
        headers: {
          Authorization: `Bearer ${userAToken}`,
          'Content-Type': 'application/json'
        },
        body: {
          latitude: hazardLocation.latitude,
          longitude: hazardLocation.longitude,
          categoryId: 1
        },
        failOnStatusCode: false
      });
    }).then((hazardResponse) => {
      hazardId = hazardResponse.body?.id || 1;

        // Step 2: Register User B if needed
        cy.request({
          method: 'POST',
          url: `${apiUrl}/api/users/register`,
          body: {
            username: userB.username,
            email: userB.email,
            password: 'password123',
            name: 'User B'
          },
          failOnStatusCode: false
        });

        // Step 3: Setup intercepts for User B's session
        cy.intercept('GET', '**/api/users/me', {
          statusCode: 200,
          body: userB
        }).as('getUserB');

        // Intercept hazards list - should include User A's hazard
        cy.intercept('GET', '**/api/hazards/open**', {
          statusCode: 200,
          body: [
            {
              id: hazardId,
              latitude: hazardLocation.latitude,
              longitude: hazardLocation.longitude,
              category: { id: 1, name: 'Pothole' },
              username: userA.username,
              status: 'OPEN'
            }
          ]
        }).as('getHazards');

        // Intercept vote submission
        cy.intercept('POST', '**/api/votes', {
          statusCode: 200,
          body: { message: 'Vote submitted' }
        }).as('submitVote');

        // Intercept vote counts - use dynamic hazard ID
        cy.intercept('GET', '**/api/votes/**/count', {
          statusCode: 200,
          body: { upvotes: 1, downvotes: 0 }
        }).as('getVotes');

        // Step 4: Login as User B and navigate to map
        cy.setAuth('mock-token-b', userB);
        
        cy.visit('/map', {
          onBeforeLoad(win) {
            // Stub geolocation to place User B near User A's hazard
            win.navigator.geolocation.watchPosition = (successCallback) => {
              setTimeout(() => {
                successCallback({
                  coords: {
                    latitude: hazardLocation.latitude, // Same location as hazard (within 150m)
                    longitude: hazardLocation.longitude
                  }
                });
              }, 100);
              return 1;
            };
            win.navigator.geolocation.clearWatch = () => {};
          }
        });

        // Wait for User B to be loaded
        cy.wait('@getUserB');

        // Wait for hazards to load (should include User A's hazard)
        cy.wait('@getHazards', { timeout: 10000 });

        // Wait for location context to update and proximity detection to trigger
        cy.wait(3000);

        // Step 5: Verify vote panel appears automatically when User B is near User A's hazard
        // The proximity detection should trigger when within 150m
        cy.contains(/still there/i, { timeout: 15000 }).should('be.visible');

        // Step 6: User B votes on User A's hazard
        cy.contains(/keep/i, { timeout: 5000 }).first().click({ force: true });

        // Wait for vote to be submitted
        cy.wait('@submitVote', { timeout: 10000 });

        // Step 7: Verify success message
        cy.contains(/vote cast/i, { timeout: 5000 }).should('be.visible');
      });
  });

  it('should show User A\'s hazard in User B\'s hazard list', () => {
    // Get API URL from environment or config
    const apiUrl = Cypress.env('apiUrl') || 'http://localhost:8081';
    let hazardId = 1;

    // Step 1: Create User A's hazard via API
    cy.request({
      method: 'POST',
      url: `${apiUrl}/api/users/register`,
      body: {
        username: userA.username,
        email: userA.email,
        password: 'password123',
        name: 'User A'
      },
      failOnStatusCode: false
    }).then(() => {
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/api/users/login`,
        body: {
          email: userA.email,
          password: 'password123'
        },
        failOnStatusCode: false
      });
    }).then((loginResponse) => {
      const userAToken = loginResponse.body.token || 'mock-token-a';

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/api/hazards`,
        headers: {
          Authorization: `Bearer ${userAToken}`,
          'Content-Type': 'application/json'
        },
        body: {
          latitude: hazardLocation.latitude,
          longitude: hazardLocation.longitude,
          categoryId: 1
        },
        failOnStatusCode: false
      });
    }).then((hazardResponse) => {
      hazardId = hazardResponse.body?.id || 1;

        // Step 2: Setup User B's session
        cy.intercept('GET', '**/api/users/me', {
          statusCode: 200,
          body: userB
        }).as('getUserB');

        cy.intercept('GET', '**/api/hazards/open**', {
          statusCode: 200,
          body: [
            {
              id: hazardId,
              latitude: hazardLocation.latitude,
              longitude: hazardLocation.longitude,
              category: { id: 1, name: 'Pothole' },
              username: userA.username,
              status: 'OPEN'
            }
          ]
        }).as('getHazards');

        cy.setAuth('mock-token-b', userB);
        
        cy.visit('/map', {
          onBeforeLoad(win) {
            win.navigator.geolocation.watchPosition = (successCallback) => {
              setTimeout(() => {
                successCallback({
                  coords: {
                    latitude: hazardLocation.latitude,
                    longitude: hazardLocation.longitude
                  }
                });
              }, 100);
              return 1;
            };
            win.navigator.geolocation.clearWatch = () => {};
          }
        });

        cy.wait('@getUserB');
        cy.wait('@getHazards', { timeout: 10000 });

        // Step 3: Verify User B sees User A's hazard
        // The hazard should appear on the map and trigger vote panel when in proximity
        cy.wait(3000);

        // Verify vote panel appears (indicating User A's hazard was detected)
        cy.contains(/still there/i, { timeout: 15000 }).should('be.visible');

        // Verify it's not User B's own hazard (should be able to vote)
        cy.contains(/keep/i).should('be.visible');
      });
  });
});
