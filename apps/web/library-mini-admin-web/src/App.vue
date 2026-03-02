<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useLibraryStore } from '@/stores/libraryStore'
import type { BorrowRecordDTO } from '@/types/api'

const libraryStore = useLibraryStore()
const activeBorrowRecords = computed<BorrowRecordDTO[]>(() => libraryStore.activeBorrowRecords.value)

const searchKeyword = ref('')
const activeTransactionTab = ref<'borrow' | 'return'>('borrow')

const createBookForm = reactive({
  title: '',
  author: '',
  totalQuantity: 1,
})

const borrowForm = reactive({
  bookId: '',
  borrowerName: '',
})

const returnForm = reactive({
  borrowRecordId: '',
})

const borrowerNameByBookId = reactive<Record<number, string>>({})

const filteredBooks = computed(() => {
  const normalizedKeyword = searchKeyword.value.trim().toLowerCase()
  if (!normalizedKeyword) {
    return libraryStore.state.books
  }

  return libraryStore.state.books.filter((book) => {
    const fullText = `${book.id} ${book.title} ${book.author}`.toLowerCase()
    return fullText.includes(normalizedKeyword)
  })
})

onMounted(async () => {
  await libraryStore.loadBooks()
})

async function handleCreateBook(): Promise<void> {
  if (!createBookForm.title.trim() || !createBookForm.author.trim() || createBookForm.totalQuantity < 1) {
    libraryStore.state.errorMessage = '請輸入完整書籍資料。'
    return
  }

  await libraryStore.createBook(
    createBookForm.title.trim(),
    createBookForm.author.trim(),
    createBookForm.totalQuantity,
  )

  createBookForm.title = ''
  createBookForm.author = ''
  createBookForm.totalQuantity = 1
}

async function handleBorrowByForm(): Promise<void> {
  const bookId = Number.parseInt(borrowForm.bookId, 10)
  const borrowerName = borrowForm.borrowerName.trim()

  if (!Number.isInteger(bookId) || bookId <= 0 || !borrowerName) {
    libraryStore.state.errorMessage = '請輸入有效 Book ID 與借閱人姓名。'
    return
  }

  await libraryStore.borrowBook(bookId, borrowerName)
  borrowForm.bookId = ''
  borrowForm.borrowerName = ''
}

async function handleReturnByForm(): Promise<void> {
  const borrowRecordId = Number.parseInt(returnForm.borrowRecordId, 10)
  if (!Number.isInteger(borrowRecordId) || borrowRecordId <= 0) {
    libraryStore.state.errorMessage = '請輸入有效 Borrow Record ID。'
    return
  }

  await libraryStore.returnBook(borrowRecordId)
  returnForm.borrowRecordId = ''
}

async function handleBorrowBook(bookId: number): Promise<void> {
  const borrowerName = borrowerNameByBookId[bookId]?.trim() ?? ''
  if (!borrowerName) {
    libraryStore.state.errorMessage = '請輸入借閱人姓名。'
    return
  }

  await libraryStore.borrowBook(bookId, borrowerName)
  borrowerNameByBookId[bookId] = ''
}

async function handleReturnBook(borrowRecordId: number): Promise<void> {
  await libraryStore.returnBook(borrowRecordId)
}
</script>

