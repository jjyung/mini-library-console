<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import { libraryStore } from '@/stores/libraryStore'

const formState = reactive({
  title: '',
  author: '',
  totalCopies: 1,
  borrowerNameByBookId: {} as Record<number, string>,
  returnedByByRecordId: {} as Record<number, string>,
})

const activeTransactionTab = ref<'borrow' | 'return'>('borrow')
const transactionBorrowState = reactive({
  bookId: 0,
  borrowerName: '',
})
const transactionReturnState = reactive({
  borrowRecordId: 0,
  returnedBy: '',
})

const openBorrowRecords = computed(() => {
  return libraryStore.state.borrowRecords.filter((borrowRecordItem) => borrowRecordItem.status === 'BORROWED')
})

const submitCreateBook = async (): Promise<void> => {
  await libraryStore.actions.createBookAction({
    title: formState.title,
    author: formState.author,
    totalCopies: formState.totalCopies,
  })

  if (libraryStore.state.errorMessage === '') {
    formState.title = ''
    formState.author = ''
    formState.totalCopies = 1
  }
}

const submitBorrowBook = async (bookId: number): Promise<void> => {
  const borrowerName = formState.borrowerNameByBookId[bookId] ?? ''
  await libraryStore.actions.borrowBookAction({
    bookId,
    borrowerName,
  })

  if (libraryStore.state.errorMessage === '') {
    formState.borrowerNameByBookId[bookId] = ''
  }
}

const submitReturnBook = async (borrowRecordId: number): Promise<void> => {
  const returnedBy = formState.returnedByByRecordId[borrowRecordId] ?? ''
  await libraryStore.actions.returnBorrowRecordAction(borrowRecordId, returnedBy)

  if (libraryStore.state.errorMessage === '') {
    formState.returnedByByRecordId[borrowRecordId] = ''
  }
}

const submitTransactionBorrow = async (): Promise<void> => {
  if (transactionBorrowState.bookId <= 0) {
    return
  }
  await libraryStore.actions.borrowBookAction({
    bookId: transactionBorrowState.bookId,
    borrowerName: transactionBorrowState.borrowerName,
  })
  if (libraryStore.state.errorMessage === '') {
    transactionBorrowState.borrowerName = ''
  }
}

const submitTransactionReturn = async (): Promise<void> => {
  if (transactionReturnState.borrowRecordId <= 0) {
    return
  }
  await libraryStore.actions.returnBorrowRecordAction(
    transactionReturnState.borrowRecordId,
    transactionReturnState.returnedBy,
  )
  if (libraryStore.state.errorMessage === '') {
    transactionReturnState.returnedBy = ''
  }
}

onMounted(async () => {
  await libraryStore.actions.initialize()
})
</script>

