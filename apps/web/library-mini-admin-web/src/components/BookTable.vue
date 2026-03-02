<script setup lang="ts">
import type { BookView } from '../api/libraryApi'

const props = defineProps<{
  books: BookView[]
}>()

const emit = defineEmits<{
  quickBorrow: [isbn: string]
  quickReturn: [isbn: string]
}>()

function isBorrowDisabled(book: BookView): boolean {
  return book.availableQuantity <= 0 || book.shelfStatus === 'OFF_SHELF'
}

function isReturnDisabled(book: BookView): boolean {
  return book.availableQuantity >= book.totalQuantity
}

function borrow(book: BookView) {
  if (!isBorrowDisabled(book)) {
    emit('quickBorrow', book.isbn)
  }
}

function returnBack(book: BookView) {
  if (!isReturnDisabled(book)) {
    emit('quickReturn', book.isbn)
  }
}
</script>

<template>
  <section class="book-table">
    <div class="book-table-header">
      <h2>館藏列表</h2>
      <span>共 {{ props.books.length }} 本</span>
    </div>
    <table data-testid="book-list-table">
      <thead>
        <tr>
          <th>Title</th>
          <th>ISBN</th>
          <th>Author</th>
          <th>Category</th>
          <th>Stock</th>
          <th>Status</th>
          <th>Action</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in props.books" :key="book.bookId" data-testid="book-list-row">
          <td>{{ book.title }}</td>
          <td>{{ book.isbn }}</td>
          <td>{{ book.author ?? '-' }}</td>
          <td>{{ book.category }}</td>
          <td>{{ book.availableQuantity }}/{{ book.totalQuantity }}</td>
          <td>{{ book.circulationStatus }}</td>
          <td class="actions">
            <button
              type="button"
              data-testid="book-row-borrow"
              :disabled="isBorrowDisabled(book)"
              @click="borrow(book)"
            >
              借出
            </button>
            <button
              type="button"
              data-testid="book-row-return"
              :disabled="isReturnDisabled(book)"
              @click="returnBack(book)"
            >
              歸還
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.book-table {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #ffffff;
  overflow: hidden;
}

.book-table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: #f8fafc;
}

.book-table-header h2 {
  margin: 0;
  font-size: 18px;
}

.book-table-header span {
  color: #6b7280;
  font-size: 13px;
}

.book-table table {
  width: 100%;
  border-collapse: collapse;
}

.book-table th,
.book-table td {
  border-bottom: 1px solid #e5e7eb;
  padding: 10px 12px;
  text-align: left;
  font-size: 14px;
}

.book-table th {
  background: #f8fafc;
  color: #374151;
}

.actions {
  white-space: nowrap;
}

.actions button {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
  padding: 5px 10px;
  margin-right: 8px;
  cursor: pointer;
}

.actions button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
