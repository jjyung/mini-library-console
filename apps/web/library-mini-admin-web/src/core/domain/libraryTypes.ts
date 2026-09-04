import type { BookDTO, BusinessCode, ErrorDetailDTO, LoanDTO } from '@/core/api/libraryApi'

export type BookStatus = 'available' | 'borrowed' | 'inactive'

export type Book = {
  bookId: BookDTO['bookId']
  title: BookDTO['title']
  isbn: BookDTO['isbn']
  author: BookDTO['author']
  category: BookDTO['category']
  status: BookStatus
  availableCount: BookDTO['availableCount']
  totalCount: BookDTO['totalCount']
  isActive: BookDTO['isActive']
}

export type Feedback = {
  type: 'success' | 'error'
  message: string
  code?: Exclude<BusinessCode, '00000'>
  traceId?: string
}

export type ValidationErrors = Record<string, string>

export type ApiFailure = {
  code: Exclude<BusinessCode, '00000'>
  message: string
  traceId: string
  details: ErrorDetailDTO[]
}

export type BorrowForm = {
  readerId: string
  isbn: string
  dueDate: string
}

export type ReturnForm = {
  isbn: string
  readerId: string
}

export type CreateBookForm = {
  title: string
  isbn: string
  author: string
  category: string
  quantity: number
  isActive: boolean
}

export type UiAction = 'catalogue' | 'create' | 'borrow' | 'return'

export type Loan = LoanDTO
