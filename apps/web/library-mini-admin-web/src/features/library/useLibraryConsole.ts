import { computed, reactive, ref } from 'vue'

import { createRequestKey } from '@/core/api/client'
import { LibraryApiError, libraryApi } from '@/core/api/libraryApi'

import type { Book, CreateBookForm } from './libraryTypes'

const defaultPageSize = 10

const initialCreateForm = (): CreateBookForm => ({
  title: '',
  isbn: '',
  author: '',
  category: 'literature',
  quantity: 1,
  isActive: true,
})

export const useLibraryConsole = (api = libraryApi) => {
  const books = ref<Book[]>([])
  const query = ref('')
  const page = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)
  const totalPages = ref(1)
  const isLoading = ref(false)
  const isSubmitting = ref(false)
  const activeTab = ref<'borrow' | 'return'>('borrow')
  const feedback = ref<{ kind: 'success' | 'error'; message: string } | null>(null)
  const createForm = reactive<CreateBookForm>(initialCreateForm())
  const borrowForm = reactive({ bookId: '', readerId: '', dueDate: '' })
  const returnForm = reactive({ bookId: '', loanId: '', readerId: '' })

  const hasPreviousPage = computed(() => page.value > 1)
  const hasNextPage = computed(() => page.value < totalPages.value)

  const setError = (error: unknown) => {
    if (error instanceof LibraryApiError) {
      feedback.value = { kind: 'error', message: error.message }
      return
    }
    feedback.value = { kind: 'error', message: '系統暫時無法完成操作，請稍後再試。' }
  }

  const clearFeedback = () => {
    feedback.value = null
  }

  const loadBooks = async (requestedPage = page.value) => {
    isLoading.value = true
    clearFeedback()
    try {
      const data = await api.listBooks({
        query: query.value.trim() || undefined,
        page: requestedPage,
        pageSize: pageSize.value,
      })
      books.value = data.items
      page.value = data.page
      pageSize.value = data.pageSize
      total.value = data.total
      totalPages.value = Math.max(1, data.totalPages)
    } catch (error) {
      setError(error)
    } finally {
      isLoading.value = false
    }
  }

  const search = async () => {
    page.value = 1
    await loadBooks(1)
  }

  const goToPage = async (requestedPage: number) => {
    if (requestedPage < 1 || requestedPage > totalPages.value || isLoading.value) return
    await loadBooks(requestedPage)
  }

  const changePageSize = async (requestedPageSize: number) => {
    if (requestedPageSize < 1 || requestedPageSize > 100 || isLoading.value) return
    pageSize.value = requestedPageSize
    page.value = 1
    await loadBooks(1)
  }

  const resetCreateForm = () => Object.assign(createForm, initialCreateForm())

  const createBook = async () => {
    isSubmitting.value = true
    clearFeedback()
    try {
      await api.createBook(
        {
          ...createForm,
          title: createForm.title.trim(),
          isbn: createForm.isbn.trim(),
          author: createForm.author?.trim() || undefined,
        },
        createRequestKey(),
      )
      resetCreateForm()
      page.value = 1
      await loadBooks(1)
      feedback.value = { kind: 'success', message: '書籍建立成功，館藏已更新。' }
    } catch (error) {
      setError(error)
    } finally {
      isSubmitting.value = false
    }
  }

  const selectBorrow = (book: Book) => {
    activeTab.value = 'borrow'
    borrowForm.bookId = book.bookId
    returnForm.bookId = book.bookId
  }

  const selectReturn = (book: Book) => {
    activeTab.value = 'return'
    returnForm.bookId = book.bookId
    returnForm.loanId = book.activeLoan?.loanId ?? ''
    returnForm.readerId = book.activeLoan?.readerId ?? ''
    borrowForm.bookId = book.bookId
  }

  const borrowBook = async () => {
    isSubmitting.value = true
    clearFeedback()
    try {
      await api.borrowBook(
        borrowForm.bookId,
        { readerId: borrowForm.readerId.trim(), dueDate: borrowForm.dueDate || undefined },
        createRequestKey(),
      )
      borrowForm.readerId = ''
      borrowForm.dueDate = ''
      await loadBooks(page.value)
      feedback.value = { kind: 'success', message: '借出成功，館藏數量已更新。' }
    } catch (error) {
      setError(error)
    } finally {
      isSubmitting.value = false
    }
  }

  const returnBook = async () => {
    isSubmitting.value = true
    clearFeedback()
    try {
      await api.returnBook(
        returnForm.bookId,
        { loanId: returnForm.loanId.trim(), readerId: returnForm.readerId.trim() || undefined },
        createRequestKey(),
      )
      returnForm.loanId = ''
      returnForm.readerId = ''
      await loadBooks(page.value)
      feedback.value = { kind: 'success', message: '歸還成功，館藏數量已更新。' }
    } catch (error) {
      setError(error)
    } finally {
      isSubmitting.value = false
    }
  }

  return {
    books,
    query,
    page,
    pageSize,
    total,
    totalPages,
    isLoading,
    isSubmitting,
    activeTab,
    feedback,
    createForm,
    borrowForm,
    returnForm,
    hasPreviousPage,
    hasNextPage,
    clearFeedback,
    loadBooks,
    search,
    goToPage,
    changePageSize,
    createBook,
    selectBorrow,
    selectReturn,
    borrowBook,
    returnBook,
  }
}
