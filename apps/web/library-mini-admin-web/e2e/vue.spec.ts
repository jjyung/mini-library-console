import { expect, test } from '@playwright/test'

test('visits app and sees required test ids', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByTestId('page-title')).toHaveText('Library Mini Admin')
  await expect(page.getByTestId('book-create-submit')).toBeVisible()
  await expect(page.getByTestId('loan-borrow-submit')).toBeVisible()
  await page.getByTestId('transaction-tab-return').click()
  await expect(page.getByTestId('loan-return-submit')).toBeVisible()
  await expect(page.getByTestId('book-search-submit')).toBeVisible()
})
