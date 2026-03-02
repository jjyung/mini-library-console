<script setup lang="ts">
import { onMounted, ref } from 'vue'
import BookTable from './components/BookTable.vue'
import BorrowForm from './components/BorrowForm.vue'
import CreateBookForm from './components/CreateBookForm.vue'
import MessagePanel from './components/MessagePanel.vue'
import ReturnForm from './components/ReturnForm.vue'
import SearchPanel from './components/SearchPanel.vue'
import { useLibraryStore } from './store/libraryStore'

const { state, actions } = useLibraryStore()
const activeTransactionTab = ref<'borrow' | 'return'>('borrow')
const borrowSeededIsbn = ref('')
const returnSeededIsbn = ref('')

onMounted(async () => {
  await actions.refreshBooks()
})

function activateBorrowTab(isbn: string) {
  activeTransactionTab.value = 'borrow'
  borrowSeededIsbn.value = isbn
}

function activateReturnTab(isbn: string) {
  activeTransactionTab.value = 'return'
  returnSeededIsbn.value = isbn
}
</script>

<template>
  <main class="app-shell">
    <header class="top-bar" data-testid="topbar">
      <div class="brand">
        <div class="logo">LB</div>
        <div>
          <h1 data-testid="page-title">Library Mini Admin</h1>
          <p>管理控制台</p>
        </div>
      </div>

      <SearchPanel compact @search="actions.searchBooksAction" @clear="actions.refreshBooks" />

      <div class="admin-area">
        <button type="button" data-testid="book-list-refresh" @click="() => actions.refreshBooks()">重新整理</button>
        <span>Admin</span>
      </div>
    </header>

    <section class="main-grid">
      <article class="transaction-card" data-testid="transaction-card">
        <div class="card-tabs">
          <button
            type="button"
            data-testid="transaction-tab-borrow"
            :class="{ active: activeTransactionTab === 'borrow' }"
            @click="activeTransactionTab = 'borrow'"
          >
            借書
          </button>
          <button
            type="button"
            data-testid="transaction-tab-return"
            :class="{ active: activeTransactionTab === 'return' }"
            @click="activeTransactionTab = 'return'"
          >
            還書
          </button>
        </div>

        <div v-show="activeTransactionTab === 'borrow'" class="card-body">
          <BorrowForm :seeded-isbn="borrowSeededIsbn" @submit="actions.borrowBookAction" />
        </div>
        <div v-show="activeTransactionTab === 'return'" class="card-body">
          <ReturnForm :seeded-isbn="returnSeededIsbn" @submit="actions.returnBookAction" />
        </div>
      </article>

      <article class="add-book-card" data-testid="add-book-form">
        <CreateBookForm @submit="actions.createBookAction" />
      </article>
    </section>

    <section class="table-area" data-testid="book-table">
      <BookTable :books="state.books" @quick-borrow="activateBorrowTab" @quick-return="activateReturnTab" />
    </section>

    <MessagePanel v-if="state.message" :code="state.message.code" :text="state.message.text" class="message-panel" />
  </main>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
  padding: 0 0 24px;
}

.top-bar {
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px 20px;
  display: grid;
  grid-template-columns: 280px 1fr auto;
  align-items: center;
  gap: 14px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #ffffff;
  font-weight: 700;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.brand h1 {
  margin: 0;
  font-size: 18px;
  line-height: 1.2;
}

.brand p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}

.admin-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-area button {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 8px 10px;
  background: #ffffff;
  cursor: pointer;
}

.admin-area span {
  font-size: 13px;
  color: #111827;
  font-weight: 600;
}

.main-grid {
  margin: 24px 20px 16px;
  display: grid;
  grid-template-columns: minmax(300px, 1fr) minmax(300px, 1fr);
  gap: 16px;
}

.transaction-card,
.add-book-card,
.table-area,
.message-panel {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #ffffff;
}

.card-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-bottom: 1px solid #e5e7eb;
}

.card-tabs button {
  border: 0;
  background: #f8fafc;
  padding: 12px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
}

.card-tabs button.active {
  background: #ffffff;
  color: #0f172a;
  box-shadow: inset 0 -3px 0 #2563eb;
}

.card-body,
.add-book-card {
  padding: 16px;
}

.table-area {
  margin: 0 20px;
}

.message-panel {
  margin: 16px 20px 0;
  padding: 12px 16px;
}

@media (max-width: 1024px) {
  .top-bar {
    grid-template-columns: 1fr;
  }

  .main-grid {
    grid-template-columns: 1fr;
  }
}
</style>
