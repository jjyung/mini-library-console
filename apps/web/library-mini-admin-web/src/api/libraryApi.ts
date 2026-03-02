import { apiClient, type ApiEnvelope } from './client'

export interface BookView {
  bookId: string
  title: string
  isbn: string
  author: string | null
  category: string
  totalQuantity: number
  availableQuantity: number
  shelfStatus: string
  circulationStatus: string
  currentBorrowerReaderId?: string | null
  currentDueDate?: string | null
}

export interface CreateBookRequest {
  title: string
  isbn: string
  author: string
  category: string
  quantity: number
  shelfStatus: string
}

export interface BorrowBookRequest {
  readerId: string
  isbn: string
  dueDate?: string
}

export interface ReturnBookRequest {
  isbn: string
  readerId?: string
  returnedAt?: string
}

interface BooksData {
  books: BookView[]
}

interface CreateBookData {
  book: BookView
}

export function createBook(payload: CreateBookRequest): Promise<ApiEnvelope<CreateBookData>> {
  return apiClient.post<CreateBookRequest, CreateBookData>('/library/books', payload)
}

export function listBooks(shelfStatus?: string): Promise<ApiEnvelope<BooksData>> {
  const query = shelfStatus ? `?shelfStatus=${encodeURIComponent(shelfStatus)}` : ''
  return apiClient.get<BooksData>(`/library/books${query}`)
}

export function searchBooks(keyword: string): Promise<ApiEnvelope<BooksData>> {
  return apiClient.get<BooksData>(`/library/books/search?keyword=${encodeURIComponent(keyword)}`)
}

export function borrowBook(payload: BorrowBookRequest): Promise<ApiEnvelope<Record<string, unknown>>> {
  return apiClient.post<BorrowBookRequest, Record<string, unknown>>('/library/loans', payload)
}

export function returnBook(payload: ReturnBookRequest): Promise<ApiEnvelope<Record<string, unknown>>> {
  return apiClient.post<ReturnBookRequest, Record<string, unknown>>('/library/loans/returns', payload)
}
