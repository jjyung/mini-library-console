import type { LibraryApiError } from '@/core/api/libraryApi'

import type { UiAction } from './libraryTypes'

const clientMessages: Record<UiAction, string> = {
  catalogue: '館藏資料目前無法取得，請重新載入。',
  create: '請修正書籍欄位或確認 ISBN 尚未存在。',
  borrow: '找不到可借出的書籍，請確認 ISBN、上架狀態或可借數量。',
  return: '找不到可歸還的借閱紀錄，或需要補充讀者 ID。',
}

const systemMessages: Record<UiAction, string> = {
  catalogue: '系統暫時無法取得館藏，請稍後重試。',
  create: '系統暫時無法新增書籍，請稍後重試。',
  borrow: '系統暫時無法完成借出，請稍後重試。',
  return: '系統暫時無法完成歸還，請稍後重試。',
}

export function mapApiError(error: LibraryApiError, action: UiAction): string {
  switch (error.code) {
    case 'A0000':
      return clientMessages[action]
    case 'C0000':
      return '外部服務暫時無法使用，請稍後重試。'
    case 'B0000':
    default:
      return systemMessages[action]
  }
}

