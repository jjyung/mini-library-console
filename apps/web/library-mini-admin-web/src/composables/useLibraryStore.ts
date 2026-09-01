import { reactive, ref } from 'vue'
import { createBook, checkoutBook, getBooks, returnBook } from '@/services/libraryApi'
import { mapBusinessError, mapSuccess } from '@/services/businessErrorMapper'
import type { Feedback } from '@/types/api'
import type { Book, CheckoutBookRequest, CreateBookRequest, ReturnBookRequest } from '@/types/library'

type OperationName = 'addBook' | 'borrow' | 'return'
type OperationFeedback = Record<OperationName, Feedback | null>

export function useLibraryStore() {
  const books = ref<Book[]>([])
  const isLoading = ref(false)
  const listError = ref<Feedback | null>(null)
  const operationFeedback = reactive<OperationFeedback>({
    addBook: null,
    borrow: null,
    return: null,
  })

  async function loadBooks(): Promise<void> {
    isLoading.value = true
    listError.value = null

    try {
      const response = await getBooks()
      books.value = response.data
    } catch (error: unknown) {
      listError.value = mapBusinessError(error)
    } finally {
      isLoading.value = false
    }
  }

  async function addBook(data: CreateBookRequest): Promise<boolean> {
    operationFeedback.addBook = null

    try {
      const response = await createBook(data)
      operationFeedback.addBook = mapSuccess(response.message || '書籍新增成功', response.traceId)
      await loadBooks()
      return true
    } catch (error: unknown) {
      operationFeedback.addBook = mapBusinessError(error)
      return false
    }
  }

  async function borrowBook(data: CheckoutBookRequest): Promise<boolean> {
    operationFeedback.borrow = null

    try {
      const response = await checkoutBook(data)
      operationFeedback.borrow = mapSuccess(response.message || '借書成功', response.traceId)
      await loadBooks()
      return true
    } catch (error: unknown) {
      operationFeedback.borrow = mapBusinessError(error)
      return false
    }
  }

  async function returnBookCopy(data: ReturnBookRequest): Promise<boolean> {
    operationFeedback.return = null

    try {
      const response = await returnBook(data)
      operationFeedback.return = mapSuccess(response.message || '還書成功', response.traceId)
      await loadBooks()
      return true
    } catch (error: unknown) {
      operationFeedback.return = mapBusinessError(error)
      return false
    }
  }

  return {
    books,
    isLoading,
    listError,
    operationFeedback,
    loadBooks,
    addBook,
    borrowBook,
    returnBook: returnBookCopy,
  }
}
