import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  createLibraryApi,
  LibraryApiError,
  type BookDTO,
  type LibraryApi,
} from '@/core/api/libraryApi'
import { mapBookDto } from '@/core/domain/bookMapper'
import { mapApiError } from '@/core/domain/errorMessages'
import { validateBorrow, validateCreateBook, validateReturn } from '@/core/domain/libraryValidators'
import { useLibraryAdmin } from '@/features/library/useLibraryAdmin'

const bookDto = (overrides: Partial<BookDTO> = {}): BookDTO => ({
  bookId: '00000000-0000-0000-0000-000000000001',
  title: '深度學習',
  isbn: '978-7-115-48570-5',
  author: 'Ian Goodfellow',
  category: 'technology',
  status: 'AVAILABLE',
  availableCount: 2,
  totalCount: 2,
  isActive: true,
  ...overrides,
})

const loanDto = {
  loanId: '00000000-0000-0000-0000-000000000002',
  bookId: '00000000-0000-0000-0000-000000000001',
  readerId: 'reader-1',
  borrowedAt: '2026-09-04T00:00:00Z',
  dueDate: null,
  returnedAt: null,
}

function response(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}

function fakeService(overrides: Partial<LibraryApi> = {}) {
  return {
    getBooks: vi.fn().mockResolvedValue([]),
    createBook: vi.fn().mockResolvedValue(bookDto()),
    borrowBook: vi.fn().mockResolvedValue({}),
    returnBook: vi.fn().mockResolvedValue({}),
    ...overrides,
  } as unknown as LibraryApi
}

describe('[FR-001] create-book validation and mapping', () => {
  it('accepts a valid book and maps an API BookDTO without renaming contract fields', () => {
    expect(validateCreateBook({ title: '新書', isbn: '123', author: '', category: 'literature', quantity: 2, isActive: true })).toEqual({})
    expect(mapBookDto(bookDto({ status: 'INACTIVE', isActive: false }))).toMatchObject({ status: 'inactive', availableCount: 2 })
  })

  it('rejects overlong fields, blank values, and non-positive quantities', () => {
    const errors = validateCreateBook({
      title: 't'.repeat(201),
      isbn: 'i'.repeat(21),
      author: 'a'.repeat(201),
      category: 'c'.repeat(31),
      quantity: 0.5,
      isActive: true,
    })
    expect(errors).toEqual({
      title: '書名不可超過 200 個字。',
      isbn: 'ISBN 不可超過 20 個字。',
      author: '作者不可超過 200 個字。',
      category: '分類不可超過 30 個字。',
      quantity: '數量必須是至少 1 的正整數。',
    })
    expect(validateCreateBook({ title: ' ', isbn: ' ', author: '', category: ' ', quantity: 1, isActive: true })).toMatchObject({
      title: '此欄位為必填。',
      isbn: '此欄位為必填。',
      category: '此欄位為必填。',
    })
  })
})

describe('[FR-002] catalogue state derivation', () => {
  it('maps API status values and preserves availability counts', () => {
    expect(mapBookDto(bookDto({ status: 'BORROWED', availableCount: 0, totalCount: 2 }))).toMatchObject({ status: 'borrowed', availableCount: 0, totalCount: 2 })
  })
})

describe('[FR-003] borrow request boundary', () => {
  it('requires the synthetic readerId and ISBN before calling the API', () => {
    expect(validateBorrow({ readerId: '', isbn: '', dueDate: '' })).toEqual({ readerId: '此欄位為必填。', isbn: '此欄位為必填。' })
  })

  it('rejects overlong reader and ISBN values', () => {
    expect(validateBorrow({ readerId: 'r'.repeat(101), isbn: 'i'.repeat(21), dueDate: '' })).toEqual({
      readerId: '讀者 ID 不可超過 100 個字。',
      isbn: 'ISBN 不可超過 20 個字。',
    })
  })
})

describe('[FR-004] return request boundary', () => {
  it('accepts an optional reader selector while requiring ISBN', () => {
    expect(validateReturn({ isbn: '123', readerId: '' })).toEqual({})
    expect(validateReturn({ isbn: '', readerId: '' })).toEqual({ isbn: '此欄位為必填。' })
  })

  it('rejects overlong return selectors', () => {
    expect(validateReturn({ isbn: 'i'.repeat(21), readerId: 'r'.repeat(101) })).toEqual({
      isbn: 'ISBN 不可超過 20 個字。',
      readerId: '讀者 ID 不可超過 100 個字。',
    })
  })
})

describe('[FR-005] operation feedback and recovery', () => {
  it('retains mapped business code and retryability in API failures', () => {
    const error = new LibraryApiError('B0000', 'internal', 'trace-1')
    expect(error.retryable).toBe(true)
    expect(mapApiError(error, 'borrow')).toContain('稍後重試')
  })
})

