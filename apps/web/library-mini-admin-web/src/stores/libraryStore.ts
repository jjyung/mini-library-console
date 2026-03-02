import { computed, reactive } from 'vue'
import { ApiClientError } from '@/services/apiClient'
import { libraryService } from '@/services/libraryService'
import type { BookDTO, BorrowRecordDTO } from '@/types/api'

interface LibraryState {
  books: BookDTO[]
  borrowRecords: BorrowRecordDTO[]
  loading: boolean
  errorMessage: string
}

const state = reactive<LibraryState>({
  books: [],
  borrowRecords: [],
  loading: false,
  errorMessage: '',
})

function mapErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    if (error.code === 'A0000') {
      return `操作失敗：${error.message}`
    }
    if (error.code === 'B0000') {
      return '系統忙碌中，請稍後再試。'
    }
    if (error.code === 'C0000') {
      return '外部服務異常。'
    }
    return error.message
  }
  return '未知錯誤，請稍後再試。'
}

function upsertBook(updatedBook: BookDTO): void {
  const targetIndex = state.books.findIndex((book) => book.id === updatedBook.id)
  if (targetIndex === -1) {
    state.books.push(updatedBook)
    state.books.sort((bookA, bookB) => bookA.id - bookB.id)
    return
  }
  state.books[targetIndex] = updatedBook
}

function upsertBorrowRecord(updatedBorrowRecord: BorrowRecordDTO): void {
  const targetIndex = state.borrowRecords.findIndex((record) => record.id === updatedBorrowRecord.id)
  if (targetIndex === -1) {
    state.borrowRecords.push(updatedBorrowRecord)
    state.borrowRecords.sort((recordA, recordB) => recordA.id - recordB.id)
    return
  }
  state.borrowRecords[targetIndex] = updatedBorrowRecord
}

export function useLibraryStore() {
  const activeBorrowRecords = computed(() =>
    state.borrowRecords.filter((borrowRecord) => borrowRecord.status === 'BORROWED'),
  )

  async function loadBooks(): Promise<void> {
    state.loading = true
    state.errorMessage = ''
    try {
      const responseBodyDTO = await libraryService.getBooks()
      state.books = responseBodyDTO.books
    } catch (error) {
      state.errorMessage = mapErrorMessage(error)
    } finally {
      state.loading = false
    }
  }

  async function createBook(title: string, author: string, totalQuantity: number): Promise<void> {
    state.errorMessage = ''
    try {
      const responseBodyDTO = await libraryService.createBook({
        title,
        author,
        totalQuantity,
      })
      upsertBook(responseBodyDTO.book)
    } catch (error) {
      state.errorMessage = mapErrorMessage(error)
      throw error
    }
  }

  async function borrowBook(bookId: number, borrowerName: string): Promise<void> {
    state.errorMessage = ''
    try {
      const responseBodyDTO = await libraryService.borrowBook({
        bookId,
        borrowerName,
      })
      upsertBorrowRecord(responseBodyDTO.borrowRecord)
      upsertBook(responseBodyDTO.book)
    } catch (error) {
      state.errorMessage = mapErrorMessage(error)
      throw error
    }
  }

  async function returnBook(borrowRecordId: number): Promise<void> {
    state.errorMessage = ''
    try {
      const responseBodyDTO = await libraryService.returnBook(borrowRecordId)
      upsertBorrowRecord(responseBodyDTO.borrowRecord)
      upsertBook(responseBodyDTO.book)
    } catch (error) {
      state.errorMessage = mapErrorMessage(error)
      throw error
    }
  }

  return {
    state,
    activeBorrowRecords,
    loadBooks,
    createBook,
    borrowBook,
    returnBook,
  }
}
