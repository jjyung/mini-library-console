import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import AddBookForm from '@/components/AddBookForm.vue'
import BookTable from '@/components/BookTable.vue'
import TopBar from '@/components/TopBar.vue'
import TransactionCard from '@/components/TransactionCard.vue'

import type { Book } from '@/core/domain/libraryTypes'

const availableBook: Book = {
  bookId: 'book-1',
  title: '深度學習',
  isbn: '978-7-115-48570-5',
  author: 'Ian Goodfellow',
  category: 'technology',
  status: 'available',
  availableCount: 2,
  totalCount: 2,
  isActive: true,
}

const borrowedBook: Book = {
  ...availableBook,
  bookId: 'book-2',
  isbn: '978-0-13-235088-4',
  status: 'borrowed',
  availableCount: 0,
  totalCount: 1,
}

describe('[FR-001] add book form', () => {
  it('renders required and optional fields and emits contract-shaped values', async () => {
    const wrapper = mount(AddBookForm)
    await wrapper.get('[data-testid="add-book-title"]').setValue('新書')
    await wrapper.get('[data-testid="add-book-isbn"]').setValue('123')
    await wrapper.get('[data-testid="add-book-category"]').setValue('science')
    await wrapper.get('[data-testid="add-book-quantity"]').setValue('3')
    await wrapper.get('[data-testid="add-book-form"]').trigger('submit')
    expect(wrapper.emitted('submit')?.[0]?.[0]).toMatchObject({ title: '新書', isbn: '123', category: 'science', quantity: 3, isActive: true })
  })
})

describe('[FR-002] catalogue table', () => {
  it('renders the contract fields, status badge, counts and actions', () => {
    const wrapper = mount(BookTable, { props: { books: [availableBook] } })
    expect(wrapper.get('[data-testid="book-row-978-7-115-48570-5"]').text()).toContain('深度學習')
    expect(wrapper.get('[data-testid="book-status-978-7-115-48570-5"]').text()).toBe('可借閱')
    expect(wrapper.get('[data-testid="book-available-978-7-115-48570-5"]').text()).toBe('2')
  })
})

describe('[FR-003] borrow transaction', () => {
  it('submits readerId, ISBN and optional dueDate from the borrow tab', async () => {
    const wrapper = mount(TransactionCard)
    await wrapper.get('[data-testid="borrow-reader-id"]').setValue('reader-1')
    await wrapper.get('[data-testid="borrow-isbn"]').setValue('123')
    await wrapper.get('[data-testid="borrow-due-date"]').setValue('2026-10-01')
    await wrapper.get('[data-testid="borrow-form"]').trigger('submit')
    expect(wrapper.emitted('borrow')?.[0]?.[0]).toEqual({ readerId: 'reader-1', isbn: '123', dueDate: '2026-10-01' })
  })
})

describe('[FR-004] return transaction', () => {
  it('switches to the return tab and emits optional readerId', async () => {
    const wrapper = mount(TransactionCard)
    await wrapper.get('[data-testid="return-tab"]').trigger('click')
    await wrapper.get('[data-testid="return-isbn"]').setValue('123')
    await wrapper.get('[data-testid="return-reader-id"]').setValue('reader-1')
    await wrapper.get('[data-testid="return-form"]').trigger('submit')
    expect(wrapper.emitted('return')?.[0]?.[0]).toEqual({ isbn: '123', readerId: 'reader-1' })
  })
})

describe('[FR-005] operation feedback', () => {
  it('renders success and business code feedback inline', () => {
    const wrapper = mount(TransactionCard, { props: { borrowStatus: { type: 'error', code: 'A0000', message: '請修正', traceId: 'trace-1' } } })
    expect(wrapper.get('[data-testid="borrow-status"]').text()).toContain('錯誤碼 A0000')
  })
})

describe('[FR-UI-001] Figma-aligned composed controls', () => {
  it('keeps the top bar brand, search visual and Admin identity', () => {
    const wrapper = mount(TopBar)
    expect(wrapper.get('[data-testid="topbar"]').text()).toContain('Library Mini Admin')
    expect(wrapper.get('[data-testid="topbar-search"]').attributes('placeholder')).toContain('搜尋')
    expect(wrapper.get('[data-testid="admin-identity"]').text()).toContain('Admin')
  })
})

