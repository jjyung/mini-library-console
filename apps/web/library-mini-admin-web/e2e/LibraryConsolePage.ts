import { expect, type Locator, type Page } from '@playwright/test'

import type { BookData } from './data'

export class LibraryConsolePage {
  readonly page: Page
  readonly searchInput: Locator
  readonly addBookForm: Locator
  readonly feedback: Locator
  readonly bookTable: Locator

  constructor(page: Page) {
    this.page = page
    this.searchInput = page.getByTestId('library-topbar-search')
    this.addBookForm = page.getByTestId('library-add-book-form')
    this.feedback = page.getByRole('alert')
    this.bookTable = page.getByTestId('library-book-table')
  }

  async open(): Promise<void> {
    await this.page.goto('/')
    await expect(this.searchInput).toBeVisible()
    await expect(this.addBookForm).toBeVisible()
  }

  async fillBookForm(book: BookData): Promise<void> {
    await this.addBookForm.getByLabel('書名').fill(book.title)
    await this.addBookForm.getByLabel('ISBN').fill(book.isbn)
    await this.addBookForm.getByLabel('作者 (選填)').fill(book.author ?? '')
    await this.addBookForm.getByLabel('分類').selectOption(book.category)
    await this.addBookForm.getByLabel('初始數量').fill(book.quantity.toString())

    const activeSwitch = this.addBookForm.getByLabel('立即上架')
    if (book.isActive !== (await activeSwitch.isChecked())) {
      await activeSwitch.check()
    }
  }

  async submitBookForm(): Promise<void> {
    await this.addBookForm.getByTestId('library-add-book-submit').click()
  }

  async createBook(book: BookData): Promise<void> {
    await this.fillBookForm(book)
    await this.submitBookForm()
    await expect(this.feedback).toContainText('書籍建立成功')
  }

  async searchFor(value: string): Promise<Locator> {
    await this.search(value)
    const row = this.rowFor(value)
    await expect(row.first()).toBeVisible()
    return row
  }

  async search(value: string): Promise<void> {
    await this.searchInput.fill(value)
    await this.searchInput.press('Enter')
  }

  rowFor(value: string): Locator {
    return this.page.getByTestId('library-book-row').filter({ hasText: value })
  }

  async borrowFromRow(row: Locator, readerId: string): Promise<void> {
    await row.getByRole('button', { name: '借出' }).click()
    await expect(this.page.getByTestId('library-borrow-tab')).toHaveAttribute('aria-selected', 'true')
    await expect(this.page.getByLabel('書籍 ID')).not.toHaveValue('')
    await this.page.getByTestId('library-borrow-reader-input').fill(readerId)
    await this.page.getByTestId('library-borrow-submit').click()
    await expect(this.feedback).toContainText('借出成功')
  }

  async returnFromRow(row: Locator): Promise<void> {
    await row.getByRole('button', { name: '歸還' }).click()
    await expect(this.page.getByTestId('library-return-tab')).toHaveAttribute('aria-selected', 'true')
    await expect(this.page.getByTestId('library-return-loan-input')).not.toHaveValue('')
    await this.page.getByTestId('library-return-submit').click()
    await expect(this.feedback).toContainText('歸還成功')
  }
}
