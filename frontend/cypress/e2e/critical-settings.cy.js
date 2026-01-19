/**
 * Critical E2E Test: Settings Page
 * Tests the complete settings page flow end-to-end
 */

describe('Critical: Settings Page Flow', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    email: 'testuser@example.com',
    name: 'Test User',
    roleName: 'USER',
    avatarUrl: null,
    backgroundUrl: null
  };

  const mockDevices = [
    {
      id: 1,
      deviceId: 'ESP32-ABC123',
      description: 'My Car Device',
      active: true
    }
  ];

  beforeEach(() => {
    // Set up authenticated user
    cy.intercept('GET', '**/api/users/me', {
      statusCode: 200,
      body: mockUser
    }).as('getUser');

    // Intercept WebSocket requests
    cy.intercept({
      method: 'GET',
      url: '**/ws/info**',
    }, {
      statusCode: 200,
      body: '{"entropy":1234567890,"origins":["*:*"],"cookie_needed":false,"websocket":true}',
      headers: { 'Content-Type': 'application/json' }
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

  it('should display settings page', () => {
    cy.visit('/settings');
    cy.wait('@getUser');

    // Should display settings page title
    cy.contains(/system.*settings|settings/i, { timeout: 5000 }).should('be.visible');

    // Should display sidebar with sections
    cy.contains(/profile/i, { timeout: 5000 }).should('be.visible');
    cy.contains(/account/i, { timeout: 5000 }).should('be.visible');
    cy.contains(/device/i, { timeout: 5000 }).should('be.visible');
    cy.contains(/friends/i, { timeout: 5000 }).should('be.visible');
  });

  it('should navigate to profile settings section', () => {
    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Profile section
    cy.contains(/profile/i, { timeout: 5000 }).click({ force: true });

    // Should display profile settings content
    cy.contains(/profile/i, { timeout: 5000 }).should('be.visible');
  });

  it('should navigate to account settings section', () => {
    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Account section
    cy.contains(/account/i, { timeout: 5000 }).click({ force: true });

    // Should display account settings content
    cy.contains(/account|username|email|password/i, { timeout: 5000 }).should('be.visible');
  });

  it('should update username in account settings', () => {
    const newUsername = 'newusername';

    cy.intercept('PUT', '**/api/users/me', {
      statusCode: 200,
      body: { ...mockUser, username: newUsername }
    }).as('updateUser');

    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Account section
    cy.contains(/account/i, { timeout: 5000 }).click({ force: true });

    // Wait for account form to load
    cy.wait(1000);

    // Find and update username field by ID
    cy.get('#username-handle', { timeout: 5000 }).clear().type(newUsername, { force: true });

    // Find and click "Commit Changes" button
    cy.contains(/commit changes|commit/i, { timeout: 5000 }).click({ force: true });

    // Should call update API
    cy.wait('@updateUser', { timeout: 10000 });
  });

  it('should update name in account settings (not profile section)', () => {
    const newName = 'New Name';

    cy.intercept('PUT', '**/api/users/me', {
      statusCode: 200,
      body: { ...mockUser, name: newName }
    }).as('updateUser');

    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Account section (name is in account, not profile)
    cy.contains(/account/i, { timeout: 5000 }).click({ force: true });

    // Wait for account form to load and ensure Display Name field is visible
    cy.contains(/display name/i, { timeout: 5000 }).should('be.visible');
    
    // Find and update name field by ID
    cy.get('#display-name', { timeout: 5000 })
      .should('be.visible')
      .clear({ force: true })
      .type(newName, { force: true });

    // Find and click "Commit Changes" button
    cy.contains(/commit changes|commit/i, { timeout: 5000 }).click({ force: true });

    // Should call update API
    cy.wait('@updateUser', { timeout: 10000 });
  });

  it('should display device settings section', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: mockDevices
    }).as('getDevices');

    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Device section
    cy.contains(/device/i, { timeout: 5000 }).click({ force: true });

    // Should load devices
    cy.wait('@getDevices', { timeout: 10000 });

    // Should display device information
    cy.contains(/device|esp32|abc123/i, { timeout: 5000 }).should('be.visible');
  });

  it('should add a new device', () => {
    const newDeviceId = 'ESP32-NEW123';
    const newDevice = {
      id: 2,
      deviceId: newDeviceId,
      description: 'New Device',
      active: false,
      apiKey: 'test-api-key-123'
    };

    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [] // No devices initially
    }).as('getDevices');

    cy.intercept('POST', '**/api/devices/register', {
      statusCode: 200,
      body: newDevice
    }).as('registerDevice');

    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Device section
    cy.contains(/device/i, { timeout: 5000 }).click({ force: true });
    cy.wait('@getDevices', { timeout: 10000 });

    // Click "Register Device" button to show form
    cy.contains(/register device/i, { timeout: 5000 }).click({ force: true });

    // Wait for form to appear and fill device ID
    cy.wait(500);
    cy.get('#deviceId', { timeout: 5000 }).type(newDeviceId, { force: true });

    // Submit form - click "Register Device" button in form
    cy.contains(/register device/i, { timeout: 5000 }).click({ force: true });

    // Should call register device API
    cy.wait('@registerDevice', { timeout: 10000 });
  });

  it('should display friends settings section', () => {
    cy.intercept('GET', '**/api/friendships/requests/incoming', {
      statusCode: 200,
      body: []
    }).as('getIncoming');

    cy.intercept('GET', '**/api/friendships/requests/outgoing', {
      statusCode: 200,
      body: []
    }).as('getOutgoing');

    cy.intercept('GET', '**/api/friendships', {
      statusCode: 200,
      body: []
    }).as('getFriends');

    cy.visit('/settings');
    cy.wait('@getUser');

    // Click on Friends section
    cy.contains(/friends/i, { timeout: 5000 }).click({ force: true });

    // Should display friends section
    cy.contains(/friends|expand.*network|incoming|outgoing/i, { timeout: 5000 }).should('be.visible');
  });

  it('should logout from settings', () => {
    // The beforeEach already sets up getUser intercept, just visit
    cy.setAuth('mock-token', mockUser);
    cy.visit('/settings', { failOnStatusCode: false });
    
    // Wait for page to load - check for settings sidebar to be visible
    cy.contains(/profile|account|device|friends/i, { timeout: 10000 }).should('be.visible');
    
    // Wait a bit for settings page to fully render
    cy.wait(1000);

    // Find logout button - it's in SettingsSidebar with exact text "Danger: Log out"
    // Use a simpler selector - just find button containing "Log out" text
    cy.get('button').contains(/danger.*log out|log out/i, { timeout: 10000 }).click({ force: true });

    // Logout clears localStorage and navigates - SettingsPage handleLogout navigates to /
    // Should redirect to home page
    cy.wait(1000); // Wait for navigation
    cy.url({ timeout: 10000 }).should('satisfy', (url) => {
      return url.includes('/') && !url.includes('/settings') && !url.includes('/profile');
    });
  });

  it('should navigate back to profile from settings', () => {
    cy.visit('/settings');
    cy.wait('@getUser');

    // Find and click back/return button
    cy.contains(/return|back/i, { timeout: 5000 }).click({ force: true });

    // Should navigate to profile
    cy.url({ timeout: 5000 }).should('include', '/profile');
  });

  it('should require authentication to access settings', () => {
    cy.clearAuth();
    cy.visit('/settings');

    // Should redirect to login
    cy.url({ timeout: 5000 }).should('include', '/login');
  });
});
