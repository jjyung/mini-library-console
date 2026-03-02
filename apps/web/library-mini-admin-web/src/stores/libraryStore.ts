import { reactive } from 'vue'

import {
  borrowBook,
  createBook,
  listBooks,
  listBorrowRecords,
  returnBorrowRecord,
} from '@/services/libraryService'
import type {
  BookDTO,
  BorrowRecordDTO,
  PostBooksRequestDTO,
  PostBorrowRecordsRequestDTO,
} from '@/types/dto'

interface LibraryState {
  books: BookDTO[]
  borrowRecords: BorrowRecordDTO[]
  errorMessage: string
  infoMessage: string
  loading: boolean
}

const mapBusinessErrorMessage = (errorSubCode: string | undefined, fallbackMessage: string): string => {
  if (errorSubCode === 'A0001') {
    return '欄位格式不正確，請確認輸入內容。'
  }
  if (errorSubCode === 'A0003') {
    return '找不到指定書籍。'
  }
  if (errorSubCode === 'A0004') {
    return '可借庫存不足，無法借出。'
  }
  if (errorSubCode === 'A0005') {
    return '找不到指定借閱紀錄。'
  }
  if (errorSubCode === 'A0006') {
    return '該借閱紀錄已歸還。'
  }
  if (errorSubCode?.startsWith('B')) {
    return '系統忙碌中，請稍後再試。'
  }
  if (errorSubCode?.startsWith('C')) {
    return '外部服務異常，請稍後再試。'
  }
  return fallbackMessage
}

const newState = (): LibraryState => ({
  books: [],
  borrowRecords: [],
  errorMessage: '',
  infoMessage: '',
  loading: false
})

const state = reactive<LibraryState>(newState())

const setError = (errorSubCode: string | undefined, fallbackMessage: string): void => {
  state.errorMessage = mapBusinessErrorMessage(errorSubCode, fallbackMessage)
}

const clearMessages = (): void => {
  state.errorMessage = ''
  state.infoMessage = ''
}

const runWithLoading = async (action: () => Promise<void>): Promise<void> => {
  state.loading = true
  try {
    await action()
  } finally {
    state.loading = false
  }
}

const refreshBooks = async (): Promise<void> => {
  const response = await listBooks({})
  if (response.code !== '00000' || response.data === null) {
    setError(response.error?.errorSubCode, response.error?.errorMessage ?? '查詢書籍失敗。')
    return
  }
  state.books = response.data.books
}

const refreshBorrowRecords = async (): Promise<void> => {
  const response = await listBorrowRecords({})
  if (response.code !== '00000' || response.data === null) {
    setError(response.error?.errorSubCode, response.error?.errorMessage ?? '查詢借閱紀錄失敗。')
    return
  }
  state.borrowRecords = response.data.borrowRecords
}

const initialize = async (): Promise<void> => {
  clearMessages()
  await runWithLoading(async () => {
    await refreshBooks()
    await refreshBorrowRecords()
  })
}

const createBookAction = async (requestDto: PostBooksRequestDTO): Promise<void> => {
  clearMessages()
  await runWithLoading(async () => {
    const response = await createBook(requestDto)
    if (response.code !== '00000' || response.data === null) {
      setError(response.error?.errorSubCode, response.error?.errorMessage ?? '新增書籍失敗。')
      return
    }
    state.infoMessage = '書籍新增成功。'
    await refreshBooks()
  })
}

const borrowBookAction = async (requestDto: PostBorrowRecordsRequestDTO): Promise<void> => {
  clearMessages()
  await runWithLoading(async () => {
    const response = await borrowBook(requestDto)
    if (response.code !== '00000' || response.data === null) {
      setError(response.error?.errorSubCode, response.error?.errorMessage ?? '借書失敗。')
      return
    }
    state.infoMessage = `借書成功：${response.data.borrowRecord.borrowerName}`
    await refreshBooks()
    await refreshBorrowRecords()
  })
}

const returnBorrowRecordAction = async (borrowRecordId: number, returnedBy: string): Promise<void> => {
  clearMessages()
  await runWithLoading(async () => {
    const response = await returnBorrowRecord(borrowRecordId, { returnedBy })
    if (response.code !== '00000' || response.data === null) {
      setError(response.error?.errorSubCode, response.error?.errorMessage ?? '還書失敗。')
      return
    }
    state.infoMessage = `還書成功：${response.data.borrowRecord.borrowerName}`
    await refreshBooks()
    await refreshBorrowRecords()
  })
}

export const libraryStore = {
  state,
  actions: {
    initialize,
    createBookAction,
    borrowBookAction,
    returnBorrowRecordAction,
  },
  getters: {
    get availableBooks(): BookDTO[] {
      return state.books.filter((bookItem) => bookItem.availableCopies > 0)
    }
  }
}
