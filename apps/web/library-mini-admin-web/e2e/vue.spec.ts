import { LibraryConsolePage } from './LibraryConsolePage'
import { test, expect } from './fixtures'

test.describe('SCN-LIB-001 library mini admin console', () => {
  test.describe.configure({ mode: 'parallel' })

  test('[AC-001][AC-003][AC-006][NFR-001][NFR-003] creates, borrows, and returns one copy through the UI', async ({ page, bookData }) => {
    const book = bookData.newBook({ quantity: 1 })
    const consolePage = new LibraryConsolePage(page)

    await consolePage.open()
    const createStartedAt = Date.now()
    await consolePage.createBook(book)
    const createdRow = await consolePage.searchFor(book.title)
    expect(Date.now() - createStartedAt).toBeLessThan(2000)
    await expect(createdRow).toContainText('1 / 1')
    await expect(createdRow).toContainText('可借閱')

    const borrowStartedAt = Date.now()
    await consolePage.borrowFromRow(createdRow, `${bookData.runId}-reader`)
    expect(Date.now() - borrowStartedAt).toBeLessThan(2000)
    await expect(createdRow).toContainText('0 / 1')
    await expect(createdRow).toContainText('借閱中')
    await expect(createdRow.getByRole('button', { name: '不可借' })).toBeDisabled()

    const returnStartedAt = Date.now()
    await consolePage.returnFromRow(createdRow)
    expect(Date.now() - returnStartedAt).toBeLessThan(2000)
    await expect(createdRow).toContainText('1 / 1')
    await expect(createdRow).toContainText('可借閱')
    await expect(createdRow.getByRole('button', { name: '無借閱' })).toBeDisabled()
  })

  test('[AC-002] rejects duplicate ISBN and keeps the existing inventory unchanged', async ({ page, bookData }) => {
    const book = bookData.newBook()
    await bookData.createBook(book)
    const consolePage = new LibraryConsolePage(page)

    await consolePage.open()
    await consolePage.fillBookForm(book)
    await consolePage.submitBookForm()

    await expect(consolePage.feedback).toContainText('ISBN 已存在')
    const row = await consolePage.searchFor(book.title)
    await expect(row).toHaveCount(1)
    await expect(row).toContainText('1 / 1')
  })

  test('[AC-004][AC-008] changes one copy at a time for a two-copy book', async ({ page, bookData }) => {
    const book = bookData.newBook({ quantity: 2 })
    const consolePage = new LibraryConsolePage(page)

    await bookData.createBook(book)
    await consolePage.open()
    const row = await consolePage.searchFor(book.title)

    await consolePage.borrowFromRow(row, `${bookData.runId}-reader-1`)
    await expect(row).toContainText('1 / 2')
    await expect(row).toContainText('可借閱')

    await consolePage.borrowFromRow(row, `${bookData.runId}-reader-2`)
    await expect(row).toContainText('0 / 2')
    await expect(row).toContainText('借閱中')

    await consolePage.returnFromRow(row)
    await expect(row).toContainText('1 / 2')
    await expect(row).toContainText('借閱中')
    await consolePage.returnFromRow(row)
    await expect(row).toContainText('2 / 2')
    await expect(row).toContainText('可借閱')
  })

  test('[AC-005][AC-007] blocks inactive borrowing and rejects an invalid return without changing inventory', async ({ page, bookData }) => {
    const inactiveBook = bookData.newBook({ isActive: false })
    const availableBook = bookData.newBook()
    await bookData.createBook(inactiveBook)
    await bookData.createBook(availableBook)

    const consolePage = new LibraryConsolePage(page)
    await consolePage.open()
    const inactiveRow = await consolePage.searchFor(inactiveBook.title)
    await expect(inactiveRow.getByRole('button', { name: '不可借' })).toBeDisabled()
    await expect(inactiveRow).toContainText('未上架')

    const availableRow = await consolePage.searchFor(availableBook.title)
    await consolePage.page.getByTestId('library-return-tab').click()
    await expect(availableRow).toHaveAttribute('data-book-id', /.+/)
    const bookId = await availableRow.evaluate((element) => element.getAttribute('data-book-id'))
    await consolePage.page.getByLabel('書籍 ID').fill(bookId as string)
    await consolePage.page.getByTestId('library-return-loan-input').fill('00000000-0000-0000-0000-000000000000')
    await consolePage.page.getByTestId('library-return-submit').click()

    await expect(consolePage.feedback).toContainText('找不到')
    await expect(availableRow).toContainText('1 / 1')
    await expect(availableRow).toContainText('可借閱')
  })

  test('[FR-004][NFR-002][NFR-005] supports search, pagination, keyboard tabs, and responsive stacking', async ({ page, bookData }) => {
    const books = Array.from({ length: 11 }, (_, index) => bookData.newBook({ title: `${bookData.runId} book-${index + 1}` }))
    for (const book of books) {
      await bookData.createBook(book)
    }

    await page.setViewportSize({ width: 390, height: 844 })
    const consolePage = new LibraryConsolePage(page)
    await consolePage.open()
    await consolePage.search(`${bookData.runId}-missing`)
    await expect(consolePage.page.getByText('找不到館藏')).toBeVisible()

    const matchingRows = await consolePage.searchFor(bookData.runId)
    await expect(matchingRows).toHaveCount(10)

    await consolePage.page.getByTestId('library-book-page-size').selectOption('20')
    await expect(consolePage.page.getByTestId('library-book-row')).toHaveCount(11)
    await expect(consolePage.page.getByTestId('library-book-next')).toBeDisabled()
    await consolePage.page.getByTestId('library-book-page-size').selectOption('10')
    await expect(consolePage.page.getByTestId('library-book-row')).toHaveCount(10)
    await expect(consolePage.page.getByTestId('library-book-next')).toBeEnabled()
    await consolePage.page.getByTestId('library-book-next').click()
    await expect(consolePage.page.getByTestId('library-book-row')).toHaveCount(1)
    const secondPageText = await consolePage.page.getByTestId('library-book-row').textContent()
    expect(books.some((book) => secondPageText?.includes(book.title))).toBe(true)

    await consolePage.page.getByTestId('library-return-tab').focus()
    await consolePage.page.keyboard.press('Enter')
    await expect(consolePage.page.getByTestId('library-return-submit')).toBeVisible()

    const order = await consolePage.page.evaluate(() => {
      const borrowTab = document.querySelector('[data-testid="library-borrow-tab"]')?.getBoundingClientRect()
      const addForm = document.querySelector('[data-testid="library-add-book-form"]')?.getBoundingClientRect()
      const table = document.querySelector('[data-testid="library-book-table"]')?.getBoundingClientRect()
      return { borrowTop: borrowTab?.top, addTop: addForm?.top, tableTop: table?.top, viewport: window.innerWidth, scrollWidth: document.documentElement.scrollWidth }
    })

    expect(order.borrowTop).toBeDefined()
    expect(order.addTop).toBeDefined()
    expect(order.tableTop).toBeDefined()
    expect(order.borrowTop).toBeLessThan(order.addTop as number)
    expect(order.addTop).toBeLessThan(order.tableTop as number)
    expect(order.scrollWidth).toBeLessThanOrEqual(order.viewport)
  })

  test('[AC-009][NFR-004] maps a B0000 catalogue failure to a visible system error', async ({ page }) => {
    await page.route('**/api/books**', async (route) => {
      if (route.request().method() === 'GET') {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({ code: 'B0000', message: '', traceId: 'qa-redacted-trace' }),
        })
        return
      }
      await route.continue()
    })

    const consolePage = new LibraryConsolePage(page)
    await consolePage.open()
    await expect(consolePage.feedback).toContainText('系統暫時無法完成操作')
    await expect(consolePage.feedback).not.toContainText('B0000')
  })
})