<template>
  <div class="page-shell" data-testid="page-root">
    <header class="topbar">
      <div class="topbar-brand">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
              d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
        <div>
          <h1 data-testid="page-title">Library Mini Admin</h1>
          <p class="brand-subtitle">管理控制台</p>
        </div>
      </div>

      <label class="topbar-search" aria-label="global-search">
        <span>搜尋</span>
        <input placeholder="搜尋書名、作者..." />
      </label>

      <div class="topbar-admin">
        <div>
          <p class="admin-name">Admin</p>
          <p class="admin-role">管理員</p>
        </div>
        <div class="admin-avatar">A</div>
      </div>
    </header>

    <main class="content">
      <p v-if="libraryStore.state.infoMessage" data-testid="info-message" class="alert alert-success">
        {{ libraryStore.state.infoMessage }}
      </p>
      <p v-if="libraryStore.state.errorMessage" data-testid="error-message" class="alert alert-error">
        {{ libraryStore.state.errorMessage }}
      </p>

      <section class="hero-grid">
        <article class="card transaction-card">
          <div class="card-header card-header-tabs">
            <button
              class="tab-button"
              :class="{ active: activeTransactionTab === 'borrow' }"
              type="button"
              @click="activeTransactionTab = 'borrow'"
            >
              借書
            </button>
            <button
              class="tab-button tab-return"
              :class="{ active: activeTransactionTab === 'return' }"
              type="button"
              @click="activeTransactionTab = 'return'"
            >
              還書
            </button>
          </div>

          <div class="card-body">
            <form v-if="activeTransactionTab === 'borrow'" class="stack-form" @submit.prevent="submitTransactionBorrow">
              <label>
                選擇書籍
                <select v-model.number="transactionBorrowState.bookId">
                  <option :value="0">請選擇書籍</option>
                  <option
                    v-for="bookItem in libraryStore.state.books"
                    :key="`borrow-option-${bookItem.id}`"
                    :value="bookItem.id"
                  >
                    {{ bookItem.title }}（可借 {{ bookItem.availableCopies }}）
                  </option>
                </select>
              </label>

              <label>
                借閱人姓名
                <input v-model="transactionBorrowState.borrowerName" placeholder="請輸入借閱人" maxlength="80" />
              </label>

              <button class="btn-primary" type="submit" :disabled="libraryStore.state.loading">確認借出</button>
            </form>

            <form v-else class="stack-form" @submit.prevent="submitTransactionReturn">
              <label>
                借閱紀錄
                <select v-model.number="transactionReturnState.borrowRecordId">
                  <option :value="0">請選擇借閱紀錄</option>
                  <option
                    v-for="borrowRecordItem in openBorrowRecords"
                    :key="`return-option-${borrowRecordItem.id}`"
                    :value="borrowRecordItem.id"
                  >
                    #{{ borrowRecordItem.id }} {{ borrowRecordItem.bookTitle }} / {{ borrowRecordItem.borrowerName }}
                  </option>
                </select>
              </label>

              <label>
                歸還人
                <input v-model="transactionReturnState.returnedBy" placeholder="請輸入歸還人" maxlength="80" />
              </label>

              <button class="btn-success" type="submit" :disabled="libraryStore.state.loading">確認歸還</button>
            </form>
          </div>
        </article>

        <article class="card" data-testid="create-book-section">
          <div class="card-header">
            <h2>新增書籍</h2>
          </div>
          <div class="card-body">
            <form class="stack-form" @submit.prevent="submitCreateBook">
              <label>
                書名
                <input
                  v-model="formState.title"
                  data-testid="input-book-title"
                  name="title"
                  required
                  maxlength="120"
                  placeholder="請輸入書名"
                />
              </label>

              <label>
                作者
                <input
                  v-model="formState.author"
                  data-testid="input-book-author"
                  name="author"
                  required
                  maxlength="80"
                  placeholder="請輸入作者"
                />
              </label>

              <label>
                庫存
                <input
                  v-model.number="formState.totalCopies"
                  data-testid="input-book-total-copies"
                  name="totalCopies"
                  type="number"
                  min="1"
                  max="9999"
                  required
                />
              </label>

              <button data-testid="button-create-book" class="btn-indigo" type="submit" :disabled="libraryStore.state.loading">
                新增書籍
              </button>
            </form>
          </div>
        </article>
      </section>

      <section class="card" data-testid="book-list-section">
        <div class="card-header space-between">
          <h2>館藏列表</h2>
          <span class="muted-label">共 {{ libraryStore.state.books.length }} 本</span>
        </div>
        <div class="card-body table-scroll">
          <table data-testid="book-table">
            <thead>
              <tr>
                <th>書名</th>
                <th>作者</th>
                <th>狀態</th>
                <th>可借/總數</th>
                <th>借書</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="bookItem in libraryStore.state.books" :key="bookItem.id" :data-testid="`book-row-${bookItem.id}`">
                <td>{{ bookItem.title }}</td>
                <td>{{ bookItem.author }}</td>
                <td>
                  <span class="sr-only" :data-testid="`book-status-${bookItem.id}`">{{ bookItem.status }}</span>
                  <span
                    class="status-badge"
                    :class="bookItem.status === 'AVAILABLE' ? 'status-available' : 'status-borrowed'"
                  >
                    {{ bookItem.status === 'AVAILABLE' ? '可借閱' : '已借出' }}
                  </span>
                </td>
                <td>
                  <strong :class="bookItem.availableCopies === 0 ? 'text-danger' : 'text-success'">
                    <span :data-testid="`book-available-${bookItem.id}`">{{ bookItem.availableCopies }}</span>
                  </strong>
                  / {{ bookItem.totalCopies }}
                </td>
                <td>
                  <div class="inline-action">
                    <input
                      v-model="formState.borrowerNameByBookId[bookItem.id]"
                      :data-testid="`input-borrower-${bookItem.id}`"
                      placeholder="借閱人"
                      maxlength="80"
                    />
                    <button
                      :data-testid="`button-borrow-${bookItem.id}`"
                      type="button"
                      :disabled="bookItem.availableCopies === 0 || libraryStore.state.loading"
                      class="btn-primary"
                      @click="submitBorrowBook(bookItem.id)"
                    >
                      借出
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="card" data-testid="borrow-record-list-section">
        <div class="card-header">
          <h2>借閱紀錄</h2>
        </div>
        <div class="card-body table-scroll">
          <table data-testid="borrow-record-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>書名</th>
                <th>借閱人</th>
                <th>借出時間</th>
                <th>歸還時間</th>
                <th>狀態</th>
                <th>還書</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="borrowRecordItem in libraryStore.state.borrowRecords"
                :key="borrowRecordItem.id"
                :data-testid="`borrow-record-row-${borrowRecordItem.id}`"
              >
                <td>{{ borrowRecordItem.id }}</td>
                <td>{{ borrowRecordItem.bookTitle }}</td>
                <td>{{ borrowRecordItem.borrowerName }}</td>
                <td>{{ borrowRecordItem.borrowedAt }}</td>
                <td>{{ borrowRecordItem.returnedAt ?? '-' }}</td>
                <td>
                  <span class="sr-only" :data-testid="`borrow-record-status-${borrowRecordItem.id}`">{{ borrowRecordItem.status }}</span>
                  <span
                    class="status-badge"
                    :class="borrowRecordItem.status === 'BORROWED' ? 'status-borrowed' : 'status-available'"
                  >
                    {{ borrowRecordItem.status === 'BORROWED' ? '借閱中' : '已歸還' }}
                  </span>
                </td>
                <td>
                  <div class="inline-action" v-if="borrowRecordItem.status === 'BORROWED'">
                    <input
                      v-model="formState.returnedByByRecordId[borrowRecordItem.id]"
                      :data-testid="`input-returned-by-${borrowRecordItem.id}`"
                      placeholder="歸還人"
                      maxlength="80"
                    />
                    <button
                      :data-testid="`button-return-${borrowRecordItem.id}`"
                      type="button"
                      :disabled="libraryStore.state.loading"
                      class="btn-success"
                      @click="submitReturnBook(borrowRecordItem.id)"
                    >
                      歸還
                    </button>
                  </div>
                  <span v-else class="muted-label">已歸還</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page-shell {
  min-height: 100vh;
  background: radial-gradient(circle at 0 0, #e6f0ff 0%, #f5f7fb 42%, #f7f8fb 100%);
  color: #1b2b3b;
  font-family: "Noto Sans TC", "PingFang TC", "Microsoft JhengHei", sans-serif;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  border-bottom: 1px solid #e2e8f2;
  background: #ffffff;
  padding: 14px 24px;
}

.topbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  display: grid;
  height: 42px;
  width: 42px;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
}

