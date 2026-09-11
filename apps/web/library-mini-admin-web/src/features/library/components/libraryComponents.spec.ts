import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import AddBookForm from './AddBookForm.vue'
import BookTable from './BookTable.vue'
import TransactionCard from './TransactionCard.vue'

const book = {
  bookId: 'book-1',
  title: 'Clean Code',
  isbn: '978-0-13-235088-4',
  author: 'Robert C. Martin',
  category: 'technology',
  status: 'available' as const,
  availableCount: 2,
  totalCount: 2,
}

const baseTableProps = {
  books: [book],
  page: 1,
  pageSize: 10,
  totalPages: 2,
  total: 11,
  isLoading: false,
  hasPreviousPage: false,
  hasNextPage: true,
}

describe('library console components', () => {
  it('[FR-UI-001] renders the designed catalogue and stable locator contract', () => {
    const wrapper = mount(BookTable, { props: baseTableProps })

    expect(wrapper.get('[data-testid="library-book-table"]').isVisible()).toBe(true)
    expect(wrapper.get('[data-testid="library-book-row"]').attributes('data-book-id')).toBe('book-1')
    expect(wrapper.text()).toContain('Clean Code')
    expect(wrapper.text()).toContain('可借閱')
  })

  it('[AC-UI-001] exposes accessible transaction tabs and form locators', () => {
    const wrapper = mount(TransactionCard, {
      props: {
        activeTab: 'borrow',
        isSubmitting: false,
        borrowForm: { bookId: 'book-1', readerId: '', dueDate: '' },
        returnForm: { bookId: '', loanId: '', readerId: '' },
      },
    })

    expect(wrapper.get('[data-testid="library-borrow-tab"]').attributes('aria-selected')).toBe('true')
    expect(wrapper.get('[data-testid="library-return-tab"]').attributes('aria-selected')).toBe('false')
    expect(wrapper.get('[data-testid="library-borrow-submit"]').isVisible()).toBe(true)
  })

  it('[AC-004] keeps borrowing available while a copy remains and emits the selected book', async () => {
    const wrapper = mount(BookTable, { props: baseTableProps })
    await wrapper.get('.text-button').trigger('click')

    expect(wrapper.emitted('borrow')?.[0]).toEqual([book])
    expect(wrapper.get('.text-button').text()).toBe('借出')
  })

  it('[AC-008] renders a borrowed multi-copy book with its return action', () => {
    const borrowedBook = {
      ...book,
      status: 'borrowed' as const,
      availableCount: 1,
      activeLoan: { loanId: 'loan-1', bookId: 'book-1', readerId: 'reader-1', borrowedAt: '2026-09-09T00:00:00Z', status: 'ACTIVE' as const },
    }
    const wrapper = mount(BookTable, { props: { ...baseTableProps, books: [borrowedBook] } })

    expect(wrapper.text()).toContain('1 / 2')
    expect(wrapper.text()).toContain('借閱中')
    expect(wrapper.text()).toContain('歸還')
  })

  it('[AC-002] keeps the add-book form fields available for correction', async () => {
    const form = { title: '', isbn: '', author: '', category: 'literature' as const, quantity: 1, isActive: true }
    const wrapper = mount(AddBookForm, { props: { form, isSubmitting: false } })
    const title = wrapper.get('[data-testid="library-book-title-input"]')
    await title.setValue('A title to correct')

    expect((title.element as HTMLInputElement).value).toBe('A title to correct')
    expect(wrapper.get('[data-testid="library-add-book-submit"]').attributes('disabled')).toBeUndefined()
  })
})
