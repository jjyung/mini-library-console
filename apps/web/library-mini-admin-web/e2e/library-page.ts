import { expect, type Locator, type Page } from '@playwright/test'

export class LibraryAdminPage {
  readonly catalogue = this.page.getByTestId('book-table')

  constructor(private readonly page: Page) {}

  async open() {
    await this.page.goto('/')
    await expect(this.catalogue).toBeVisible()
  }

  row(isbn: string): Locator {
    return this.page.getByTestId(`book-row-${isbn}`)
  }

  status(isbn: string): Locator {
    return this.page.getByTestId(`book-status-${isbn}`)
  }

  available(isbn: string): Locator {
    return this.page.getByTestId(`book-available-${isbn}`)
  }

  async createBook(book: { title: string; isbn: string; author?: string; category: string; quantity: number }) {
    await this.page.getByLabel('書名').fill(book.title)
    await this.page.getByLabel('ISBN').fill(book.isbn)
    if (book.author) await this.page.getByLabel('作者').fill(book.author)
    await this.page.getByLabel('分類').selectOption(book.category)
    await this.page.getByLabel('數量').fill(String(book.quantity))
    await this.page.getByRole('button', { name: '新增書籍', exact: true }).click()
  }

  async borrow(isbn: string, readerId: string, dueDate?: string) {
    await this.page.getByTestId('borrow-reader-id').fill(readerId)
    await this.page.getByTestId('borrow-isbn').fill(isbn)
    if (dueDate) await this.page.getByTestId('borrow-due-date').fill(dueDate)
    await this.page.getByRole('button', { name: '確認借出', exact: true }).click()
  }

  async selectReturnTab() {
    await this.page.getByRole('tab', { name: '還書', exact: true }).click()
    await expect(this.page.getByTestId('return-form')).toBeVisible()
  }

  async returnBook(isbn: string, readerId?: string) {
    await this.page.getByTestId('return-isbn').fill(isbn)
    if (readerId) await this.page.getByTestId('return-reader-id').fill(readerId)
    await this.page.getByRole('button', { name: '確認歸還', exact: true }).click()
  }
}
