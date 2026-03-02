import { expect, test } from '@playwright/test'

test('create book then borrow and return', async ({ page }) => {
  const uniqueBookTitle = `Clean Code ${Date.now()}`
  await page.goto('/')

  await page.getByTestId('input-book-title').fill(uniqueBookTitle)
  await page.getByTestId('input-book-author').fill('Robert C. Martin')
  await page.getByTestId('input-book-total-copies').fill('1')
  await page.getByTestId('button-create-book').click()

  const createdBookRow = page.getByTestId(/book-row-/).filter({ hasText: uniqueBookTitle }).first()
  await expect(createdBookRow).toBeVisible()
  const bookRowTestId = await createdBookRow.getAttribute('data-testid')
  const bookId = Number((bookRowTestId ?? '').replace('book-row-', ''))
  expect(bookId).toBeGreaterThan(0)

  await expect(page.getByTestId(`book-available-${bookId}`)).toHaveText('1')

  await page.getByTestId(`input-borrower-${bookId}`).fill('Samson')
  await page.getByTestId(`button-borrow-${bookId}`).click()

  await expect(page.getByTestId(`book-available-${bookId}`)).toHaveText('0')
  await expect(page.getByTestId(`book-status-${bookId}`)).toHaveText('CHECKED_OUT')

  const createdBorrowRecordRow = page.getByTestId(/borrow-record-row-/).filter({ hasText: uniqueBookTitle }).first()
  await expect(createdBorrowRecordRow).toBeVisible()
  const borrowRecordRowTestId = await createdBorrowRecordRow.getAttribute('data-testid')
  const borrowRecordId = Number((borrowRecordRowTestId ?? '').replace('borrow-record-row-', ''))
  expect(borrowRecordId).toBeGreaterThan(0)
  await expect(page.getByTestId(`borrow-record-status-${borrowRecordId}`)).toHaveText('BORROWED')

  await page.getByTestId(`input-returned-by-${borrowRecordId}`).fill('Samson')
  await page.getByTestId(`button-return-${borrowRecordId}`).click()

  await expect(page.getByTestId(`book-available-${bookId}`)).toHaveText('1')
  await expect(page.getByTestId(`book-status-${bookId}`)).toHaveText('AVAILABLE')
  await expect(page.getByTestId(`borrow-record-status-${borrowRecordId}`)).toHaveText('RETURNED')
})
