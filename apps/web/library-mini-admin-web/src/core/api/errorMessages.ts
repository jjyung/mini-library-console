export const businessErrorMessages: Record<string, string> = {
  A0000: '請檢查輸入內容後再試一次。',
  B0000: '系統暫時無法完成操作，請稍後再試。',
  C0000: '外部服務暫時無法使用，請稍後再試。',
}

export const getBusinessErrorMessage = (code: string, fallback?: string): string =>
  fallback?.trim() || businessErrorMessages[code] || businessErrorMessages.B0000 || '系統暫時無法完成操作，請稍後再試。'
