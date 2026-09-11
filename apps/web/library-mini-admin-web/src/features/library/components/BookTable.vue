<script setup lang="ts">
import { categoryLabels, statusLabels } from '../libraryTypes'

import type { Book } from '../libraryTypes'

defineProps<{
  books: Book[]
  page: number
  pageSize: number
  totalPages: number
  total: number
  isLoading: boolean
  hasPreviousPage: boolean
  hasNextPage: boolean
}>()

const emit = defineEmits<{
  borrow: [book: Book]
  return: [book: Book]
  page: [page: number]
  pageSize: [pageSize: number]
}>()
</script>

<template>
  <section class="panel catalogue-panel">
    <div class="panel-heading catalogue-heading">
      <div>
        <p class="eyebrow">Live inventory</p>
        <h2>館藏清單 <span class="count-badge">{{ total }}</span></h2>
      </div>
      <span class="sync-label"><span class="dot"></span> 即時同步</span>
    </div>
    <div v-if="isLoading" class="table-state" role="status">載入館藏中…</div>
    <div v-else-if="books.length === 0" class="table-state empty-state">
      <span class="empty-icon" aria-hidden="true">⌕</span>
      <strong>找不到館藏</strong>
      <span>試試其他搜尋文字，或先新增一本書。</span>
    </div>
    <div v-else class="table-wrap">
      <table data-testid="library-book-table">
        <thead>
          <tr>
            <th>書籍</th>
            <th>分類</th>
            <th>庫存</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.bookId" data-testid="library-book-row" :data-book-id="book.bookId">
            <td>
              <div class="book-cell">
                <span class="book-cover" aria-hidden="true">▥</span>
                <span><strong>{{ book.title }}</strong><small>{{ book.author || '未提供作者' }} · {{ book.isbn }}</small></span>
              </div>
            </td>
            <td>{{ categoryLabels[book.category as keyof typeof categoryLabels] || book.category }}</td>
            <td><strong>{{ book.availableCount }}</strong><span class="muted"> / {{ book.totalCount }}</span></td>
            <td><span :class="['status-pill', `status-${book.status}`]">{{ statusLabels[book.status] }}</span></td>
            <td class="action-cell">
              <button v-if="book.availableCount > 0 && book.status !== 'inactive'" class="text-button" type="button" @click="emit('borrow', book)">借出</button>
              <button v-else class="text-button disabled-action" disabled type="button">不可借</button>
              <button v-if="book.activeLoan" class="text-button return-link" type="button" @click="emit('return', book)">歸還</button>
              <button v-else class="text-button disabled-action" disabled type="button">無借閱</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="pagination" aria-label="館藏分頁">
      <span>第 {{ page }} / {{ totalPages }} 頁</span>
      <div class="pagination-actions">
        <label class="page-size-label" for="library-page-size">每頁</label>
        <select id="library-page-size" data-testid="library-book-page-size" :value="pageSize" :disabled="isLoading" @change="emit('pageSize', Number(($event.target as HTMLSelectElement).value))">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <button data-testid="library-book-prev" :disabled="!hasPreviousPage || isLoading" type="button" @click="emit('page', page - 1)">上一頁</button>
        <button data-testid="library-book-next" :disabled="!hasNextPage || isLoading" type="button" @click="emit('page', page + 1)">下一頁</button>
      </div>
    </div>
  </section>
</template>
