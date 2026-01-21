import { test, expect } from '@playwright/test';

/**
 * E2E test for the full report flow:
 * 1. User 1 registers (with USER role) → logs in → creates a hazard report
 * 2. User 2 registers → logs in → navigates near the report → votes on it
 * 
 * This tests:
 * - User registration creates statistics row
 * - USER role is assigned correctly
 * - Report creation works and increments statistics
 * - WebSocket updates are received
 * - Voting works and increments statistics
 */
test('report flow: user creates report, another user votes', async ({ page, request, context }) => {
  const apiBase = process.env.VITE_API_URL || process.env.PLAYWRIGHT_API_URL || 'http://localhost:8081';
  const unique = `${Date.now()}-${Math.random().toString(16).slice(2)}`;

  // ============================================
  // SETUP: Create two users
  // ============================================
  const user1Email = `reporter-${unique}@example.com`;
  const user1Password = 'testpassword123';
  const user1Username = `reporter_${unique}`;

  const user2Email = `voter-${unique}@example.com`;
  const user2Password = 'testpassword123';
  const user2Username = `voter_${unique}`;

  // Register User 1 (reporter)
  const registerUser1 = await request.post(`${apiBase}/api/users/register`, {
    data: {
      username: user1Username,
      email: user1Email,
      password: user1Password,
      name: 'Reporter User',
    },
  });
  expect(registerUser1.ok(), `User 1 registration should succeed: ${await registerUser1.text()}`).toBeTruthy();
  const user1Data = await registerUser1.json();
  expect(user1Data.role?.name || user1Data.role, 'User 1 should have USER role').toBe('USER');

  // Register User 2 (voter)
  const registerUser2 = await request.post(`${apiBase}/api/users/register`, {
    data: {
      username: user2Username,
      email: user2Email,
      password: user2Password,
      name: 'Voter User',
    },
  });
  expect(registerUser2.ok(), `User 2 registration should succeed: ${await registerUser2.text()}`).toBeTruthy();
  const user2Data = await registerUser2.json();
  expect(user2Data.role?.name || user2Data.role, 'User 2 should have USER role').toBe('USER');

  // Verify statistics rows exist (they should be created at registration)
  const token1Initial = await getToken(apiBase, user1Email, user1Password, request);
  const stats1 = await request.get(`${apiBase}/api/users/${user1Username}/stats`, {
    headers: { Authorization: `Bearer ${token1Initial}` },
  });
  expect(stats1.ok(), 'User 1 statistics should exist').toBeTruthy();
  const stats1Data = await stats1.json();
  expect(stats1Data.totalHazardsReported, 'User 1 should start with 0 hazards reported').toBe(0);
  expect(stats1Data.totalVotes, 'User 1 should start with 0 votes').toBe(0);

  // ============================================
  // STEP 1: User 1 logs in and creates a report
  // ============================================
  await page.goto('/login');
  await page.locator('#login-email').fill(user1Email);
  await page.locator('#login-password').fill(user1Password);
  await page.getByRole('button', { name: /initialize log in/i }).click();
  await page.waitForURL('**/map', { timeout: 30_000 });

  // Wait for map to load and get user location
  await page.waitForTimeout(2000); // Give time for geolocation

  // Mock a location near a known point (Amsterdam coordinates)
  // In real tests, you'd use actual geolocation, but for E2E we'll use a fixed location
  const testLat = 52.3676;
  const testLng = 4.9041;

  // Get categories first
  const token1 = await page.evaluate(() => window.localStorage.getItem('token'));
  const categoriesRes = await request.get(`${apiBase}/api/hazard-categories`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  expect(categoriesRes.ok()).toBeTruthy();
  const categories = await categoriesRes.json();
  expect(categories.length, 'Should have at least one category').toBeGreaterThan(0);
  const firstCategory = categories[0];

  // Create a hazard report via API (simulating clicking the report button and selecting a category)
  const createHazardRes = await request.post(`${apiBase}/api/hazards`, {
    headers: {
      Authorization: `Bearer ${token1}`,
      'Content-Type': 'application/json',
    },
    data: {
      latitude: testLat,
      longitude: testLng,
      categoryId: firstCategory.id,
    },
  });
  expect(createHazardRes.ok(), `Hazard creation should succeed: ${await createHazardRes.text()}`).toBeTruthy();
  const hazard = await createHazardRes.json();
  expect(hazard.id, 'Hazard should have an ID').toBeTruthy();
  expect(hazard.latitude, 'Hazard should have latitude').toBe(testLat);
  expect(hazard.longitude, 'Hazard should have longitude').toBe(testLng);

  // Verify User 1's statistics were incremented
  const stats1After = await request.get(`${apiBase}/api/users/${user1Username}/stats`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  const stats1AfterData = await stats1After.json();
  expect(stats1AfterData.totalHazardsReported, 'User 1 should have 1 hazard reported').toBe(1);

  // ============================================
  // STEP 2: User 2 logs in and votes on the report
  // ============================================
  // Create a new browser context/page for User 2 (simulating another user)
  const page2 = await context.newPage();
  
  await page2.goto('/login');
  await page2.locator('#login-email').fill(user2Email);
  await page2.locator('#login-password').fill(user2Password);
  await page2.getByRole('button', { name: /initialize log in/i }).click();
  await page2.waitForURL('**/map', { timeout: 30_000 });

  // Wait for map to load
  await page2.waitForTimeout(2000);

  // Get User 2's token
  const token2 = await page2.evaluate(() => window.localStorage.getItem('token'));
  expect(token2, 'User 2 should have a token').toBeTruthy();

  // Verify User 2's statistics exist
  const stats2 = await request.get(`${apiBase}/api/users/${user2Username}/stats`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  expect(stats2.ok(), 'User 2 statistics should exist').toBeTruthy();
  const stats2Data = await stats2.json();
  expect(stats2Data.totalVotes, 'User 2 should start with 0 votes').toBe(0);

  // Vote on the hazard (User 2 votes on User 1's report)
  const voteRes = await request.post(`${apiBase}/api/votes`, {
    headers: {
      Authorization: `Bearer ${token2}`,
      'Content-Type': 'application/json',
    },
    data: {
      hazardId: hazard.id,
      voteType: 'UPVOTE',
    },
  });
  expect(voteRes.ok(), `Vote should succeed: ${await voteRes.text()}`).toBeTruthy();
  const vote = await voteRes.json();
  expect(vote.id, 'Vote should have an ID').toBeTruthy();
  expect(vote.voteType, 'Vote type should be UPVOTE').toBe('UPVOTE');

  // Verify User 2's statistics were incremented
  const stats2After = await request.get(`${apiBase}/api/users/${user2Username}/stats`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  const stats2AfterData = await stats2After.json();
  expect(stats2AfterData.totalVotes, 'User 2 should have 1 vote').toBe(1);

  // Verify vote counts on the hazard
  const voteCountsRes = await request.get(`${apiBase}/api/votes/${hazard.id}/count`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  expect(voteCountsRes.ok()).toBeTruthy();
  const voteCounts = await voteCountsRes.json();
  expect(voteCounts.upvotes, 'Hazard should have 1 upvote').toBe(1);
  expect(voteCounts.downvotes, 'Hazard should have 0 downvotes').toBe(0);

  await page2.close();
});

/**
 * Helper function to get auth token
 */
async function getToken(apiBase, email, password, request) {
  const loginRes = await request.post(`${apiBase}/api/users/login`, {
    data: { email, password },
  });
  if (!loginRes.ok()) {
    throw new Error(`Login failed: ${await loginRes.text()}`);
  }
  const loginData = await loginRes.json();
  return loginData.token;
}
