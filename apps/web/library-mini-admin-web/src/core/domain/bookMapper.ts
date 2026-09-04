import type { BookDTO } from '@/core/api/libraryApi'

import type { Book, BookStatus } from './libraryTypes'

const statusMap: Record<BookDTO['status'], BookStatus> = {
  AVAILABLE: 'available',
  BORROWED: 'borrowed',
  INACTIVE: 'inactive',
}

export function mapBookDto(book: BookDTO): Book {
  return {
    bookId: book.bookId,
    title: book.title,
    isbn: book.isbn,
    author: book.author,
    category: book.category,
    status: statusMap[book.status],
    availableCount: book.availableCount,
    totalCount: book.totalCount,
    isActive: book.isActive,
  }
}