.brand-logo svg {
  height: 24px;
  width: 24px;
}

h1 {
  margin: 0;
  font-size: 17px;
}

.brand-subtitle {
  margin: 2px 0 0;
  color: #78879a;
  font-size: 12px;
}

.topbar-search {
  display: grid;
  flex: 1;
  max-width: 560px;
  gap: 6px;
  color: #556476;
  font-size: 13px;
}

.topbar-search input {
  border: 1px solid #d7e0eb;
  border-radius: 10px;
  background: #f8fafc;
  padding: 10px 12px;
}

.topbar-admin {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-name {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
}

.admin-role {
  margin: 0;
  font-size: 12px;
  color: #7f8da0;
  text-align: right;
}

.admin-avatar {
  display: grid;
  height: 38px;
  width: 38px;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(140deg, #7c3aed, #ec4899);
  color: #ffffff;
  font-weight: 700;
}

.content {
  margin: 0 auto;
  max-width: 1200px;
  padding: 24px;
  display: grid;
  gap: 18px;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.card {
  border: 1px solid #dee6f1;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(16, 42, 67, 0.06);
}

.card-header {
  border-bottom: 1px solid #ebf0f5;
  padding: 16px 18px;
}

.card-header h2 {
  margin: 0;
  font-size: 17px;
}

.card-header.space-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  padding: 0;
}

.tab-button {
  border: 0;
  border-bottom: 2px solid transparent;
  background: #ffffff;
  color: #58687c;
  font-size: 14px;
  font-weight: 700;
  padding: 14px;
  cursor: pointer;
}

.tab-button.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}

.tab-button.tab-return.active {
  color: #059669;
  border-bottom-color: #059669;
}

.card-body {
  padding: 18px;
}

.stack-form {
  display: grid;
  gap: 12px;
}

label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
}

input,
select,
button {
  font: inherit;
}

input,
select {
  border: 1px solid #d2dbe8;
  border-radius: 10px;
  padding: 10px 12px;
  background: #fbfcfe;
}

button {
  border: 0;
  border-radius: 10px;
  color: #ffffff;
  cursor: pointer;
  padding: 10px 12px;
  font-weight: 700;
}

button:disabled {
  background: #95a6bc;
  cursor: not-allowed;
}

.btn-primary {
  background: #2563eb;
}

.btn-success {
  background: #059669;
}

.btn-indigo {
  background: #4f46e5;
}

.alert {
  margin: 0;
  border-radius: 10px;
  padding: 10px 12px;
  font-weight: 600;
}

.alert-success {
  background: #ecfdf3;
  color: #11703e;
}

.alert-error {
  background: #fff1f2;
  color: #b4233a;
}

.table-scroll {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  border-bottom: 1px solid #ebf0f6;
  padding: 10px 8px;
  text-align: left;
  vertical-align: middle;
  font-size: 13px;
}

th {
  color: #68788d;
  font-weight: 700;
  background: #f8fafc;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.status-available {
  background: #dcfce7;
  color: #166534;
}

.status-borrowed {
  background: #dbeafe;
  color: #1e40af;
}

.inline-action {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  min-width: 210px;
}

.muted-label {
  color: #78889d;
  font-size: 12px;
}

.text-success {
  color: #15803d;
}

.text-danger {
  color: #b91c1c;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 980px) {
  .topbar {
    flex-wrap: wrap;
  }

  .topbar-search {
    order: 3;
    min-width: 100%;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .content {
    padding: 14px;
  }

  .inline-action {
    grid-template-columns: 1fr;
  }
}
</style>
