import { apiClient } from '@/services/apiClient'
import type {
  GetBooksResponseBodyDTO,
  PatchBorrowRecordsResponseBodyDTO,
  PostBooksRequestDTO,
  PostBooksResponseBodyDTO,
  PostBorrowRecordsRequestDTO,
  PostBorrowRecordsResponseBodyDTO,
} from '@/types/api'

export const libraryService = {
  createBook(requestDTO: PostBooksRequestDTO): Promise<PostBooksResponseBodyDTO> {
    return apiClient.post<PostBooksResponseBodyDTO>('/books', requestDTO as unknown as Record<string, unknown>)
  },
  getBooks(): Promise<GetBooksResponseBodyDTO> {
    return apiClient.get<GetBooksResponseBodyDTO>('/books')
  },
  borrowBook(requestDTO: PostBorrowRecordsRequestDTO): Promise<PostBorrowRecordsResponseBodyDTO> {
    return apiClient.post<PostBorrowRecordsResponseBodyDTO>(
      '/borrow-records',
      requestDTO as unknown as Record<string, unknown>,
    )
  },
  returnBook(borrowRecordId: number): Promise<PatchBorrowRecordsResponseBodyDTO> {
    return apiClient.patch<PatchBorrowRecordsResponseBodyDTO>(`/borrow-records/${borrowRecordId}/return`, {})
  },
}
