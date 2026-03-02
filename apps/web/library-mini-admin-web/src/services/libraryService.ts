import { getApi, patchApi, postApi } from '@/services/apiClient'
import type {
  ApiResponseDTO,
  GetBooksRequestDTO,
  GetBooksResponseDataDTO,
  GetBorrowRecordsRequestDTO,
  GetBorrowRecordsResponseDataDTO,
  PatchBorrowRecordsRequestDTO,
  PatchBorrowRecordsResponseDataDTO,
  PostBooksRequestDTO,
  PostBooksResponseDataDTO,
  PostBorrowRecordsRequestDTO,
  PostBorrowRecordsResponseDataDTO,
} from '@/types/dto'

export const createBook = (requestDto: PostBooksRequestDTO): Promise<ApiResponseDTO<PostBooksResponseDataDTO>> => {
  return postApi('/api/v1/books', requestDto)
}

export const listBooks = (requestDto: GetBooksRequestDTO): Promise<ApiResponseDTO<GetBooksResponseDataDTO>> => {
  return getApi('/api/v1/books', {
    status: requestDto.status,
    titleKeyword: requestDto.titleKeyword
  })
}

export const borrowBook = (
  requestDto: PostBorrowRecordsRequestDTO
): Promise<ApiResponseDTO<PostBorrowRecordsResponseDataDTO>> => {
  return postApi('/api/v1/borrow-records', requestDto)
}

export const returnBorrowRecord = (
  borrowRecordId: number,
  requestDto: PatchBorrowRecordsRequestDTO
): Promise<ApiResponseDTO<PatchBorrowRecordsResponseDataDTO>> => {
  return patchApi(`/api/v1/borrow-records/${borrowRecordId}/return`, requestDto)
}

export const listBorrowRecords = (
  requestDto: GetBorrowRecordsRequestDTO
): Promise<ApiResponseDTO<GetBorrowRecordsResponseDataDTO>> => {
  return getApi('/api/v1/borrow-records', {
    status: requestDto.status,
    bookId: requestDto.bookId,
    borrowerName: requestDto.borrowerName
  })
}
