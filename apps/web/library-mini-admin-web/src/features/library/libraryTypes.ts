import type { components } from '@/core/api/generated/schema'

export type Book = components['schemas']['BookSummaryResponseDTO']
export type BookDetail = components['schemas']['BookDetailResponseDTO']
export type Category = components['schemas']['PostBooksRequestDTO']['category']
export type CreateBookForm = Omit<components['schemas']['PostBooksRequestDTO'], 'isActive'> & {
  isActive: boolean
}

export const categories: Array<{ value: Category; label: string }> = [
  { value: 'literature', label: '文學' },
  { value: 'science', label: '科學' },
  { value: 'technology', label: '科技' },
  { value: 'history', label: '歷史' },
  { value: 'art', label: '藝術' },
  { value: 'philosophy', label: '哲學' },
  { value: 'business', label: '商業' },
  { value: 'education', label: '教育' },
]

export const categoryLabels: Record<Category, string> = Object.fromEntries(
  categories.map((category) => [category.value, category.label]),
) as Record<Category, string>

export const statusLabels: Record<Book['status'], string> = {
  available: '可借閱',
  borrowed: '借閱中',
  inactive: '未上架',
}
