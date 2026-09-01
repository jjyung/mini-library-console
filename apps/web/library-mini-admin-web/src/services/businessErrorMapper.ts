import { ApiRequestError } from './apiClient'
import type { BusinessCode, Feedback } from '@/types/api'

const DEFAULT_MESSAGES: Record<BusinessCode, string> = {
  '00000': '操作成功',
  A0000: '請檢查輸入內容後再試一次',
  A0001: '請填寫正確的必填欄位',
  A0002: '找不到該 ISBN 的書籍',
  A0003: 'ISBN 已存在，無法重複新增',
  A0004: '該書籍已無可借副本',
  A0005: '該書籍未上架，無法借閱',
  A0006: '該書籍沒有借閱中的副本可歸還',
  B0000: '系統暫時無法完成操作，請稍後再試',
  B0001: '館藏狀態更新失敗，請重新載入後再試',
  C0000: '外部服務暫時無法使用，請稍後再試',
}

const FIELD_LABELS: Record<string, string> = {
  title: '書名',
  isbn: 'ISBN',
  category: '分類',
  quantity: '數量',
  readerId: '讀者 ID',
  dueDate: '到期日',
}

export function isApiRequestError(error: unknown): error is ApiRequestError {
  return error instanceof ApiRequestError
}

function getMessage(error: ApiRequestError): string {
  if (error.code === 'A0001' && error.details?.length) {
    const fields = error.details
      .map((detail) => FIELD_LABELS[detail.field] ?? detail.field)
      .join('、')
    return `請修正欄位：${fields}`
  }

  return DEFAULT_MESSAGES[error.code] ?? DEFAULT_MESSAGES.B0000
}

export function mapBusinessError(error: unknown): Feedback {
  if (isApiRequestError(error)) {
    return {
      type: 'error',
      code: error.code,
      message: getMessage(error),
      traceId: error.traceId,
      details: error.details,
    }
  }

  return {
    type: 'error',
    code: 'B0000',
    message: DEFAULT_MESSAGES.B0000,
  }
}

export function mapSuccess(message: string, traceId?: string): Feedback {
  return {
    type: 'success',
    code: '00000',
    message,
    traceId,
  }
}
