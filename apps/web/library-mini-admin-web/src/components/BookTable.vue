<script setup lang="ts">
import type { Book, BookStatus } from '@/core/domain/libraryTypes'

defineProps<{
  books: Book[]
}>()

const emit = defineEmits<{
  quickBorrow: [isbn: string]
  quickReturn: [isbn: string]
}>()

const categoryLabels: Record<string, string> = {
  literature: '文學',
  science: '科學',
  technology: '科技',
  history: '歷史',
  art: '藝術',
  philosophy: '哲學',
  business: '商業',
  education: '教育',
}

const statusLabels: Record<BookStatus, string> = {
  available: '可借閱',
  borrowed: '已借出',
  inactive: '未上架',
}

function categoryLabel(category: string) {
  return categoryLabels[category] ?? category
}

function isBorrowDisabled(book: Book) {
  return book.availableCount === 0 || book.status === 'inactive'
}

function isReturnDisabled(book: Book) {
  return book.availableCount === book.totalCount
}
</script>

<template>
  <section class="card catalogue-card" data-testid="book-table">
    <div class="card-heading card-heading--catalogue">
      <div>
        <h2>館藏列表</h2>
        <p>查看目前館藏與借閱狀態</p>
      </div>
      <span v-if="books.length > 0" class="book-count">共 {{ books.length }} 本</span>
    </div>

    <div v-if="books.length === 0" data-testid="catalogue-empty" class="empty-state">
      <div class="empty-icon" aria-hidden="true">▧</div>
      <h3>尚無館藏</h3>
      <p>請使用右側表單新增書籍</p>
    </div>

    <div v-else class="table-scroll">
      <table>
        <caption class="sr-only">館藏書籍列表</caption>
        <thead>
          <tr>
            <th scope="col">書名</th>
            <th scope="col">ISBN</th>
            <th scope="col">作者</th>
            <th scope="col">分類</th>
            <th scope="col">狀態</th>
            <th scope="col">可借數／總數</th>
            <th scope="col">動作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.bookId" :data-testid="`book-row-${book.isbn}`">
            <td class="book-title">{{ book.title }}</td>
            <td class="isbn">{{ book.isbn }}</td>
            <td>{{ book.author || '—' }}</td>
            <td>{{ categoryLabel(book.category) }}</td>
            <td>
              <span :data-testid="`book-status-${book.isbn}`" class="status-badge" :class="`status-badge--${book.status}`">
                {{ statusLabels[book.status] }}
              </span>
            </td>
            <td>
              <span :data-testid="`book-available-${book.isbn}`" :class="{ 'available-zero': book.availableCount === 0 }" class="availability">
                {{ book.availableCount }}
              </span>
              <span class="availability-total">／{{ book.totalCount }}</span>
            </td>
            <td>
              <div class="table-actions">
                <button
                  type="button"
                  :data-testid="`quick-borrow-${book.isbn}`"
                  class="table-action table-action--borrow"
                  :disabled="isBorrowDisabled(book)"
                  :aria-label="`快速借出 ${book.title}`"
                  @click="emit('quickBorrow', book.isbn)"
                >
                  <span aria-hidden="true">↗</span>
                  借出
                </button>
                <button
                  type="button"
                  :data-testid="`quick-return-${book.isbn}`"
                  class="table-action table-action--return"
                  :disabled="isReturnDisabled(book)"
                  :aria-label="`快速歸還 ${book.title}`"
                  @click="emit('quickReturn', book.isbn)"
                >
                  <span aria-hidden="true">↙</span>
                  歸還
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

