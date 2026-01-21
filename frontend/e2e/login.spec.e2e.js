import { test, expect } from '@playwright/test';

test('login: user can log in and is redirected to /map', async ({ page, request }) => {
  const apiBase = process.env.VITE_API_URL || process.env.PLAYWRIGHT_API_URL || 'http://localhost:8080';

  // Create a unique user for this run (safe for parallel CI runs).
  const unique = `${Date.now()}-${Math.random().toString(16).slice(2)}`;
  const email = `e2e-${unique}@example.com`;
  const password = 'testpassword123';

  const registerRes = await request.post(`${apiBase}/api/users/register`, {
    data: {
      username: `e2e_${unique}`,
      email,
      password,
      name: 'E2E User',
    },
  });

  // If registration fails, surface useful debugging info.
  if (!registerRes.ok()) {
    throw new Error(
      `User registration failed (${registerRes.status()}): ${await registerRes.text()}`
    );
  }

  await page.goto('/login');
  await page.locator('#login-email').fill(email);
  await page.locator('#login-password').fill(password);
  await page.getByRole('button', { name: /initialize log in/i }).click();

  await page.waitForURL('**/map', { timeout: 30_000 });
  await expect(page).toHaveURL(/\/map$/);

  // Token is stored by frontend on successful login.
  const token = await page.evaluate(() => window.localStorage.getItem('token'));
  expect(token, 'token should be set in localStorage after login').toBeTruthy();
});

