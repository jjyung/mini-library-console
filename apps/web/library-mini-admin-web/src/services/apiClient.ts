import type { ApiResponseDTO } from '@/types/api'

export class ApiClientError extends Error {
  public readonly code: string
  public readonly httpStatus: number

  constructor(message: string, code: string, httpStatus: number) {
    super(message)
    this.code = code
    this.httpStatus = httpStatus
  }
}

const API_BASE_URL = 'http://localhost:8080'

async function requestJson<TBody>(
  path: string,
  method: 'GET' | 'POST' | 'PATCH',
  body?: Record<string, unknown>,
): Promise<TBody> {
  const requestOptions: RequestInit = {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
  }

  if (method !== 'GET' && body !== undefined) {
    requestOptions.body = JSON.stringify(body)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, requestOptions)

  const responseJson = (await response.json()) as ApiResponseDTO<TBody>
  if (!response.ok || responseJson.code !== '00000') {
    throw new ApiClientError(responseJson.message, responseJson.code, response.status)
  }

  return responseJson.data
}

export const apiClient = {
  get<TBody>(path: string): Promise<TBody> {
    return requestJson<TBody>(path, 'GET')
  },
  post<TBody>(path: string, body: Record<string, unknown>): Promise<TBody> {
    return requestJson<TBody>(path, 'POST', body)
  },
  patch<TBody>(path: string, body: Record<string, unknown>): Promise<TBody> {
    return requestJson<TBody>(path, 'PATCH', body)
  },
}