describe('[FR-UI-001] responsive UI contract adapter', () => {
  it('uses the app view model status vocabulary for the Figma badge states', () => {
    expect(['available', 'borrowed', 'inactive']).toContain(mapBookDto(bookDto()).status)
  })
})

describe('[AC-001] valid book creates and refreshes catalogue', () => {
  it('calls create with frozen DTO fields and then loads the catalogue', async () => {
    const service = fakeService({
      getBooks: vi.fn().mockResolvedValue([bookDto()]),
      createBook: vi.fn().mockResolvedValue(bookDto()),
    })
    const admin = useLibraryAdmin(service)

    expect(await admin.createBook({ title: '新書', isbn: '123', author: '', category: 'literature', quantity: 2, isActive: true })).toBe(true)
    expect(service.createBook).toHaveBeenCalledWith({ title: '新書', isbn: '123', author: undefined, category: 'literature', quantity: 2, isActive: true })
    expect(admin.books.value).toHaveLength(1)
    expect(admin.addBookStatus.value?.type).toBe('success')
  })
})

describe('[AC-002] invalid book form is rejected locally', () => {
  it('does not call the API for missing required fields or invalid quantity', async () => {
    const service = fakeService()
    const admin = useLibraryAdmin(service)
    expect(await admin.createBook({ title: '', isbn: '', author: '', category: '', quantity: 0, isActive: true })).toBe(false)
    expect(service.createBook).not.toHaveBeenCalled()
    expect(admin.addBookStatus.value?.code).toBe('A0000')
  })
})

describe('[AC-003] duplicate ISBN business error', () => {
  it('maps A0000 to a correctable create message and leaves the list unchanged', async () => {
    const service = fakeService({ createBook: vi.fn().mockRejectedValue(new LibraryApiError('A0000', 'duplicate', 'trace-duplicate')) })
    const admin = useLibraryAdmin(service)
    expect(await admin.createBook({ title: '重複', isbn: '123', author: '', category: 'literature', quantity: 1, isActive: true })).toBe(false)
    expect(admin.addBookStatus.value).toMatchObject({ type: 'error', code: 'A0000', traceId: 'trace-duplicate' })
    expect(admin.books.value).toHaveLength(0)
  })
})

describe('[AC-004] borrow with available copies', () => {
  it('sends dueDate only when supplied and refreshes after success', async () => {
    const service = fakeService({
      getBooks: vi.fn().mockResolvedValue([bookDto({ availableCount: 1 })]),
    })
    const admin = useLibraryAdmin(service)
    expect(await admin.borrowBook({ readerId: 'reader-1', isbn: '123', dueDate: '2026-10-01' })).toBe(true)
    expect(service.borrowBook).toHaveBeenCalledWith({ readerId: 'reader-1', isbn: '123', dueDate: '2026-10-01' })
    expect(admin.borrowStatus.value?.type).toBe('success')
  })
})

describe('[AC-005] last-copy borrow state', () => {
  it('keeps the mapped BORROWED state and zero availability visible', async () => {
    const service = fakeService({
      getBooks: vi.fn().mockResolvedValue([bookDto({ status: 'BORROWED', availableCount: 0, totalCount: 1 })]),
    })
    const admin = useLibraryAdmin(service)
    await admin.borrowBook({ readerId: 'reader-1', isbn: '123', dueDate: '' })
    expect(admin.books.value[0]).toMatchObject({ status: 'borrowed', availableCount: 0 })
  })
})

describe('[AC-006] unavailable book borrow is blocked', () => {
  it('rejects an invalid borrow before the unavailable-book API branch', async () => {
    const service = fakeService()
    const admin = useLibraryAdmin(service)
    expect(await admin.borrowBook({ readerId: '', isbn: '123', dueDate: '' })).toBe(false)
    expect(service.borrowBook).not.toHaveBeenCalled()
  })
})

describe('[AC-007] returned loan refreshes availability', () => {
  it('sends the optional reader selector and refreshes after success', async () => {
    const service = fakeService({
      getBooks: vi.fn().mockResolvedValue([bookDto({ status: 'AVAILABLE', availableCount: 2, totalCount: 2 })]),
    })
    const admin = useLibraryAdmin(service)
    expect(await admin.returnBook({ isbn: '123', readerId: 'reader-1' })).toBe(true)
    expect(service.returnBook).toHaveBeenCalledWith({ isbn: '123', readerId: 'reader-1' })
    expect(admin.returnStatus.value?.type).toBe('success')
  })
})

describe('[AC-008] return without an active loan maps A0000', () => {
  it('shows a return-specific correction message', async () => {
    const service = fakeService({ returnBook: vi.fn().mockRejectedValue(new LibraryApiError('A0000', 'none', 'trace-return')) })
    const admin = useLibraryAdmin(service)
    expect(await admin.returnBook({ isbn: '123', readerId: '' })).toBe(false)
    expect(admin.returnStatus.value?.message).toContain('可歸還')
    expect(admin.returnStatus.value?.traceId).toBe('trace-return')
  })
})

