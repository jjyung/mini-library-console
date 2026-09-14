import type { APIRequestContext, TestInfo } from '@playwright/test'
import { randomUUID } from 'node:crypto'

const apiBaseUrl = process.env.QA_API_BASE_URL ?? 'http://localhost:8080'

export type BookData = {
  title: string
  isbn: string
  author?: string
  category: string
  quantity: number
  isActive: boolean
}

type BookResponse = {
  code: string
  message: string
  data: Record<string, unknown>
}

const makeRunId = (testInfo: TestInfo): string => {
  const titleSlug = testInfo.title.replace(/[^a-z0-9]+/gi, '-').slice(0, 18).replace(/-+$/g, '')
  return `qa-${Date.now().toString(36)}-w${testInfo.workerIndex}-${titleSlug || 'flow'}`
}

export class BookDataClient {
  private sequence = 0

  constructor(
    private readonly request: APIRequestContext,
    readonly runId: string,
  ) {}

  newBook(overrides: Partial<BookData> = {}): BookData {
    this.sequence += 1
    const uniquePart = `${Date.now()}${this.sequence}`.slice(-10)

    return {
      title: `${this.runId} book ${this.sequence}`,
      isbn: `978${uniquePart}${this.sequence.toString().padStart(2, '0')}`,
      author: 'QA Test Author',
      category: 'technology',
      quantity: 1,
      isActive: true,
      ...overrides,
    }
  }

  async createBook(book: BookData): Promise<Record<string, unknown>> {
    const response = await this.request.post(`${apiBaseUrl}/api/books`, {
      headers: { 'Idempotency-Key': randomUUID() },
      data: book,
    })
    const payload = (await response.json()) as BookResponse

    if (!response.ok() || payload.code !== '00000') {
      throw new Error(`QA setup failed to create ${book.isbn}: ${response.status()} ${JSON.stringify(payload)}`)
    }

    return payload.data
  }
}

export { makeRunId }
