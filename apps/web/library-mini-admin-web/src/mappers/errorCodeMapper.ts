const errorCodeTextMap: Record<string, string> = {
  '00000': '操作成功',
  A0001: '查無書籍',
  A0002: 'ISBN 已存在',
  A0003: '欄位缺漏或格式錯誤',
  A0004: '庫存不足',
  A0005: '書籍未上架',
  A0006: '無借出紀錄',
  A0007: '逾期歸還（已完成）',
  B0000: '系統錯誤',
  C0000: '第三方服務錯誤',
}

export function mapBusinessCodeToText(code: string, fallbackMessage: string): string {
  return errorCodeTextMap[code] ?? fallbackMessage
}
