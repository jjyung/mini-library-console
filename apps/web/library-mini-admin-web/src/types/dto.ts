export type ApiCode = '00000' | 'A0000' | 'B0000' | 'C0000'

export interface ApiErrorDTO {
  errorSubCode: string
  errorMessage: string
}

export interface ApiResponseDTO<TData> {
  code: ApiCode
  message: string
  data: TData | null
  error: ApiErrorDTO | null
}

export interface BookDTO {
  id: number
  title: string
  author: string
  totalCopies: number
  availableCopies: number
  status: 'AVAILABLE' | 'CHECKED_OUT'
  createdAt: string
  updatedAt: string
}

export interface BorrowRecordDTO {
  id: number
  bookId: number
  bookTitle: string
  borrowerName: string
  borrowedAt: string
  returnedAt: string | null
  status: 'BORROWED' | 'RETURNED'
}

export interface PostBooksRequestDTO {
  title: string
  author: string
  totalCopies: number
}

export interface GetBooksRequestDTO {
  status?: 'AVAILABLE' | 'CHECKED_OUT'
  titleKeyword?: string
}

export interface PostBorrowRecordsRequestDTO {
  bookId: number
  borrowerName: string
}

export interface PatchBorrowRecordsRequestDTO {
  returnedBy: string
}

export interface GetBorrowRecordsRequestDTO {
  status?: 'BORROWED' | 'RETURNED'
  bookId?: number
  borrowerName?: string
}

export interface PostBooksResponseDataDTO {
  book: BookDTO
}

export interface GetBooksResponseDataDTO {
  books: BookDTO[]
}

export interface PostBorrowRecordsResponseDataDTO {
  borrowRecord: BorrowRecordDTO
}

export interface PatchBorrowRecordsResponseDataDTO {
  borrowRecord: BorrowRecordDTO
}

export interface GetBorrowRecordsResponseDataDTO {
  borrowRecords: BorrowRecordDTO[]
}