describe('[AC-001] valid add form', () => {
  it('allows an active book with initial quantity', async () => {
    const wrapper = mount(AddBookForm)
    await wrapper.get('[data-testid="add-book-title"]').setValue('有效書籍')
    await wrapper.get('[data-testid="add-book-isbn"]').setValue('isbn-1')
    await wrapper.get('[data-testid="add-book-form"]').trigger('submit')
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })
})

describe('[AC-002] add validation failure', () => {
  it('keeps the form and shows field errors instead of emitting', async () => {
    const wrapper = mount(AddBookForm)
    await wrapper.get('[data-testid="add-book-form"]').trigger('submit')
    expect(wrapper.emitted('submit')).toBeUndefined()
    expect(wrapper.get('[data-testid="add-book-status"]').text()).toContain('請修正')
  })
})

describe('[AC-003] duplicate ISBN feedback', () => {
  it('renders the mapped duplicate response without changing inputs', () => {
    const wrapper = mount(AddBookForm, { props: { status: { type: 'error', code: 'A0000', message: '請確認 ISBN', traceId: 'trace-duplicate' } } })
    expect(wrapper.get('[data-testid="add-book-status"]').text()).toContain('請確認 ISBN')
    expect((wrapper.get('[data-testid="add-book-isbn"]').element as HTMLInputElement).value).toBe('')
  })
})

describe('[AC-004] available-copy borrow', () => {
  it('exposes the borrow submit control in the default tab', () => {
    const wrapper = mount(TransactionCard)
    expect(wrapper.get('[data-testid="borrow-submit"]').attributes('disabled')).toBeUndefined()
  })
})

describe('[AC-005] last-copy disabled state', () => {
  it('disables quick borrow when the table has no available copies', () => {
    const wrapper = mount(BookTable, { props: { books: [borrowedBook] } })
    expect(wrapper.get('[data-testid="quick-borrow-978-0-13-235088-4"]').attributes('disabled')).toBeDefined()
    expect(wrapper.get('[data-testid="book-status-978-0-13-235088-4"]').text()).toBe('已借出')
  })
})

describe('[AC-006] unavailable book actions', () => {
  it('disables quick borrow for an inactive book', () => {
    const inactive = { ...availableBook, status: 'inactive' as const, isActive: false }
    const wrapper = mount(BookTable, { props: { books: [inactive] } })
    expect(wrapper.get('[data-testid="quick-borrow-978-7-115-48570-5"]').attributes('disabled')).toBeDefined()
  })
})

describe('[AC-007] return action state', () => {
  it('enables quick return only when a copy is currently borrowed', () => {
    const wrapper = mount(BookTable, { props: { books: [borrowedBook] } })
    expect(wrapper.get('[data-testid="quick-return-978-0-13-235088-4"]').attributes('disabled')).toBeUndefined()
  })
})

describe('[AC-008] return validation failure', () => {
  it('shows an inline error when ISBN is missing', async () => {
    const wrapper = mount(TransactionCard)
    await wrapper.get('[data-testid="return-tab"]').trigger('click')
    await wrapper.get('[data-testid="return-form"]').trigger('submit')
    expect(wrapper.get('[data-testid="return-status"]').text()).toContain('請填寫 ISBN')
  })
})

describe('[AC-009] empty catalogue', () => {
  it('renders the empty state and does not render row actions', () => {
    const wrapper = mount(BookTable, { props: { books: [] } })
    expect(wrapper.get('[data-testid="catalogue-empty"]').text()).toContain('尚無館藏')
    expect(wrapper.find('[data-testid^="book-row-"]').exists()).toBe(false)
  })
})

describe('[AC-010] system error feedback', () => {
  it('renders retryable status information supplied by the client mapper', () => {
    const wrapper = mount(TransactionCard, { props: { borrowStatus: { type: 'error', code: 'B0000', message: '系統暫時無法完成借出，請稍後重試。', traceId: 'trace-system' } } })
    expect(wrapper.get('[data-testid="borrow-status"]').text()).toContain('trace-system')
  })
})

describe('[AC-UI-001] locator and responsive component states', () => {
  it('preserves all acceptance-critical component locators', () => {
    const topbar = mount(TopBar)
    const transaction = mount(TransactionCard)
    const form = mount(AddBookForm)
    const table = mount(BookTable, { props: { books: [availableBook] } })
    expect(topbar.find('[data-testid="topbar"]').exists()).toBe(true)
    expect(transaction.find('[data-testid="transaction-card"]').exists()).toBe(true)
    expect(form.find('[data-testid="add-book-form"]').exists()).toBe(true)
    expect(table.find('[data-testid="book-table"]').exists()).toBe(true)
  })
})
