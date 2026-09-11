import type { components } from './generated/schema'
import { apiClient, createRequestKey } from './client'
import { getBusinessErrorMessage } from './errorMessages'

type BookSummary = components['schemas']['BookSummaryResponseDTO']
type BookDetail = components['schemas']['BookDetailResponseDTO']
type LoanSummary = components['schemas']['LoanSummaryResponseDTO']
type BooksPage = components['schemas']['GetBooksDataResponseDTO']
type CreateBookRequest = components['schemas']['PostBooksRequestDTO']
type BorrowRequest = components['schemas']['PostBookBorrowsRequestDTO']
type ReturnRequest = components['schemas']['PostBookReturnsRequestDTO']

export type LibraryApiErrorDetail = components['schemas']['ErrorDetailResponseDTO']

export class LibraryApiError extends Error {
  readonly code: string
  readonly details: LibraryApiErrorDetail[]
  readonly retryable: boolean

  constructor(
    code: string,
    message: string,
    details: LibraryApiErrorDetail[] = [],
    retryable = false,
  ) {
    super(getBusinessErrorMessage(code, message))
    this.name = 'LibraryApiError'
    this.code = code
    this.details = details
    this.retryable = retryable
  }
}

type ErrorPayload = components['schemas']['ErrorResponseDTO']

const asError = (payload: unknown, status: number): LibraryApiError => {
  const candidate = payload as Partial<ErrorPayload> | null
  const code = typeof candidate?.code === 'string' ? candidate.code : status >= 500 ? 'B0000' : 'A0000'
  const message = typeof candidate?.message === 'string' ? candidate.message : undefined
  const details = Array.isArray(candidate?.details) ? candidate.details : []
  return new LibraryApiError(code, message ?? '', details, status >= 500)
}

const handleResponse = <T>(data: T | undefined, error: unknown, response: Response): T => {
  if (!response.ok || error) {
    throw asError(error, response.status)
  }

  if (data === undefined) {
    throw new LibraryApiError('B0000', 'API 未回傳有效資料。', [], true)
  }

  return data
}

export const libraryApi = {
  async listBooks(params: { query?: string; page: number; pageSize: number }): Promise<BooksPage> {
    try {
      const result = await apiClient.GET('/api/books', {
        params: {
          query: {
            query: params.query || undefined,
            page: params.page,
            pageSize: params.pageSize,
          },
        },
      })
      const payload = handleResponse(result.data, result.error, result.response)
      return payload.data
    } catch (error) {
      if (error instanceof LibraryApiError) throw error
      throw new LibraryApiError('B0000', '無法連線至館藏服務。', [], true)
    }
  },

  async createBook(request: CreateBookRequest, idempotencyKey = createRequestKey()): Promise<BookDetail> {
    try {
      const result = await apiClient.POST('/api/books', {
        params: { header: { 'Idempotency-Key': idempotencyKey } },
        body: request,
      })
      return handleResponse(result.data, result.error, result.response).data
    } catch (error) {
      if (error instanceof LibraryApiError) throw error
      throw new LibraryApiError('B0000', '無法連線至館藏服務。', [], true)
    }
  },

  async borrowBook(
    bookId: string,
    request: BorrowRequest,
    idempotencyKey = createRequestKey(),
  ): Promise<{ book: BookDetail; loan: LoanSummary }> {
    try {
      const result = await apiClient.POST('/api/books/{bookId}/borrow', {
        params: { path: { bookId }, header: { 'Idempotency-Key': idempotencyKey } },
        body: request,
      })
      return handleResponse(result.data, result.error, result.response).data
    } catch (error) {
      if (error instanceof LibraryApiError) throw error
      throw new LibraryApiError('B0000', '無法連線至館藏服務。', [], true)
    }
  },

  async returnBook(
    bookId: string,
    request: ReturnRequest,
    idempotencyKey = createRequestKey(),
  ): Promise<{ book: BookDetail; loan: LoanSummary }> {
    try {
      const result = await apiClient.POST('/api/books/{bookId}/return', {
        params: { path: { bookId }, header: { 'Idempotency-Key': idempotencyKey } },
        body: request,
      })
      return handleResponse(result.data, result.error, result.response).data
    } catch (error) {
      if (error instanceof LibraryApiError) throw error
      throw new LibraryApiError('B0000', '無法連線至館藏服務。', [], true)
    }
  },
}

export type { BookSummary, BookDetail, BooksPage, CreateBookRequest, BorrowRequest, ReturnRequest, LoanSummary }
