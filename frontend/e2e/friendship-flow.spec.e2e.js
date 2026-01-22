import { test, expect } from '@playwright/test';

/**
 * E2E test for the full friendship flow:
 * 1. User 1 registers → logs in → sends friend request to User 2
 * 2. User 2 registers → logs in → accepts the friend request
 * 3. User 1 removes User 2 as a friend (unfriends)
 * 
 * This tests:
 * - Friend request creation
 * - Friend request acceptance
 * - Friendship removal (unfriend)
 * - Friendship list endpoints
 */
test('friendship flow: user sends request, friend accepts, then removes friend', async ({ page, request, context }) => {
  // Arrange
  const apiBase = process.env.VITE_API_URL || process.env.PLAYWRIGHT_API_URL || 'http://localhost:8080';
  const unique = `${Date.now()}-${Math.random().toString(16).slice(2)}`;

  const user1Email = `requester-${unique}@example.com`;
  const user1Password = 'testpassword123';
  const user1Username = `requester_${unique}`;

  const user2Email = `accepter-${unique}@example.com`;
  const user2Password = 'testpassword123';
  const user2Username = `accepter_${unique}`;

  // Register User 1 (requester)
  const registerUser1 = await request.post(`${apiBase}/api/users/register`, {
    data: {
      username: user1Username,
      email: user1Email,
      password: user1Password,
      name: 'Requester User',
    },
  });
  expect(registerUser1.ok(), `User 1 registration should succeed: ${await registerUser1.text()}`).toBeTruthy();
  const user1Data = await registerUser1.json();
  expect(user1Data.roleName, 'User 1 should have USER role').toBe('USER');

  // Register User 2 (accepter)
  const registerUser2 = await request.post(`${apiBase}/api/users/register`, {
    data: {
      username: user2Username,
      email: user2Email,
      password: user2Password,
      name: 'Accepter User',
    },
  });
  expect(registerUser2.ok(), `User 2 registration should succeed: ${await registerUser2.text()}`).toBeTruthy();
  const user2Data = await registerUser2.json();
  expect(user2Data.roleName, 'User 2 should have USER role').toBe('USER');

  // ============================================
  // STEP 1: User 1 logs in and sends friend request to User 2
  // ============================================
  // Act - User 1 logs in
  await page.goto('/login');
  await page.locator('#login-email').fill(user1Email);
  await page.locator('#login-password').fill(user1Password);
  await page.getByRole('button', { name: /initialize log in/i }).click();
  await page.waitForURL('**/map', { timeout: 30_000 });

  // Get User 1's token
  const token1 = await page.evaluate(() => window.localStorage.getItem('token'));
  expect(token1, 'User 1 should have a token').toBeTruthy();

  // Act - User 1 sends friend request to User 2
  const sendRequestRes = await request.post(`${apiBase}/api/friendships/request`, {
    headers: {
      Authorization: `Bearer ${token1}`,
      'Content-Type': 'application/json',
    },
    data: {
      username: user2Username,
    },
  });
  expect(sendRequestRes.ok(), `Friend request should succeed: ${await sendRequestRes.text()}`).toBeTruthy();

  // Assert - Verify User 1 has outgoing request
  const outgoingRequests1 = await request.get(`${apiBase}/api/friendships/requests/outgoing`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  expect(outgoingRequests1.ok(), 'Should be able to fetch outgoing requests').toBeTruthy();
  const outgoing1 = await outgoingRequests1.json();
  expect(outgoing1.length, 'User 1 should have 1 outgoing request').toBe(1);
  expect(outgoing1[0].requesterUsername, 'Outgoing request requester should be User 1').toBe(user1Username);
  expect(outgoing1[0].addresseeUsername, 'Outgoing request should be to User 2').toBe(user2Username);
  expect(outgoing1[0].status, 'Request status should be REQUESTED').toBe('REQUESTED');

  // Assert - Verify User 2 has incoming request
  const token2Initial = await getToken(apiBase, user2Email, user2Password, request);
  const incomingRequests2 = await request.get(`${apiBase}/api/friendships/requests/incoming`, {
    headers: { Authorization: `Bearer ${token2Initial}` },
  });
  expect(incomingRequests2.ok(), 'Should be able to fetch incoming requests').toBeTruthy();
  const incoming2 = await incomingRequests2.json();
  expect(incoming2.length, 'User 2 should have 1 incoming request').toBe(1);
  expect(incoming2[0].requesterUsername, 'Incoming request should be from User 1').toBe(user1Username);
  expect(incoming2[0].addresseeUsername, 'Incoming request addressee should be User 2').toBe(user2Username);
  expect(incoming2[0].status, 'Request status should be REQUESTED').toBe('REQUESTED');

  // ============================================
  // STEP 2: User 2 logs in and accepts the friend request
  // ============================================
  // Act - User 2 logs in
  const page2 = await context.newPage();
  await page2.goto('/login');
  await page2.locator('#login-email').fill(user2Email);
  await page2.locator('#login-password').fill(user2Password);
  await page2.getByRole('button', { name: /initialize log in/i }).click();
  await page2.waitForURL('**/map', { timeout: 30_000 });

  // Get User 2's token
  const token2 = await page2.evaluate(() => window.localStorage.getItem('token'));
  expect(token2, 'User 2 should have a token').toBeTruthy();

  // Act - User 2 accepts the friend request
  const acceptRequestRes = await request.post(`${apiBase}/api/friendships/accept/${encodeURIComponent(user1Username)}`, {
    headers: {
      Authorization: `Bearer ${token2}`,
    },
  });
  expect(acceptRequestRes.ok(), `Accept friend request should succeed: ${await acceptRequestRes.text()}`).toBeTruthy();

  // Assert - Verify both users are now friends
  const friends1 = await request.get(`${apiBase}/api/friendships`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  expect(friends1.ok(), 'Should be able to fetch User 1 friends').toBeTruthy();
  const friends1List = await friends1.json();
  expect(friends1List.length, 'User 1 should have 1 friend').toBe(1);
  // Check that the friendship includes both users (could be requester or addressee)
  const friendship1 = friends1List[0];
  expect(
    (friendship1.requesterUsername === user1Username && friendship1.addresseeUsername === user2Username) ||
    (friendship1.requesterUsername === user2Username && friendship1.addresseeUsername === user1Username),
    'User 1 should be friends with User 2'
  ).toBeTruthy();
  expect(friendship1.status, 'Friendship status should be ACCEPTED').toBe('ACCEPTED');

  const friends2 = await request.get(`${apiBase}/api/friendships`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  expect(friends2.ok(), 'Should be able to fetch User 2 friends').toBeTruthy();
  const friends2List = await friends2.json();
  expect(friends2List.length, 'User 2 should have 1 friend').toBe(1);
  // Check that the friendship includes both users
  const friendship2 = friends2List[0];
  expect(
    (friendship2.requesterUsername === user1Username && friendship2.addresseeUsername === user2Username) ||
    (friendship2.requesterUsername === user2Username && friendship2.addresseeUsername === user1Username),
    'User 2 should be friends with User 1'
  ).toBeTruthy();
  expect(friendship2.status, 'Friendship status should be ACCEPTED').toBe('ACCEPTED');

  // Assert - Verify no pending requests remain
  const incomingAfterAccept = await request.get(`${apiBase}/api/friendships/requests/incoming`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  const incomingAfterAcceptList = await incomingAfterAccept.json();
  expect(incomingAfterAcceptList.length, 'User 2 should have no pending incoming requests').toBe(0);

  const outgoingAfterAccept = await request.get(`${apiBase}/api/friendships/requests/outgoing`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  const outgoingAfterAcceptList = await outgoingAfterAccept.json();
  expect(outgoingAfterAcceptList.length, 'User 1 should have no pending outgoing requests').toBe(0);

  // ============================================
  // STEP 3: User 1 removes User 2 as a friend
  // ============================================
  // Act - User 1 unfriends User 2
  const unfriendRes = await request.delete(`${apiBase}/api/friendships/unfriend/${encodeURIComponent(user2Username)}`, {
    headers: {
      Authorization: `Bearer ${token1}`,
    },
  });
  expect(unfriendRes.ok(), `Unfriend should succeed: ${await unfriendRes.text()}`).toBeTruthy();

  // Assert - Verify friendship is removed for both users
  const friends1AfterUnfriend = await request.get(`${apiBase}/api/friendships`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  const friends1AfterUnfriendList = await friends1AfterUnfriend.json();
  expect(friends1AfterUnfriendList.length, 'User 1 should have 0 friends after unfriending').toBe(0);

  const friends2AfterUnfriend = await request.get(`${apiBase}/api/friendships`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  const friends2AfterUnfriendList = await friends2AfterUnfriend.json();
  expect(friends2AfterUnfriendList.length, 'User 2 should have 0 friends after being unfriended').toBe(0);

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
