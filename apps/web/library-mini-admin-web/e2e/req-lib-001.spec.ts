import { expect, test } from '@playwright/test'

test('shows validation message mapped from business code when borrower is empty', async ({ page }) => {
  const uniqueBookTitle = `DDD ${Date.now()}`

  await page.goto('/')
  await page.getByTestId('input-book-title').fill(uniqueBookTitle)
  await page.getByTestId('input-book-author').fill('Eric Evans')
  await page.getByTestId('input-book-total-copies').fill('1')
  await page.getByTestId('button-create-book').click()

  const createdBookRow = page.getByTestId(/book-row-/).filter({ hasText: uniqueBookTitle }).first()
  await expect(createdBookRow).toBeVisible()
  const bookRowTestId = await createdBookRow.getAttribute('data-testid')
  const bookId = Number((bookRowTestId ?? '').replace('book-row-', ''))
  expect(bookId).toBeGreaterThan(0)

  await page.getByTestId(`button-borrow-${bookId}`).click()
  await expect(page.getByTestId('error-message')).toHaveText('欄位格式不正確，請確認輸入內容。')
})

test('disables borrow button when available copies reaches zero', async ({ page }) => {
  const uniqueBookTitle = `DDIA ${Date.now()}`

  await page.goto('/')
  await page.getByTestId('input-book-title').fill(uniqueBookTitle)
  await page.getByTestId('input-book-author').fill('Martin Kleppmann')
  await page.getByTestId('input-book-total-copies').fill('1')
  await page.getByTestId('button-create-book').click()

  const createdBookRow = page.getByTestId(/book-row-/).filter({ hasText: uniqueBookTitle }).first()
  await expect(createdBookRow).toBeVisible()
  const bookRowTestId = await createdBookRow.getAttribute('data-testid')
  const bookId = Number((bookRowTestId ?? '').replace('book-row-', ''))
  expect(bookId).toBeGreaterThan(0)

  await page.getByTestId(`input-borrower-${bookId}`).fill('Samson')
  await page.getByTestId(`button-borrow-${bookId}`).click()

  await expect(page.getByTestId(`book-status-${bookId}`)).toHaveText('CHECKED_OUT')
  await expect(page.getByTestId(`book-available-${bookId}`)).toHaveText('0')
  await expect(page.getByTestId(`button-borrow-${bookId}`)).toBeDisabled()
})
