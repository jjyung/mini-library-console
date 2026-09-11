import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from './client'
import { LibraryApiError, libraryApi } from './libraryApi'

vi.mock('./client', () => ({
  apiClient: { GET: vi.fn(), POST: vi.fn() },
  createRequestKey: vi.fn(() => 'test-request-key'),
}))

const mockedClient = vi.mocked(apiClient)

const response = (status = 200) => new Response(null, { status })

describe('library API adapter', () => {
  beforeEach(() => vi.clearAllMocks())

  it('[FR-004] requests the catalogue with typed pagination parameters', async () => {
    vi.mocked(mockedClient.GET).mockResolvedValue({
      data: { code: '00000', message: 'ok', traceId: 'trace', data: { items: [], page: 2, pageSize: 10, totalPages: 2, total: 11 } },
      response: response(),
    } as never)

    await expect(libraryApi.listBooks({ query: 'clean', page: 2, pageSize: 10 })).resolves.toMatchObject({ page: 2, total: 11 })
    expect(mockedClient.GET).toHaveBeenCalledWith('/api/books', {
      params: { query: { query: 'clean', page: 2, pageSize: 10 } },
    })
  })

  it('[FR-005] maps API business errors without optimistic data changes', async () => {
    vi.mocked(mockedClient.GET).mockResolvedValue({
      error: { code: 'A0000', message: '頁碼無效', traceId: 'trace', details: [{ field: 'page', reason: 'must be positive' }] },
      response: response(400),
    } as never)

    const request = libraryApi.listBooks({ page: 0, pageSize: 10 })
    await expect(request).rejects.toMatchObject({ code: 'A0000', details: [{ field: 'page' }] })
  })

  it('[AC-001] creates a book through the generated POST contract', async () => {
    vi.mocked(mockedClient.POST).mockResolvedValue({
      data: { code: '00000', message: 'created', traceId: 'trace', data: { bookId: 'book-1', title: 'Clean Code', isbn: 'isbn-1', category: 'technology', status: 'available', availableCount: 1, totalCount: 1 } },
      response: response(),
    } as never)

    await libraryApi.createBook({ title: 'Clean Code', isbn: 'isbn-1', category: 'technology', quantity: 1, isActive: true }, 'key-1')
    expect(mockedClient.POST).toHaveBeenCalledWith('/api/books', expect.objectContaining({ params: { header: { 'Idempotency-Key': 'key-1' } } }))
  })

  it('[AC-003] borrows one book and returns the transaction payload', async () => {
    vi.mocked(mockedClient.POST).mockResolvedValue({
      data: { code: '00000', message: 'borrowed', traceId: 'trace', data: { book: {}, loan: {} } },
      response: response(),
    } as never)

    await expect(libraryApi.borrowBook('book-1', { readerId: 'reader-1' }, 'key-2')).resolves.toHaveProperty('loan')
    expect(mockedClient.POST).toHaveBeenCalledWith('/api/books/{bookId}/borrow', expect.objectContaining({ params: { path: { bookId: 'book-1' }, header: { 'Idempotency-Key': 'key-2' } } }))
  })

  it('[AC-006] returns one loan through the generated POST contract', async () => {
    vi.mocked(mockedClient.POST).mockResolvedValue({
      data: { code: '00000', message: 'returned', traceId: 'trace', data: { book: {}, loan: {} } },
      response: response(),
    } as never)

    await expect(libraryApi.returnBook('book-1', { loanId: 'loan-1' }, 'key-3')).resolves.toHaveProperty('book')
  })

  it('[AC-002] converts a connection failure into a retryable B0000 error', async () => {
    vi.mocked(mockedClient.POST).mockRejectedValue(new Error('offline'))
    await expect(libraryApi.createBook({ title: 'x', isbn: 'x', category: 'art', quantity: 1, isActive: true })).rejects.toEqual(
      expect.objectContaining({ code: 'B0000', retryable: true }),
    )
  })

  it('[AC-005] preserves a server business error code for borrow failure', async () => {
    vi.mocked(mockedClient.POST).mockResolvedValue({
      error: { code: 'A0000', message: '無可借複本', traceId: 'trace' },
      response: response(409),
    } as never)

    await expect(libraryApi.borrowBook('book-1', { readerId: 'reader-1' })).rejects.toBeInstanceOf(LibraryApiError)
  })

  it('[FR-004] handles a catalogue connection failure as retryable', async () => {
    vi.mocked(mockedClient.GET).mockRejectedValue(new Error('offline'))
    await expect(libraryApi.listBooks({ page: 1, pageSize: 10 })).rejects.toMatchObject({ code: 'B0000', retryable: true })
  })

  it('[AC-003] handles a borrow connection failure as retryable', async () => {
    vi.mocked(mockedClient.POST).mockRejectedValue(new Error('offline'))
    await expect(libraryApi.borrowBook('book-1', { readerId: 'reader-1' })).rejects.toMatchObject({ code: 'B0000', retryable: true })
  })

  it('[AC-006] handles a return connection failure as retryable', async () => {
    vi.mocked(mockedClient.POST).mockRejectedValue(new Error('offline'))
    await expect(libraryApi.returnBook('book-1', { loanId: 'loan-1' })).rejects.toMatchObject({ code: 'B0000', retryable: true })
  })

  it('[AC-009] handles an error response without a payload and an empty success payload', async () => {
    vi.mocked(mockedClient.GET).mockResolvedValue({ error: undefined, response: response(503) } as never)
    await expect(libraryApi.listBooks({ page: 1, pageSize: 10 })).rejects.toMatchObject({ code: 'B0000', retryable: true })

    vi.mocked(mockedClient.GET).mockResolvedValue({ data: undefined, response: response() } as never)
    await expect(libraryApi.listBooks({ page: 1, pageSize: 10 })).rejects.toMatchObject({ code: 'B0000', retryable: true })
  })
})
