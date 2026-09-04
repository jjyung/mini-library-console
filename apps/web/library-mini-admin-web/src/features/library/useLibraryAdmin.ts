import { computed, ref } from 'vue'

import { LibraryApiError, libraryApi } from '@/core/api/libraryApi'
import { mapBookDto } from '@/core/domain/bookMapper'
import { mapApiError } from '@/core/domain/errorMessages'
import { validateBorrow, validateCreateBook, validateReturn } from '@/core/domain/libraryValidators'

import type { LibraryApi } from '@/core/api/libraryApi'
import type { Book, BorrowForm, CreateBookForm, Feedback, ReturnForm, UiAction } from '@/core/domain/libraryTypes'

type CatalogueState = 'loading' | 'success' | 'error'

function toFailure(error: unknown): LibraryApiError {
  if (error instanceof LibraryApiError) return error
  return new LibraryApiError('B0000', 'System failure')
}

export function useLibraryAdmin(service: LibraryApi = libraryApi) {
  const books = ref<Book[]>([])
  const catalogueState = ref<CatalogueState>('loading')
  const catalogueError = ref<Feedback | null>(null)
  const addBookStatus = ref<Feedback | null>(null)
  const borrowStatus = ref<Feedback | null>(null)
  const returnStatus = ref<Feedback | null>(null)
  const toast = ref<Feedback | null>(null)
  const isLoading = ref(false)
  const isCreating = ref(false)
  const isBorrowing = ref(false)
  const isReturning = ref(false)

  const isMutating = computed(() => isCreating.value || isBorrowing.value || isReturning.value)

  function setError(action: UiAction, error: unknown): Feedback {
    const apiError = toFailure(error)
    const feedback: Feedback = {
      type: 'error',
      message: mapApiError(apiError, action),
      code: apiError.code,
      traceId: apiError.traceId,
    }

    if (action === 'catalogue') catalogueError.value = feedback
    if (action === 'create') addBookStatus.value = feedback
    if (action === 'borrow') borrowStatus.value = feedback
    if (action === 'return') returnStatus.value = feedback
    toast.value = feedback
    return feedback
  }

  function setSuccess(action: Exclude<UiAction, 'catalogue'>, message: string): Feedback {
    const feedback: Feedback = { type: 'success', message, code: undefined }
    if (action === 'create') addBookStatus.value = feedback
    if (action === 'borrow') borrowStatus.value = feedback
    if (action === 'return') returnStatus.value = feedback
    toast.value = feedback
    return feedback
  }

  async function loadBooks(): Promise<boolean> {
    isLoading.value = true
    catalogueState.value = 'loading'
    catalogueError.value = null

    try {
      const response = await service.getBooks()
      books.value = response.map(mapBookDto)
      catalogueState.value = 'success'
      return true
    } catch (error) {
      catalogueState.value = 'error'
      setError('catalogue', error)
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function createBook(form: CreateBookForm): Promise<boolean> {
    const validationErrors = validateCreateBook(form)
    if (Object.keys(validationErrors).length > 0) {
      addBookStatus.value = { type: 'error', message: '請修正新增書籍表單中的欄位。', code: 'A0000' }
      toast.value = addBookStatus.value
      return false
    }

    isCreating.value = true
    addBookStatus.value = null
    try {
      await service.createBook({
        title: form.title.trim(),
        isbn: form.isbn.trim(),
        author: form.author.trim() || undefined,
        category: form.category.trim(),
        quantity: form.quantity,
        isActive: form.isActive,
      })
      setSuccess('create', '書籍新增成功！')
      return await loadBooks()
    } catch (error) {
      setError('create', error)
      return false
    } finally {
      isCreating.value = false
    }
  }

  async function borrowBook(form: BorrowForm): Promise<boolean> {
    const validationErrors = validateBorrow(form)
    if (Object.keys(validationErrors).length > 0) {
      borrowStatus.value = { type: 'error', message: '請填寫讀者 ID 與 ISBN。', code: 'A0000' }
      toast.value = borrowStatus.value
      return false
    }

    isBorrowing.value = true
    borrowStatus.value = null
    try {
      await service.borrowBook({
        readerId: form.readerId.trim(),
        isbn: form.isbn.trim(),
        dueDate: form.dueDate || undefined,
      })
      setSuccess('borrow', '借書成功！')
      return await loadBooks()
    } catch (error) {
      setError('borrow', error)
      return false
    } finally {
      isBorrowing.value = false
    }
  }

  async function returnBook(form: ReturnForm): Promise<boolean> {
    const validationErrors = validateReturn(form)
    if (Object.keys(validationErrors).length > 0) {
      returnStatus.value = { type: 'error', message: '請填寫 ISBN。', code: 'A0000' }
      toast.value = returnStatus.value
      return false
    }

    isReturning.value = true
    returnStatus.value = null
    try {
      await service.returnBook({
        isbn: form.isbn.trim(),
        readerId: form.readerId.trim() || undefined,
      })
      setSuccess('return', '歸還成功！')
      return await loadBooks()
    } catch (error) {
      setError('return', error)
      return false
    } finally {
      isReturning.value = false
    }
  }

  return {
    books,
    catalogueState,
    catalogueError,
    addBookStatus,
    borrowStatus,
    returnStatus,
    toast,
    isLoading,
    isCreating,
    isBorrowing,
    isReturning,
    isMutating,
    loadBooks,
    createBook,
    borrowBook,
    returnBook,
  }
}