<template>
  <div class="page" data-testid="library-page">
    <header class="topbar" data-testid="topbar">
      <div class="brand">
        <div class="brand-icon">LB</div>
        <div>
          <h1>Library Mini Admin</h1>
          <p>管理控制台</p>
        </div>
      </div>

      <div class="search-wrap">
        <input
          v-model="searchKeyword"
          data-testid="global-search-input"
          type="text"
          placeholder="搜尋書名、作者、Book ID..."
        />
      </div>

      <div class="admin-meta">
        <strong>Admin</strong>
        <small>管理員</small>
      </div>
    </header>

    <main class="content">
      <section class="two-column" data-testid="two-column-layout">
        <article class="card" data-testid="transaction-card">
          <div class="tabs">
            <button
              data-testid="borrow-tab"
              type="button"
              :class="{ active: activeTransactionTab === 'borrow' }"
              @click="activeTransactionTab = 'borrow'"
            >
              借書
            </button>
            <button
              data-testid="return-tab"
              type="button"
              :class="{ active: activeTransactionTab === 'return' }"
              @click="activeTransactionTab = 'return'"
            >
              還書
            </button>
          </div>

          <div v-if="activeTransactionTab === 'borrow'" class="form-body" data-testid="borrow-form-card">
            <label>
              Book ID
              <input
                v-model="borrowForm.bookId"
                data-testid="borrow-book-id-input"
                type="number"
                min="1"
                placeholder="請輸入 Book ID"
              />
            </label>
            <label>
              借閱人
              <input
                v-model="borrowForm.borrowerName"
                data-testid="borrow-reader-input"
                type="text"
                placeholder="請輸入借閱人"
              />
            </label>
            <button data-testid="borrow-submit-button" type="button" @click="handleBorrowByForm">確認借出</button>
          </div>

          <div v-else class="form-body" data-testid="return-form-card">
            <label>
              Borrow Record ID
              <input
                v-model="returnForm.borrowRecordId"
                data-testid="return-record-id-input"
                type="number"
                min="1"
                placeholder="請輸入 Borrow Record ID"
              />
            </label>
            <button data-testid="return-submit-button" type="button" @click="handleReturnByForm">確認歸還</button>
          </div>
        </article>

        <article class="card" data-testid="create-book-section">
          <h2>新增書籍</h2>
          <div class="form-body">
            <label>
              書名
              <input
                v-model="createBookForm.title"
                data-testid="book-title-input"
                type="text"
                placeholder="Book title"
              />
            </label>
            <label>
              作者
              <input
                v-model="createBookForm.author"
                data-testid="book-author-input"
                type="text"
                placeholder="Author"
              />
            </label>
            <label>
              數量
              <input
                v-model.number="createBookForm.totalQuantity"
                data-testid="book-total-quantity-input"
                type="number"
                min="1"
              />
            </label>
            <button data-testid="create-book-submit" type="button" @click="handleCreateBook">新增書籍</button>
          </div>
        </article>
      </section>

      <p v-if="libraryStore.state.loading" data-testid="loading-text">Loading...</p>
      <p v-if="libraryStore.state.errorMessage" data-testid="error-message" class="error">
        {{ libraryStore.state.errorMessage }}
      </p>

      <section class="card" data-testid="books-section">
        <div class="table-head">
          <h2>館藏列表</h2>
          <span>共 {{ filteredBooks.length }} 本</span>
        </div>

        <table data-testid="books-table">
          <thead>
            <tr>
              <th>Book ID</th>
              <th>Title</th>
              <th>Author</th>
              <th>Total</th>
              <th>Available</th>
              <th>Status</th>
              <th>Quick Borrow</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="book in filteredBooks" :key="book.id" :data-testid="`book-row-${book.id}`">
              <td>{{ book.id }}</td>
              <td>{{ book.title }}</td>
              <td>{{ book.author }}</td>
              <td>{{ book.totalQuantity }}</td>
              <td :data-testid="`book-available-${book.id}`">{{ book.availableQuantity }}</td>
              <td :data-testid="`book-status-${book.id}`">{{ book.status }}</td>
              <td>
                <div class="inline-form">
                  <input
                    v-model="borrowerNameByBookId[book.id]"
                    :data-testid="`borrower-name-input-${book.id}`"
                    type="text"
                    placeholder="Borrower"
                  />
                  <button
                    :data-testid="`borrow-book-button-${book.id}`"
                    type="button"
                    :disabled="book.availableQuantity <= 0"
                    @click="handleBorrowBook(book.id)"
                  >
                    借出
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="card" data-testid="borrow-records-section">
        <div class="table-head">
          <h2>借閱紀錄（未歸還）</h2>
        </div>
        <ul>
          <li v-for="borrowRecord in activeBorrowRecords" :key="borrowRecord.id" :data-testid="`borrow-record-row-${borrowRecord.id}`">
            <span>
              #{{ borrowRecord.id }} {{ borrowRecord.borrowerName }} borrowed book #{{ borrowRecord.bookId }}
            </span>
            <button
              :data-testid="`return-book-button-${borrowRecord.id}`"
              type="button"
              @click="handleReturnBook(borrowRecord.id)"
            >
              歸還
            </button>
          </li>
        </ul>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page {
  background: #f8fafc;
  min-height: 100vh;
}

.topbar {
  align-items: center;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: grid;
  gap: 16px;
  grid-template-columns: 260px 1fr 140px;
  padding: 16px 24px;
}

.brand {
  align-items: center;
  display: flex;
  gap: 12px;
}

.brand-icon {
  align-items: center;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-radius: 10px;
  color: #ffffff;
  display: flex;
  font-size: 12px;
  font-weight: 700;
  height: 40px;
  justify-content: center;
  width: 40px;
}

.brand h1 {
  font-size: 16px;
  margin: 0;
}

.brand p {
  color: #6b7280;
  font-size: 12px;
  margin: 0;
}

.search-wrap input {
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  width: 100%;
}

.admin-meta {
  text-align: right;
}

.admin-meta strong {
  display: block;
  font-size: 14px;
}

.admin-meta small {
  color: #6b7280;
  font-size: 12px;
}

.content {
  margin: 0 auto;
  max-width: 1200px;
  padding: 24px;
}

.two-column {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 16px;
}

.card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.card h2 {
  font-size: 18px;
  margin: 0 0 12px;
}

.tabs {
  border-bottom: 1px solid #e5e7eb;
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin: -16px -16px 16px;
}

.tabs button {
  background: transparent;
  border: 0;
  cursor: pointer;
  font-weight: 600;
  padding: 12px;
}

.tabs button.active {
  border-bottom: 2px solid #2563eb;
  color: #2563eb;
}

.form-body {
  display: grid;
  gap: 12px;
}

.form-body label {
  color: #374151;
  display: grid;
  font-size: 13px;
  gap: 6px;
}

.form-body input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
}

button {
  background: #2563eb;
  border: 0;
  border-radius: 8px;
  color: #ffffff;
  cursor: pointer;
  font-weight: 600;
  padding: 10px 12px;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.error {
  color: #b91c1c;
  font-weight: 600;
  margin: 8px 0 16px;
}

.table-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

table {
  border-collapse: collapse;
  width: 100%;
}

th,
td {
  border-bottom: 1px solid #e5e7eb;
  padding: 10px;
  text-align: left;
}

th {
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
}

.inline-form {
  display: grid;
  gap: 8px;
  grid-template-columns: 1fr auto;
}

.inline-form input {
  min-width: 140px;
}

ul {
  display: grid;
  gap: 8px;
  list-style: none;
  margin: 0;
  padding: 0;
}

li {
  align-items: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
}

@media (max-width: 960px) {
  .topbar {
    grid-template-columns: 1fr;
  }

  .admin-meta {
    text-align: left;
  }

  .two-column {
    grid-template-columns: 1fr;
  }
}
</style>
