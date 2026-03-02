import { reactive, computed } from 'vue'
import {
  borrowBook,
  createBook,
  listBooks,
  returnBook,
  searchBooks,
  type BookView,
  type BorrowBookRequest,
  type CreateBookRequest,
  type ReturnBookRequest,
} from '../api/libraryApi'
import { mapBusinessCodeToText } from '../mappers/errorCodeMapper'

interface UiMessage {
  code: string
  text: string
}

const state = reactive({
  books: [] as BookView[],
  message: null as UiMessage | null,
  loading: false,
})

const getters = {
  hasBooks: computed(() => state.books.length > 0),
}

function setMessage(code: string, message: string) {
  state.message = { code, text: mapBusinessCodeToText(code, message) }
}

async function refreshBooks(shelfStatus?: string) {
  state.loading = true
  try {
    const response = await listBooks(shelfStatus)
    if (response.code === '00000' && response.data?.books) {
      state.books = response.data.books
    }
    setMessage(response.code, response.message)
  } finally {
    state.loading = false
  }
}

async function createBookAction(payload: CreateBookRequest) {
  const response = await createBook(payload)
  setMessage(response.code, response.message)
  await refreshBooks()
}

async function searchBooksAction(keyword: string) {
  const response = await searchBooks(keyword)
  if (response.code === '00000' && response.data?.books) {
    state.books = response.data.books
  }
  setMessage(response.code, response.message)
}

async function borrowBookAction(payload: BorrowBookRequest) {
  const response = await borrowBook(payload)
  setMessage(response.code, response.message)
  await refreshBooks()
}

async function returnBookAction(payload: ReturnBookRequest) {
  const response = await returnBook(payload)
  setMessage(response.code, response.message)
  await refreshBooks()
}

export function useLibraryStore() {
  return {
    state,
    getters,
    actions: {
      refreshBooks,
      createBookAction,
      searchBooksAction,
      borrowBookAction,
      returnBookAction,
    },
  }
}
