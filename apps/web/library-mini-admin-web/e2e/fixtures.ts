import { test as base, expect } from '@playwright/test'

import { BookDataClient, makeRunId } from './data'

type QaFixtures = {
  bookData: BookDataClient
  runId: string
}

export const test = base.extend<QaFixtures>({
  runId: async ({ request: _request }, use, testInfo) => {
    await use(makeRunId(testInfo))
  },
  bookData: async ({ request, runId }, use) => {
    await use(new BookDataClient(request, runId))
  },
})

export { expect }
