export type BookStatus = 'available' | 'borrowed' | 'inactive'

export type Book = {
  bookId: string
  title: string
  isbn: string
  author?: string | null
  category: string
  status: BookStatus
  availableCount: number
  totalCount: number
  isActive: boolean
}

export type CreateBookRequest = {
  title: string
  isbn: string
  author?: string
  category: string
  quantity: number
  isActive: boolean
}

export type CheckoutBookRequest = {
  readerId: string
  isbn: string
  dueDate?: string
}

export type ReturnBookRequest = {
  isbn: string
  readerId?: string
}

export type ErrorDetail = {
  field: string
  reason: string
}

export const categoryOptions = [
  { value: 'literature', label: '文學' },
  { value: 'science', label: '科學' },
  { value: 'technology', label: '科技' },
  { value: 'history', label: '歷史' },
  { value: 'art', label: '藝術' },
  { value: 'philosophy', label: '哲學' },
  { value: 'business', label: '商業' },
  { value: 'education', label: '教育' },
] as const
