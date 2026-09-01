import type { ErrorDetail } from './library'

export type BusinessCode =
  | '00000'
  | 'A0000'
  | 'A0001'
  | 'A0002'
  | 'A0003'
  | 'A0004'
  | 'A0005'
  | 'A0006'
  | 'B0000'
  | 'B0001'
  | 'C0000'

export type ApiEnvelope<T> = {
  code: BusinessCode | string
  message: string
  traceId: string
  data: T
  details?: ErrorDetail[]
}

export type ApiErrorPayload = {
  code?: string
  message?: string
  traceId?: string
  details?: ErrorDetail[]
}

export type Feedback = {
  type: 'success' | 'error'
  code: BusinessCode
  message: string
  traceId?: string
  details?: ErrorDetail[]
}
