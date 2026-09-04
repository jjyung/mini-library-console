import createClient from 'openapi-fetch'

import type { components, paths } from './generated/schema'

export type BusinessCode = '00000' | 'A0000' | 'B0000' | 'C0000'
export type BookDTO = components['schemas']['BookDTO']
export type LoanDTO = components['schemas']['LoanDTO']
export type PostBooksRequestDTO = components['schemas']['PostBooksRequestDTO']
export type PostLoansBorrowRequestDTO = components['schemas']['PostLoansBorrowRequestDTO']
export type PostLoansReturnRequestDTO = components['schemas']['PostLoansReturnRequestDTO']
export type ErrorDetailDTO = components['schemas']['ErrorDetailDTO']

type ErrorBody = components['schemas']['ErrorResponseDTO']
type SuccessEnvelope<T> = {
  code: '00000'
  message: string
  traceId: string
  data: T
}

export class LibraryApiError extends Error {
  readonly code: Exclude<BusinessCode, '00000'>
  readonly traceId: string
  readonly details: ErrorDetailDTO[]
  readonly retryable: boolean

  constructor(
    code: Exclude<BusinessCode, '00000'>,
    message: string,
    traceId = '',
    details: ErrorDetailDTO[] = [],
  ) {
    super(message)
    this.name = 'LibraryApiError'
    this.code = code
    this.traceId = traceId
    this.details = details
    this.retryable = code === 'B0000' || code === 'C0000'
  }
}

export interface LibraryApi {
  getBooks(): Promise<BookDTO[]>
  createBook(payload: PostBooksRequestDTO): Promise<BookDTO>
  borrowBook(payload: PostLoansBorrowRequestDTO): Promise<LoanDTO>
  returnBook(payload: PostLoansReturnRequestDTO): Promise<LoanDTO>
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

function isBusinessErrorCode(value: unknown): value is Exclude<BusinessCode, '00000'> {
  return value === 'A0000' || value === 'B0000' || value === 'C0000'
}

function isSuccessEnvelope<T>(value: unknown): value is SuccessEnvelope<T> {
  if (!value || typeof value !== 'object') return false
  const candidate = value as Partial<SuccessEnvelope<T>>
  return candidate.code === '00000' && typeof candidate.message === 'string' && typeof candidate.traceId === 'string'
}

function toLibraryApiError(value: unknown, fallbackMessage: string): LibraryApiError {
  if (value && typeof value === 'object') {
    const body = value as Partial<ErrorBody>
    if (isBusinessErrorCode(body.code)) {
      return new LibraryApiError(
        body.code,
        typeof body.message === 'string' ? body.message : fallbackMessage,
        typeof body.traceId === 'string' ? body.traceId : '',
        Array.isArray(body.details) ? body.details : [],
      )
    }
  }

  return new LibraryApiError('B0000', fallbackMessage)
}

async function request<T>(
  operation: () => Promise<{ data?: unknown; error?: unknown }>,
  fallbackMessage: string,
): Promise<T> {
  try {
    const result = await operation()
    if (isSuccessEnvelope<T>(result.data)) return result.data.data
    throw toLibraryApiError(result.error ?? result.data, fallbackMessage)
  } catch (error) {
    if (error instanceof LibraryApiError) throw error
    throw new LibraryApiError('B0000', fallbackMessage)
  }
}

export function createLibraryApi(
  fetchImplementation: typeof globalThis.fetch = globalThis.fetch,
  baseUrl = apiBaseUrl,
): LibraryApi {
  const client = createClient<paths>({ baseUrl, fetch: fetchImplementation })

  return {
    getBooks(): Promise<BookDTO[]> {
      return request(
        async () => client.GET('/api/books'),
        '系統暫時無法取得館藏，請稍後重試。',
      )
    },

    createBook(payload: PostBooksRequestDTO): Promise<BookDTO> {
      return request(
        async () => client.POST('/api/books', { body: payload }),
        '系統暫時無法新增書籍，請稍後重試。',
      )
    },

    borrowBook(payload: PostLoansBorrowRequestDTO): Promise<LoanDTO> {
      return request(
        async () => client.POST('/api/loans/borrow', { body: payload }),
        '系統暫時無法完成借出，請稍後重試。',
      )
    },

    returnBook(payload: PostLoansReturnRequestDTO): Promise<LoanDTO> {
      return request(
        async () => client.POST('/api/loans/return', { body: payload }),
        '系統暫時無法完成歸還，請稍後重試。',
      )
    },
  }
}

export const libraryApi = createLibraryApi()
