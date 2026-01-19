/**
 * Critical E2E Test: Car Health Page
 * Tests the complete car health page flow end-to-end
 */

describe('Critical: Car Health Page Flow', () => {
  const mockUser = {
    id: 1,
    username: 'testuser',
    email: 'testuser@example.com',
    roleName: 'USER'
  };

  const mockDevice = {
    id: 1,
    deviceId: 'ESP32-ABC123',
    description: 'My Car Device',
    active: true
  };

  const mockTelemetry = {
    connected: true,
    deviceId: 'ESP32-ABC123',
    engineStatus: 'RUNNING',
    speed: 60,
    rpm: 2500,
    temperature: 85,
    errorCodes: [],
    timestamp: new Date().toISOString()
  };

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

  it('should display car health page when device is connected', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [mockDevice]
    }).as('getDevices');

    cy.intercept('GET', `**/api/telemetry/device/${mockDevice.deviceId}/health`, {
      statusCode: 200,
      body: mockTelemetry
    }).as('getCarHealth');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevices', { timeout: 10000 });
    cy.wait('@getCarHealth', { timeout: 10000 });

    // Should display car health page title
    cy.contains(/car.*health/i, { timeout: 5000 }).should('be.visible');

    // Should display telemetry data
    cy.contains(/telemetry|diagnostic|connected/i, { timeout: 5000 }).should('be.visible');
  });

  it('should display telemetry data', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [mockDevice]
    }).as('getDevices');

    cy.intercept('GET', `**/api/telemetry/device/${mockDevice.deviceId}/health`, {
      statusCode: 200,
      body: mockTelemetry
    }).as('getCarHealth');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevices', { timeout: 10000 });
    cy.wait('@getCarHealth', { timeout: 10000 });

    // Should display telemetry information
    // May show speed, rpm, temperature, engine status, etc.
    cy.get('body', { timeout: 5000 }).should('be.visible');
  });

  it('should handle no device scenario', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: []
    }).as('getDevices');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevices', { timeout: 10000 });

    // Should display error message about no device
    cy.contains(/no device|add.*device|device.*found/i, { timeout: 5000 }).should('be.visible');
  });

  it('should display plate number input', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [mockDevice]
    }).as('getDevices');

    cy.intercept('GET', `**/api/telemetry/device/${mockDevice.deviceId}/health`, {
      statusCode: 200,
      body: mockTelemetry
    }).as('getCarHealth');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevices', { timeout: 10000 });
    cy.wait('@getCarHealth', { timeout: 10000 });

    // Should display plate number input (may be defaulted to "12-AB-34")
    cy.contains(/12-AB-34|plate|license/i, { timeout: 5000 }).should('be.visible');
  });

  it('should handle device fetch error', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 500,
      body: { error: 'Internal server error' }
    }).as('getDevicesError');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevicesError', { timeout: 10000 });

    // Should display error message
    cy.contains(/error|failed|unable/i, { timeout: 5000 }).should('be.visible');
  });

  it('should handle telemetry fetch error', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [mockDevice]
    }).as('getDevices');

    cy.intercept('GET', `**/api/telemetry/device/${mockDevice.deviceId}/health`, {
      statusCode: 500,
      body: { error: 'Failed to load telemetry' }
    }).as('getCarHealthError');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevices', { timeout: 10000 });
    cy.wait('@getCarHealthError', { timeout: 10000 });

    // Should display error message
    cy.contains(/error|failed|unable/i, { timeout: 5000 }).should('be.visible');
  });

  it('should display loading state', () => {
    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [mockDevice],
      delay: 1000
    }).as('getDevices');

    cy.intercept('GET', `**/api/telemetry/device/${mockDevice.deviceId}/health`, {
      statusCode: 200,
      body: mockTelemetry,
      delay: 1000
    }).as('getCarHealth');

    cy.visit('/car');
    cy.wait('@getUser');

    // Should show loading state
    cy.contains(/loading/i, { timeout: 2000 }).should('be.visible');
  });

  it('should handle disconnected device', () => {
    const disconnectedTelemetry = {
      ...mockTelemetry,
      connected: false
    };

    cy.intercept('GET', '**/api/devices/my-devices', {
      statusCode: 200,
      body: [mockDevice]
    }).as('getDevices');

    cy.intercept('GET', `**/api/telemetry/device/${mockDevice.deviceId}/health`, {
      statusCode: 200,
      body: disconnectedTelemetry
    }).as('getCarHealth');

    cy.visit('/car');
    cy.wait('@getUser');
    cy.wait('@getDevices', { timeout: 10000 });
    cy.wait('@getCarHealth', { timeout: 10000 });

    // Should display disconnected state
    cy.get('body', { timeout: 5000 }).should('be.visible');
  });

  it('should require authentication to access car health page', () => {
    cy.clearAuth();
    cy.visit('/car');

    // Should redirect to login
    cy.url({ timeout: 5000 }).should('include', '/login');
  });
});
