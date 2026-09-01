import type { ApiEnvelope, ApiErrorPayload, BusinessCode } from '@/types/api'

const SUCCESS_CODE: BusinessCode = '00000'
const SYSTEM_ERROR_CODE: BusinessCode = 'B0000'

export class ApiRequestError extends Error {
  readonly code: BusinessCode
  readonly traceId?: string
  readonly details?: ApiErrorPayload['details']
  readonly status?: number

  constructor(payload: ApiErrorPayload, status?: number) {
    super(payload.message ?? '系統暫時無法完成操作')
    this.name = 'ApiRequestError'
    this.code = isBusinessCode(payload.code) ? payload.code : SYSTEM_ERROR_CODE
    this.traceId = payload.traceId
    this.details = payload.details
    this.status = status
  }
}

function isBusinessCode(code: string | undefined): code is BusinessCode {
  return code === '00000'
    || code === 'A0000'
    || code === 'A0001'
    || code === 'A0002'
    || code === 'A0003'
    || code === 'A0004'
    || code === 'A0005'
    || code === 'A0006'
    || code === 'B0000'
    || code === 'B0001'
    || code === 'C0000'
}

function getApiBaseUrl(): string {
  return import.meta.env.VITE_API_BASE_URL ?? ''
}

function getRequestUrl(path: string): string {
  const baseUrl = getApiBaseUrl().replace(/\/$/, '')
  return `${baseUrl}${path}`
}

async function readPayload(response: Response): Promise<ApiErrorPayload> {
  try {
    const payload: unknown = await response.json()
    if (payload !== null && typeof payload === 'object') {
      return payload as ApiErrorPayload
    }
  } catch {
    // The fallback payload below preserves the business-code boundary.
  }

  return {
    code: SYSTEM_ERROR_CODE,
    message: 'API 回應格式無法辨識',
  }
}

export async function request<T>(path: string, init?: RequestInit): Promise<ApiEnvelope<T>> {
  let response: Response

  try {
    response = await fetch(getRequestUrl(path), {
      ...init,
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...init?.headers,
      },
    })
  } catch {
    throw new ApiRequestError({
      code: SYSTEM_ERROR_CODE,
      message: '目前無法連線至圖書館 API，請確認服務已啟動',
    })
  }

  const payload = await readPayload(response)
  if (!response.ok || payload.code !== SUCCESS_CODE) {
    throw new ApiRequestError(payload, response.status)
  }

  return payload as ApiEnvelope<T>
}

export function post<T>(path: string, body: unknown): Promise<ApiEnvelope<T>> {
  return request<T>(path, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}
