import { test as base } from '@playwright/test'

import { LibraryDataClient } from './library-data'
import { LibraryAdminPage } from './library-page'

type QaFixtures = {
  libraryPage: LibraryAdminPage
  testData: LibraryDataClient
}

export const test = base.extend<QaFixtures>({
  libraryPage: async ({ page }, use) => {
    await use(new LibraryAdminPage(page))
  },
  testData: async ({ request }, use) => {
    const client = new LibraryDataClient(request)
    await use(client)
    await client.cleanup()
  },
})

export { expect } from '@playwright/test'
