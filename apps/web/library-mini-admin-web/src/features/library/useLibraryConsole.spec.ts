import { nextTick } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import type { libraryApi } from '@/core/api/libraryApi'
import { LibraryApiError } from '@/core/api/libraryApi'

import type { Book } from './libraryTypes'
import { useLibraryConsole } from './useLibraryConsole'

const page = {
  items: [{ bookId: 'book-1', title: 'Clean Code', isbn: 'isbn-1', category: 'technology', status: 'available' as const, availableCount: 2, totalCount: 2 }],
  page: 1,
  pageSize: 10,
  totalPages: 1,
  total: 1,
}
const sampleBook = page.items[0]!

const createApi = () => ({
  listBooks: vi.fn().mockResolvedValue(page),
  createBook: vi.fn().mockResolvedValue(sampleBook),
  borrowBook: vi.fn().mockResolvedValue({ book: sampleBook, loan: { loanId: 'loan-1' } }),
  returnBook: vi.fn().mockResolvedValue({ book: sampleBook, loan: { loanId: 'loan-1' } }),
}) as unknown as typeof libraryApi

describe('library console state', () => {
  beforeEach(() => vi.clearAllMocks())

  it('[FR-001] creates a book and reloads the catalogue', async () => {
    const api = createApi()
    const state = useLibraryConsole(api)
    state.createForm.title = ' Clean Code '
    state.createForm.isbn = ' isbn-1 '
    await state.createBook()

    expect(api.createBook).toHaveBeenCalledWith(expect.objectContaining({ title: 'Clean Code', isbn: 'isbn-1' }), expect.any(String))
    expect(state.feedback.value?.kind).toBe('success')
    expect(api.listBooks).toHaveBeenCalled()
  })

  it('[FR-002] selects a book and submits a borrow without changing data optimistically', async () => {
    const api = createApi()
    const state = useLibraryConsole(api)
    state.selectBorrow(sampleBook)
    state.borrowForm.readerId = 'reader-1'
    await state.borrowBook()

    expect(api.borrowBook).toHaveBeenCalledWith('book-1', { readerId: 'reader-1', dueDate: undefined }, expect.any(String))
    expect(state.feedback.value?.message).toContain('借出成功')
  })

  it('[FR-003] selects an active loan and submits a return', async () => {
    const api = createApi()
    const state = useLibraryConsole(api)
    const borrowedBook: Book = {
      ...sampleBook,
      status: 'borrowed',
      availableCount: 0,
      activeLoan: { loanId: 'loan-1', bookId: 'book-1', readerId: 'reader-1', borrowedAt: '2026-09-09T00:00:00Z', status: 'ACTIVE' },
    }
    state.selectReturn(borrowedBook)
    await state.returnBook()

    expect(api.returnBook).toHaveBeenCalledWith('book-1', { loanId: 'loan-1', readerId: 'reader-1' }, expect.any(String))
    expect(state.feedback.value?.message).toContain('歸還成功')
  })

  it('[FR-004] searches from page one and exposes pagination controls', async () => {
    const api = createApi()
    const state = useLibraryConsole(api)
    state.query.value = 'clean'
    await state.search()

    expect(api.listBooks).toHaveBeenCalledWith({ query: 'clean', page: 1, pageSize: 10 })
    expect(state.hasPreviousPage.value).toBe(false)
    expect(state.hasNextPage.value).toBe(false)
  })

  it('[FR-005] preserves input and maps a failed mutation to an error feedback', async () => {
    const api = createApi()
    vi.mocked(api.createBook).mockRejectedValue(new Error('server down'))
    const state = useLibraryConsole(api)
    state.createForm.title = 'Keep this value'
    await state.createBook()
    await nextTick()

    expect(state.createForm.title).toBe('Keep this value')
    expect(state.feedback.value).toEqual({ kind: 'error', message: '系統暫時無法完成操作，請稍後再試。' })
  })

  it('[AC-007] leaves return input available when the return request fails', async () => {
    const api = createApi()
    vi.mocked(api.returnBook).mockRejectedValue(new Error('not found'))
    const state = useLibraryConsole(api)
    state.returnForm.bookId = 'book-1'
    state.returnForm.loanId = 'loan-unknown'
    await state.returnBook()

    expect(state.returnForm.loanId).toBe('loan-unknown')
    expect(state.feedback.value?.kind).toBe('error')
  })

  it('[FR-004] ignores an invalid page transition while loading', async () => {
    const api = createApi()
    const state = useLibraryConsole(api)
    state.totalPages.value = 2
    state.isLoading.value = true
    await state.goToPage(2)
    expect(api.listBooks).not.toHaveBeenCalled()

    state.isLoading.value = false
    await state.goToPage(3)
    expect(api.listBooks).not.toHaveBeenCalled()
  })

  it('[FR-004] changes page size from the pagination control and resets to page one', async () => {
    const api = createApi()
    vi.mocked(api.listBooks).mockResolvedValue({ ...page, pageSize: 20 })
    const state = useLibraryConsole(api)
    await state.changePageSize(20)

    expect(api.listBooks).toHaveBeenCalledWith({ query: undefined, page: 1, pageSize: 20 })
    expect(state.pageSize.value).toBe(20)
  })

  it('[AC-009] shows the business error message from a failed catalogue read', async () => {
    const api = createApi()
    vi.mocked(api.listBooks).mockRejectedValue(new LibraryApiError('A0000', '資料已更新，請重新載入。'))
    const state = useLibraryConsole(api)
    await state.loadBooks()
    expect(state.feedback.value).toEqual({ kind: 'error', message: '資料已更新，請重新載入。' })
  })
})
