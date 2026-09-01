import { test, expect } from '@playwright/test'

// See here how to get started:
// https://playwright.dev/docs/intro
test('visits the app root url', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByTestId('library-admin-page')).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Library Mini Admin' })).toBeVisible()
})
