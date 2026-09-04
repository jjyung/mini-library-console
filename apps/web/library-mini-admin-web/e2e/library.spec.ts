import type { Page, Response as PlaywrightResponse } from '@playwright/test'

import { createReaderId, createTestBook } from './library-data'
import { test, expect } from './fixtures'

function waitForApi(page: Page, path: string, method = 'POST') {
  return page.waitForResponse((response) => {
    const request = response.request()
    return new URL(response.url()).pathname === path && request.method() === method
  })
}

async function readBusinessCode(response: PlaywrightResponse) {
  return response.json() as Promise<{ code: string; traceId?: string }>
}

test.describe('SCN-LIB-001 library mini admin', () => {
  test('creates, borrows the last copy, and returns it with consistent state', async ({ page, libraryPage, testData }, testInfo) => {
    const book = createTestBook(testInfo, 'journey', 1)
    const readerId = createReaderId(testInfo, 'journey')

    await libraryPage.open()
    await expect(page.getByTestId('admin-identity')).toContainText('Admin')

    const createResponsePromise = waitForApi(page, '/api/books')
    await libraryPage.createBook(book)
    const createResponse = await createResponsePromise
    expect(createResponse.status()).toBe(201)
    expect((await readBusinessCode(createResponse)).code).toBe('00000')

    await expect(libraryPage.row(book.isbn)).toContainText(book.title)
    await expect(libraryPage.status(book.isbn)).toHaveText('可借閱')
    await expect(libraryPage.available(book.isbn)).toHaveText('1')

    const borrowResponsePromise = waitForApi(page, '/api/loans/borrow')
    await libraryPage.borrow(book.isbn, readerId, '2026-10-01')
    const borrowResponse = await borrowResponsePromise
    expect(borrowResponse.status()).toBe(200)
    expect((await readBusinessCode(borrowResponse)).code).toBe('00000')
    testData.rememberActiveLoan(book.isbn, readerId)

    await expect(page.getByTestId('borrow-status')).toContainText('借書成功')
    await expect(page.getByTestId('catalogue-success-toast')).toContainText('借書成功')
    await expect(libraryPage.status(book.isbn)).toHaveText('已借出')
    await expect(libraryPage.available(book.isbn)).toHaveText('0')
    await expect(page.getByTestId(`quick-borrow-${book.isbn}`)).toBeDisabled()
    await expect(page.getByTestId(`quick-return-${book.isbn}`)).toBeEnabled()

    await libraryPage.selectReturnTab()
    const returnResponsePromise = waitForApi(page, '/api/loans/return')
    await libraryPage.returnBook(book.isbn, readerId)
    const returnResponse = await returnResponsePromise
    expect(returnResponse.status()).toBe(200)
    expect((await readBusinessCode(returnResponse)).code).toBe('00000')
    testData.forgetActiveLoan(book.isbn, readerId)

    await expect(page.getByTestId('return-status')).toContainText('歸還成功')
    await expect(page.getByTestId('return-status')).not.toContainText('罰款')
    await expect(libraryPage.status(book.isbn)).toHaveText('可借閱')
    await expect(libraryPage.available(book.isbn)).toHaveText('1')
    await expect(page.getByTestId(`quick-borrow-${book.isbn}`)).toBeEnabled()
    await expect(page.getByTestId(`quick-return-${book.isbn}`)).toBeDisabled()
  })

  test('shows client-side validation and preserves the editable input', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByTestId('add-book-form')).toBeVisible()
    await page.getByTestId('add-book-title').fill('保留的修正輸入')
    await page.getByTestId('add-book-submit').click()
    await expect(page.getByTestId('add-book-status')).toContainText('請修正')
    await expect(page.getByTestId('add-book-title')).toHaveValue('保留的修正輸入')
  })

  test('shows an A0000 error for an unknown book without changing state', async ({ page, libraryPage }, testInfo) => {
    const unknownBook = createTestBook(testInfo, 'unknown')
    const readerId = createReaderId(testInfo, 'unknown')

    await libraryPage.open()
    await page.getByTestId('borrow-reader-id').fill(readerId)
    await page.getByTestId('borrow-isbn').fill(unknownBook.isbn)
    const borrowResponsePromise = waitForApi(page, '/api/loans/borrow')
    await page.getByTestId('borrow-submit').click()
    const borrowResponse = await borrowResponsePromise
    expect(borrowResponse.status()).toBe(400)
    expect((await readBusinessCode(borrowResponse)).code).toBe('A0000')
    await expect(page.getByTestId('borrow-status')).toContainText('錯誤碼 A0000')
    await expect(page.getByTestId('borrow-status')).toContainText('找不到可借出的書籍')
    await expect(page.getByTestId(`book-row-${unknownBook.isbn}`)).toHaveCount(0)
  })

  test('rejects a duplicate ISBN through the UI and keeps one catalogue row', async ({ page, libraryPage, testData }, testInfo) => {
    const book = createTestBook(testInfo, 'duplicate')
    await testData.createBook(book)

    await libraryPage.open()
    const createResponsePromise = waitForApi(page, '/api/books')
    await libraryPage.createBook(book)
    const createResponse = await createResponsePromise
    expect(createResponse.status()).toBe(400)
    expect((await readBusinessCode(createResponse)).code).toBe('A0000')
    await expect(page.getByTestId('add-book-status')).toContainText('錯誤碼 A0000')
    await expect(page.getByTestId('add-book-status')).toContainText('ISBN')
    await expect(page.getByTestId(`book-row-${book.isbn}`)).toHaveCount(1)
  })

  test('disables quick borrow for inactive and fully borrowed books', async ({ page, libraryPage, testData }, testInfo) => {
    const inactiveBook = createTestBook(testInfo, 'inactive', 1, false)
    const borrowedBook = createTestBook(testInfo, 'fully-borrowed')
    const readerId = createReaderId(testInfo, 'fully-borrowed')
    await testData.createBook(inactiveBook)
    await testData.createBook(borrowedBook)
    await testData.borrowBook(borrowedBook.isbn, readerId)

    await libraryPage.open()
    await expect(libraryPage.status(inactiveBook.isbn)).toHaveText('未上架')
    await expect(page.getByTestId(`quick-borrow-${inactiveBook.isbn}`)).toBeDisabled()
    await expect(libraryPage.status(borrowedBook.isbn)).toHaveText('已借出')
    await expect(libraryPage.available(borrowedBook.isbn)).toHaveText('0')
    await expect(page.getByTestId(`quick-borrow-${borrowedBook.isbn}`)).toBeDisabled()
    await expect(page.getByTestId(`quick-return-${borrowedBook.isbn}`)).toBeEnabled()
  })

  test('rejects returning a book with no active loan and preserves availability', async ({ page, libraryPage, testData }, testInfo) => {
    const book = createTestBook(testInfo, 'no-loan', 1)
    await testData.createBook(book)

    await libraryPage.open()
    await libraryPage.selectReturnTab()
    const returnResponsePromise = waitForApi(page, '/api/loans/return')
    await libraryPage.returnBook(book.isbn)
    const returnResponse = await returnResponsePromise
    expect(returnResponse.status()).toBe(400)
    expect((await readBusinessCode(returnResponse)).code).toBe('A0000')
    await expect(page.getByTestId('return-status')).toContainText('錯誤碼 A0000')
    await expect(page.getByTestId('return-status')).toContainText('找不到可歸還的借閱紀錄')
    await expect(libraryPage.status(book.isbn)).toHaveText('可借閱')
    await expect(libraryPage.available(book.isbn)).toHaveText('1')
  })

  test('renders an empty catalogue response as an explicit empty state', async ({ page }) => {
    await page.route('**/api/books', async (route) => {
      if (route.request().method() !== 'GET') return route.continue()
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: '00000', message: 'Success', traceId: 'qa-empty', data: [] }),
      })
    })

    await page.goto('/')
    await expect(page.getByTestId('catalogue-empty')).toContainText('尚無館藏')
    await expect(page.getByTestId('catalogue-empty')).toContainText('請使用右側表單新增書籍')
    await expect(page.getByTestId(/book-row-/)).toHaveCount(0)
  })

  test('maps a catalogue system failure to B0000 and recovers through retry', async ({ page }) => {
    let attempts = 0
    await page.route('**/api/books', async (route) => {
      if (route.request().method() !== 'GET') return route.continue()
      attempts += 1
      if (attempts > 1) {
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ code: '00000', message: 'Success', traceId: 'qa-retry', data: [] }),
        })
      }
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ code: 'B0000', message: 'System failure', traceId: 'qa-system' }),
      })
    })

    await page.goto('/')
    await expect(page.getByTestId('catalogue-error')).toContainText('錯誤碼 B0000')
    await expect(page.getByTestId('catalogue-retry')).toBeEnabled()

    const retryResponsePromise = waitForApi(page, '/api/books', 'GET')
    await page.getByTestId('catalogue-retry').click()
    const retryResponse = await retryResponsePromise
    expect(retryResponse.status()).toBe(200)
    expect((await readBusinessCode(retryResponse)).code).toBe('00000')
    await expect(page.getByTestId('catalogue-empty')).toContainText('尚無館藏')
  })

  test('keeps the Figma page structure usable at a narrow viewport', async ({ page, libraryPage }) => {
    await page.route('**/api/books', async (route) => {
      if (route.request().method() !== 'GET') return route.continue()
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: '00000',
          message: 'Success',
          traceId: 'qa-responsive',
          data: [{
            bookId: '00000000-0000-4000-8000-000000000001',
            title: 'QA Responsive Fixture',
            isbn: 'QA-RESPONSIVE',
            author: 'QA Test Author',
            category: 'technology',
            status: 'AVAILABLE',
            availableCount: 1,
            totalCount: 1,
            isActive: true,
          }],
        }),
      })
    })
    await page.setViewportSize({ width: 390, height: 844 })
    await libraryPage.open()
    await expect(page.getByTestId('topbar')).toBeVisible()
    await expect(page.getByTestId('transaction-card')).toBeVisible()
    await expect(page.getByTestId('add-book-form')).toBeVisible()
    await expect(page.getByRole('table', { name: '館藏書籍列表' })).toBeVisible()
  })
})
