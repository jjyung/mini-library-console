import createClient from 'openapi-fetch'

import type { paths } from './generated/schema'

const defaultApiBaseUrl = 'http://localhost:8080'

export const apiBaseUrl =
  (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ??
  defaultApiBaseUrl

export const apiClient = createClient<paths>({ baseUrl: apiBaseUrl })

export const createRequestKey = (): string => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `mvp-${Date.now()}-${Math.random().toString(16).slice(2)}`
}
