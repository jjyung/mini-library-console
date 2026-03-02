export type BusinessCode = '00000' | 'A0000' | 'B0000' | 'C0000'

export interface ApiResponseDTO<TData> {
  code: BusinessCode
  message: string
  data: TData
}

export type BookStatus = 'AVAILABLE' | 'BORROWED'
export type BorrowRecordStatus = 'BORROWED' | 'RETURNED'

export interface BookDTO {
  id: number
  title: string
  author: string
  totalQuantity: number
  availableQuantity: number
  status: BookStatus
  createdAt: string
  updatedAt: string
}

export interface BorrowRecordDTO {
  id: number
  bookId: number
  borrowerName: string
  status: BorrowRecordStatus
  borrowedAt: string
  returnedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface PostBooksRequestDTO {
  title: string
  author: string
  totalQuantity: number
}

export interface PostBooksResponseBodyDTO {
  book: BookDTO
}

export interface GetBooksResponseBodyDTO {
  books: BookDTO[]
}

export interface PostBorrowRecordsRequestDTO {
  bookId: number
  borrowerName: string
}

export interface PostBorrowRecordsResponseBodyDTO {
  borrowRecord: BorrowRecordDTO
  book: BookDTO
}

export interface PatchBorrowRecordsResponseBodyDTO {
  borrowRecord: BorrowRecordDTO
  book: BookDTO
}
