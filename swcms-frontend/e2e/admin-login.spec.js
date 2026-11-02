import { expect, test } from '@playwright/test'

test('local administrator signs in and reaches the dashboard', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Admin Login' }).click()
  await page.locator('input[name="email"]').fill('admin@swcms.local')
  await page.locator('input[name="password"]').fill('LocalAdmin123!')
  await page.getByRole('button', { name: 'Login', exact: true }).click()

  await expect(
    page.getByRole('heading', { name: 'Welcome, Administrator!' })
  ).toBeVisible()
})
