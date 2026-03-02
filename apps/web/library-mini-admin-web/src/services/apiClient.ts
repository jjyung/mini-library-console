import type { ApiResponseDTO } from '@/types/dto'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const buildQueryString = (queryParameters: Record<string, string | number | undefined>): string => {
  const urlSearchParams = new URLSearchParams()
  Object.entries(queryParameters).forEach(([queryKey, queryValue]) => {
    if (queryValue === undefined || queryValue === '') {
      return
    }
    urlSearchParams.set(queryKey, String(queryValue))
  })
  const serializedQuery = urlSearchParams.toString()
  return serializedQuery ? `?${serializedQuery}` : ''
}

export const getApi = async <TData>(path: string, queryParameters: Record<string, string | number | undefined> = {}) => {
  const response = await fetch(`${API_BASE_URL}${path}${buildQueryString(queryParameters)}`)
  const payload = (await response.json()) as ApiResponseDTO<TData>
  return payload
}

export const postApi = async <TData, TRequest>(path: string, requestBody: TRequest) => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestBody)
  })
  const payload = (await response.json()) as ApiResponseDTO<TData>
  return payload
}

export const patchApi = async <TData, TRequest>(path: string, requestBody: TRequest) => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestBody)
  })
  const payload = (await response.json()) as ApiResponseDTO<TData>
  return payload
}
