import { expect, test } from '@playwright/test'

test('create book then borrow and return', async ({ page }) => {
  const uniqueTitle = `Clean Code ${Date.now()}`

  await page.goto('/')

  await page.getByTestId('book-title-input').fill(uniqueTitle)
  await page.getByTestId('book-author-input').fill('Robert C. Martin')
  await page.getByTestId('book-total-quantity-input').fill('1')
  await page.getByTestId('create-book-submit').click()

  const row = page.getByTestId(/book-row-\d+/).filter({ hasText: uniqueTitle }).first()
  await expect(row).toBeVisible()

  const rowTestId = await row.getAttribute('data-testid')
  if (!rowTestId) {
    throw new Error('Cannot find row test id for created book.')
  }
  const bookId = rowTestId.replace('book-row-', '')

  await page.getByTestId(`borrower-name-input-${bookId}`).fill('Samson')
  await page.getByTestId(`borrow-book-button-${bookId}`).click()

  await expect(page.getByTestId(`book-available-${bookId}`)).toHaveText('0')
  await expect(page.getByTestId(`book-status-${bookId}`)).toHaveText('BORROWED')

  const borrowRecordRow = page.getByTestId(/borrow-record-row-\d+/).first()
  await expect(borrowRecordRow).toContainText('Samson')

  const borrowRecordRowTestId = await borrowRecordRow.getAttribute('data-testid')
  if (!borrowRecordRowTestId) {
    throw new Error('Cannot find borrow record row test id.')
  }
  const borrowRecordId = borrowRecordRowTestId.replace('borrow-record-row-', '')

  await page.getByTestId(`return-book-button-${borrowRecordId}`).click()

  await expect(page.getByTestId(`book-available-${bookId}`)).toHaveText('1')
  await expect(page.getByTestId(`book-status-${bookId}`)).toHaveText('AVAILABLE')
})
