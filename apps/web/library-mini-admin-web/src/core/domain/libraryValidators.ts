import type { CreateBookForm, BorrowForm, ReturnForm, ValidationErrors } from './libraryTypes'

const requiredMessage = '此欄位為必填。'

export function validateCreateBook(form: CreateBookForm): ValidationErrors {
  const errors: ValidationErrors = {}
  if (!form.title.trim()) errors.title = requiredMessage
  if (form.title.trim().length > 200) errors.title = '書名不可超過 200 個字。'
  if (!form.isbn.trim()) errors.isbn = requiredMessage
  if (form.isbn.trim().length > 20) errors.isbn = 'ISBN 不可超過 20 個字。'
  if (!form.category.trim()) errors.category = requiredMessage
  if (form.category.trim().length > 30) errors.category = '分類不可超過 30 個字。'
  if (form.author.trim().length > 200) errors.author = '作者不可超過 200 個字。'
  if (!Number.isInteger(form.quantity) || form.quantity < 1) errors.quantity = '數量必須是至少 1 的正整數。'
  return errors
}

export function validateBorrow(form: BorrowForm): ValidationErrors {
  const errors: ValidationErrors = {}
  if (!form.readerId.trim()) errors.readerId = requiredMessage
  if (!form.isbn.trim()) errors.isbn = requiredMessage
  if (form.readerId.trim().length > 100) errors.readerId = '讀者 ID 不可超過 100 個字。'
  if (form.isbn.trim().length > 20) errors.isbn = 'ISBN 不可超過 20 個字。'
  return errors
}

export function validateReturn(form: ReturnForm): ValidationErrors {
  const errors: ValidationErrors = {}
  if (!form.isbn.trim()) errors.isbn = requiredMessage
  if (form.readerId.trim().length > 100) errors.readerId = '讀者 ID 不可超過 100 個字。'
  if (form.isbn.trim().length > 20) errors.isbn = 'ISBN 不可超過 20 個字。'
  return errors
}
