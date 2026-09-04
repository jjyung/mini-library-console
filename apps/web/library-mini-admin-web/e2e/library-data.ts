import type { APIRequestContext, TestInfo } from '@playwright/test'

type BookPayload = {
  title: string
  isbn: string
  author?: string
  category: string
  quantity: number
  isActive: boolean
}

type ApiEnvelope<T> = {
  code: string
  message: string
  traceId: string
  data: T
}

type ActiveLoan = {
  isbn: string
  readerId: string
}

const apiBaseUrl = (process.env.QA_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')
const runToken = (process.env.QA_RUN_ID ?? Date.now().toString(36)).replace(/[^a-z0-9]/gi, '').slice(-6)

function hash(value: string) {
  let result = 0
  for (const character of value) result = (result * 31 + character.charCodeAt(0)) >>> 0
  return result.toString(36)
}

export function createTestBook(testInfo: TestInfo, label: string, quantity = 1, isActive = true): BookPayload {
  const identity = `${runToken}-${testInfo.project.name}-${testInfo.workerIndex}-${testInfo.parallelIndex}-${testInfo.testId}-${label}`
  const suffix = hash(identity).slice(-8)

  return {
    title: `QA ${label} ${suffix}`,
    isbn: `Q${runToken}${suffix}`.slice(0, 20),
    author: 'QA Test Author',
    category: 'technology',
    quantity,
    isActive,
  }
}

export function createReaderId(testInfo: TestInfo, label: string) {
  return `qa-reader-${runToken}-${hash(`${testInfo.testId}-${testInfo.project.name}-${label}`).slice(-8)}`
}

export class LibraryDataClient {
  private readonly activeLoans = new Map<string, ActiveLoan>()

  constructor(private readonly request: APIRequestContext) {}

  async createBook(payload: BookPayload) {
    const response = await this.request.post(`${apiBaseUrl}/api/books`, { data: payload })
    const body = await response.json() as ApiEnvelope<unknown>
    if (!response.ok() || body.code !== '00000') {
      throw new Error(`Test data setup failed while creating ${payload.isbn}: ${body.code}`)
    }
    return body.data
  }

  async borrowBook(isbn: string, readerId: string) {
    const response = await this.request.post(`${apiBaseUrl}/api/loans/borrow`, {
      data: { isbn, readerId },
    })
    const body = await response.json() as ApiEnvelope<unknown>
    if (!response.ok() || body.code !== '00000') {
      throw new Error(`Test data setup failed while borrowing ${isbn}: ${body.code}`)
    }
    this.activeLoans.set(`${isbn}:${readerId}`, { isbn, readerId })
    return body.data
  }

  async returnBook(isbn: string, readerId?: string) {
    const response = await this.request.post(`${apiBaseUrl}/api/loans/return`, {
      data: { isbn, ...(readerId ? { readerId } : {}) },
    })
    const body = await response.json() as ApiEnvelope<unknown>
    if (!response.ok() || body.code !== '00000') {
      throw new Error(`Test data cleanup failed while returning ${isbn}: ${body.code}`)
    }
    if (readerId) this.activeLoans.delete(`${isbn}:${readerId}`)
    return body.data
  }

  rememberActiveLoan(isbn: string, readerId: string) {
    this.activeLoans.set(`${isbn}:${readerId}`, { isbn, readerId })
  }

  forgetActiveLoan(isbn: string, readerId: string) {
    this.activeLoans.delete(`${isbn}:${readerId}`)
  }

  async cleanup() {
    const loans = [...this.activeLoans.values()]
    this.activeLoans.clear()
    for (const loan of loans) {
      try {
        await this.returnBook(loan.isbn, loan.readerId)
      } catch {
        // The API has no delete/test-reset endpoint. Unique namespaces keep
        // data independent; this best-effort step neutralizes active loans.
      }
    }
  }
}

export function getApiUrl(path: string) {
  return `${apiBaseUrl}${path}`
}
