import { post, request } from './apiClient'
import type { ApiEnvelope } from '@/types/api'
import type {
  Book,
  CheckoutBookRequest,
  CreateBookRequest,
  ReturnBookRequest,
} from '@/types/library'

export type CheckoutResult = {
  loanId: string
  bookId: string
  isbn: string
  readerId: string
  dueDate?: string | null
  loanedAt: string
  returned: false
}

export type ReturnResult = {
  loanId: string
  book: Book
  returnedAt: string
}

export function getBooks(): Promise<ApiEnvelope<Book[]>> {
  return request<Book[]>('/books')
}

export function createBook(data: CreateBookRequest): Promise<ApiEnvelope<Book>> {
  return post<Book>('/books', data)
}

export function checkoutBook(data: CheckoutBookRequest): Promise<ApiEnvelope<CheckoutResult>> {
  return post<CheckoutResult>('/loans', data)
}

export function returnBook(data: ReturnBookRequest): Promise<ApiEnvelope<ReturnResult>> {
  return post<ReturnResult>('/loans/returns', data)
}