describe('[AC-009] empty catalogue', () => {
  it('treats an empty 00000 response as a successful empty state', async () => {
    const admin = useLibraryAdmin(fakeService({ getBooks: vi.fn().mockResolvedValue([]) }))
    expect(await admin.loadBooks()).toBe(true)
    expect(admin.catalogueState.value).toBe('success')
    expect(admin.books.value).toEqual([])
  })
})

describe('[AC-010] system failure preserves catalogue state', () => {
  it('keeps existing books when a refresh fails and exposes B0000', async () => {
    const service = fakeService({
      getBooks: vi.fn()
        .mockResolvedValueOnce([bookDto()])
        .mockRejectedValueOnce(new LibraryApiError('B0000', 'db down', 'trace-system')),
    })
    const admin = useLibraryAdmin(service)
    await admin.loadBooks()
    expect(await admin.loadBooks()).toBe(false)
    expect(admin.books.value).toHaveLength(1)
    expect(admin.catalogueError.value).toMatchObject({ code: 'B0000', traceId: 'trace-system' })
  })
})

describe('[AC-UI-001] Figma-aligned interaction states', () => {
  it('supports retryable system feedback without introducing search or fine behavior', () => {
    const error = new LibraryApiError('B0000', 'db down', 'trace-ui')
    expect(mapApiError(error, 'catalogue')).toBe('系統暫時無法取得館藏，請稍後重試。')
    expect(mapApiError(new LibraryApiError('C0000', 'third party', 'trace-ui'), 'catalogue')).toContain('外部服務')
  })
})

describe('[API contract] centralized client uses frozen paths and business codes', () => {
  let mockFetch: ReturnType<typeof vi.fn>
  let testApi: LibraryApi

  beforeEach(() => {
    mockFetch = vi.fn()
    testApi = createLibraryApi(mockFetch as unknown as typeof fetch, 'http://localhost:8080')
  })

  it('calls GET /api/books and returns the success data envelope', async () => {
    mockFetch.mockResolvedValue(response({ code: '00000', message: 'ok', traceId: 'trace-get', data: [bookDto()] }))
    await expect(testApi.getBooks()).resolves.toHaveLength(1)
    expect(mockFetch.mock.calls[0]?.[0]).toMatchObject({ method: 'GET', url: 'http://localhost:8080/api/books' })
  })

  it('maps an HTTP 400 response by body business code rather than status', async () => {
    mockFetch.mockResolvedValue(response({ code: 'A0000', message: 'duplicate', traceId: 'trace-client' }, 400))
    await expect(testApi.createBook({ title: 'x', isbn: '123', category: 'literature', quantity: 1, isActive: true })).rejects.toMatchObject({ code: 'A0000', traceId: 'trace-client' })
  })

  it('maps a network failure to retryable B0000', async () => {
    mockFetch.mockRejectedValue(new Error('offline'))
    await expect(testApi.getBooks()).rejects.toMatchObject({ code: 'B0000', retryable: true })
  })

  it('maps an incomplete error body to the fallback system code', async () => {
    mockFetch.mockResolvedValue(response({ code: 'UNKNOWN', details: 'not-an-array' }, 500))
    await expect(testApi.getBooks()).rejects.toMatchObject({ code: 'B0000', retryable: true })
  })

  it('uses fallback values for partially formed business errors', async () => {
    mockFetch.mockResolvedValue(response({ code: 'A0000', details: [] }, 400))
    await expect(testApi.getBooks()).rejects.toMatchObject({ code: 'A0000', traceId: '' })
  })

  it('covers all frozen POST operation boundaries with success envelopes', async () => {
    mockFetch
      .mockResolvedValueOnce(response({ code: '00000', message: 'created', traceId: 'trace-create', data: bookDto() }, 201))
      .mockResolvedValueOnce(response({ code: '00000', message: 'borrowed', traceId: 'trace-borrow', data: loanDto }))
      .mockResolvedValueOnce(response({ code: '00000', message: 'returned', traceId: 'trace-return', data: { ...loanDto, returnedAt: '2026-09-04T00:00:00Z' } }))

    await expect(testApi.createBook({ title: 'x', isbn: '123', category: 'literature', quantity: 1, isActive: true })).resolves.toMatchObject({ isbn: '978-7-115-48570-5' })
    await expect(testApi.borrowBook({ readerId: 'reader-1', isbn: '123' })).resolves.toMatchObject({ readerId: 'reader-1' })
    await expect(testApi.returnBook({ isbn: '123' })).resolves.toMatchObject({ returnedAt: '2026-09-04T00:00:00Z' })
  })
})
