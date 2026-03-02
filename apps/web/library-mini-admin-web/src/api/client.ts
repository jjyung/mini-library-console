export interface ApiEnvelope<T> {
  code: string
  message: string
  data: T | null
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

function buildUrl(path: string): string {
  if (!apiBaseUrl) {
    return path
  }
  return `${apiBaseUrl}${path}`
}

async function request<T>(path: string, init: RequestInit): Promise<ApiEnvelope<T>> {
  const response = await fetch(buildUrl(path), {
    headers: {
      'Content-Type': 'application/json',
      ...init.headers,
    },
    ...init,
  })
  const json = (await response.json()) as ApiEnvelope<T>
  return json
}

export const apiClient = {
  get<T>(path: string): Promise<ApiEnvelope<T>> {
    return request<T>(path, { method: 'GET' })
  },
  post<TReq, TResp>(path: string, payload: TReq): Promise<ApiEnvelope<TResp>> {
    return request<TResp>(path, { method: 'POST', body: JSON.stringify(payload) })
  },
}
