/**
 * Critical E2E Test: Adding Friends
 * Tests the complete friend request flow end-to-end
 */

describe('Critical: Adding Friends Flow', () => {
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

    cy.setAuth('mock-token', mockUser);
  });

  it('should successfully send a friend request', () => {
    // Mock friend request API
    cy.intercept('POST', '**/api/friendships/request', {
      statusCode: 200,
      body: { message: 'Friend request sent' }
    }).as('sendRequest');

    cy.intercept('GET', '**/api/friendships/requests/outgoing', {
      statusCode: 200,
      body: [
        {
          id: 1,
          requesterUsername: 'testuser',
          addresseeUsername: 'frienduser',
          status: 'PENDING'
        }
      ]
    }).as('getOutgoing');

    cy.intercept('GET', '**/api/friendships/requests/incoming', {
      statusCode: 200,
      body: []
    }).as('getIncoming');

    cy.intercept('GET', '**/api/friendships', {
      statusCode: 200,
      body: []
    }).as('getFriends');

    cy.visit('/settings');
    cy.wait('@getUser');

    // Wait for settings to load
    cy.wait(2000);

    // Navigate to friends section (click on friends button in sidebar)
    // The sidebar has buttons with "Friends" label
    cy.get('button').contains(/^friends$/i).first().click({ force: true });

    // Wait for friends section to load
    cy.wait(2000);

    // Wait for friends section to fully load - look for the "Expand Network" heading
    cy.contains(/expand network/i, { timeout: 10000 }).should('be.visible');

    // Enter friend username in the input field (in the "Expand Network" section)
    cy.get('input[placeholder*="username"], input[placeholder*="handle"]').first().type('frienduser', { force: true });

    // Send friend request
    cy.get('button[type="submit"], button').contains(/send request/i).first().click({ force: true });

    // Wait for API call
    cy.wait('@sendRequest', { timeout: 10000 });

    // Wait for reload to complete (the component reloads after sending)
    cy.wait('@getOutgoing', { timeout: 10000 });
    cy.wait('@getIncoming', { timeout: 10000 });
    cy.wait('@getFriends', { timeout: 10000 });

    // Should show success message (the component shows "Request sent to {username}")
    // The message appears briefly, so check for it or check that the outgoing request appears
    cy.contains(/request sent to frienduser|frienduser/i, { timeout: 10000 }).should('be.visible');
  });

  it('should show validation error for empty username', () => {
    cy.intercept('GET', '**/api/friendships/requests/**', {
      statusCode: 200,
      body: []
    });
    
    cy.intercept('GET', '**/api/friendships', {
      statusCode: 200,
      body: []
    });

    cy.visit('/settings');
    cy.wait('@getUser');
    cy.wait(2000);

    cy.get('button').contains(/^friends$/i).first().click({ force: true });
    cy.wait(1000);

    // Wait for the form to be visible
    cy.contains(/expand network/i, { timeout: 10000 }).should('be.visible');
    
    // Try to send without username
    cy.get('button[type="submit"]').contains(/send request/i).first().click({ force: true });

    // Should show error
    cy.contains(/enter a username/i, { timeout: 5000 }).should('be.visible');
  });

  it('should display incoming friend requests', () => {
    cy.intercept('GET', '**/api/friendships/requests/incoming', {
      statusCode: 200,
      body: [
        {
          id: 1,
          requesterUsername: 'requester',
          addresseeUsername: 'testuser',
          status: 'PENDING'
        }
      ]
    }).as('getIncoming');

    cy.intercept('GET', '**/api/friendships/requests/outgoing', {
      statusCode: 200,
      body: []
    });

    cy.intercept('GET', '**/api/friendships', {
      statusCode: 200,
      body: []
    });

    cy.visit('/settings');
    cy.wait('@getUser');
    cy.wait(2000);

    cy.get('button').contains(/^friends$/i).first().click({ force: true });
    cy.wait(2000);
    
    // Wait for the friends section to load - look for section headings
    cy.contains(/expand network|incoming|outgoing/i, { timeout: 10000 }).should('be.visible');
    
    cy.wait('@getIncoming', { timeout: 10000 });

    // Should show incoming request with the requester username
    cy.contains(/requester|@requester/i, { timeout: 5000 }).should('be.visible');
  });

  it('should allow accepting friend requests', () => {
    // Set up initial intercepts with unique aliases
    cy.intercept('GET', '**/api/friendships/requests/incoming', {
      statusCode: 200,
      body: [
        {
          id: 1,
          requesterUsername: 'requester',
          addresseeUsername: 'testuser',
          status: 'PENDING'
        }
      ]
    }).as('getIncomingAcceptTest');

    cy.intercept('GET', '**/api/friendships/requests/outgoing', {
      statusCode: 200,
      body: []
    });

    cy.intercept('GET', '**/api/friendships', {
      statusCode: 200,
      body: []
    });

    cy.intercept('POST', '**/api/friendships/accept/**', {
      statusCode: 200,
      body: { message: 'Friend request accepted' }
    }).as('acceptRequest');

    cy.visit('/settings');
    cy.wait('@getUser');
    cy.wait(2000);

    cy.get('button').contains(/^friends$/i).first().click({ force: true });
    cy.wait(2000);

    // Wait for the friends section to load
    cy.contains(/expand network/i, { timeout: 10000 }).should('be.visible');
    
    // Set up intercepts for reload BEFORE clicking accept
    // The reload() function calls all three endpoints: incoming, outgoing, and friends
    cy.intercept('GET', '**/api/friendships/requests/incoming', {
      statusCode: 200,
      body: [] // Empty after accepting
    }).as('getIncomingAfterAccept');
    
    cy.intercept('GET', '**/api/friendships/requests/outgoing', {
      statusCode: 200,
      body: []
    }).as('getOutgoingAfterAccept');
    
    cy.intercept('GET', '**/api/friendships', {
      statusCode: 200,
      body: [
        {
          id: 1,
          requesterUsername: 'requester',
          addresseeUsername: 'testuser',
          status: 'ACCEPTED'
        }
      ]
    }).as('getFriendsAfterAccept');
    
    // Wait for incoming requests to load
    cy.wait('@getIncomingAcceptTest', { timeout: 10000 });
    
    // Wait a bit for the UI to render
    cy.wait(2000);

    // Verify incoming request is visible
    cy.contains(/requester|@requester/i, { timeout: 5000 }).should('be.visible');

    // Click accept button (should be in the incoming requests section)
    // The accept button is a FriendButton component with "Accept" text
    cy.get('button').contains(/^accept$/i).first().click({ force: true });

    cy.wait('@acceptRequest', { timeout: 10000 });

    // The component reloads after accepting - wait for the reload API calls
    // reload() calls all three endpoints in parallel
    cy.wait('@getIncomingAfterAccept', { timeout: 10000 });
    cy.wait('@getOutgoingAfterAccept', { timeout: 10000 });
    cy.wait('@getFriendsAfterAccept', { timeout: 10000 });
    
    // Wait a bit for UI to update
    cy.wait(2000);
    
    // The UI should update - check that the request is no longer in incoming section
    // After accepting, it moves to friends list
    
    // Find the grid container with the three sections (Incoming, Pending, Friends)
    // The grid has class "grid grid-cols-1 lg:grid-cols-3 gap-6"
    cy.get('div[class*="grid"][class*="gap-6"]', { timeout: 5000 }).should('be.visible').within(() => {
      // Verify incoming section shows "Empty" (no more pending requests)
      cy.contains(/^incoming$/i)
        .closest('div[class*="bg-slate-50"]') // Find the parent section card
        .contains(/empty/i)
        .should('be.visible');
      
      // And "requester" should now be in the Friends section
      // Find the Friends section heading and verify requester username is in that section
      cy.contains(/^friends$/i)
        .closest('div[class*="bg-slate-50"]') // Find the parent section card
        .contains(/@?requester/i, { timeout: 5000 })
        .should('be.visible');
    });
  });

  it('should require authentication to manage friends', () => {
    cy.clearAuth();
    cy.visit('/settings');

    // Should redirect to login
    cy.url().should('include', '/login');
  });
});
