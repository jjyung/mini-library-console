<script setup lang="ts">
import { onMounted, ref } from 'vue'

import AddBookForm from '@/components/AddBookForm.vue'
import BookTable from '@/components/BookTable.vue'
import TopBar from '@/components/TopBar.vue'
import TransactionCard from '@/components/TransactionCard.vue'
import { useLibraryAdmin } from '@/features/library/useLibraryAdmin'

import type { BorrowForm, CreateBookForm, ReturnForm } from '@/core/domain/libraryTypes'

const {
  books,
  catalogueState,
  catalogueError,
  addBookStatus,
  borrowStatus,
  returnStatus,
  toast,
  isLoading,
  isCreating,
  isBorrowing,
  isReturning,
  isMutating,
  loadBooks,
  createBook,
  borrowBook,
  returnBook,
} = useLibraryAdmin()

const addBookResetKey = ref(0)
const borrowIsbnPrefill = ref('')
const returnIsbnPrefill = ref('')

async function handleCreateBook(form: CreateBookForm) {
  if (await createBook(form)) addBookResetKey.value += 1
}

async function handleBorrow(form: BorrowForm) {
  await borrowBook(form)
}

async function handleReturn(form: ReturnForm) {
  await returnBook(form)
}

function handleQuickBorrow(isbn: string) {
  returnIsbnPrefill.value = ''
  borrowIsbnPrefill.value = isbn
}

function handleQuickReturn(isbn: string) {
  borrowIsbnPrefill.value = ''
  returnIsbnPrefill.value = isbn
}

onMounted(() => {
  void loadBooks()
})
</script>

<template>
  <div class="library-page" data-testid="library-admin-page">
    <TopBar />

    <main class="page-shell">
      <div class="workspace-grid">
        <TransactionCard
          :borrow-status="borrowStatus"
          :return-status="returnStatus"
          :busy="isBorrowing || isReturning"
          :borrow-isbn-prefill="borrowIsbnPrefill"
          :return-isbn-prefill="returnIsbnPrefill"
          @borrow="handleBorrow"
          @return="handleReturn"
        />
        <AddBookForm :status="addBookStatus" :busy="isCreating" :reset-key="addBookResetKey" @submit="handleCreateBook" />
      </div>

      <section class="catalogue-region" aria-live="polite">
        <div v-if="catalogueState === 'loading'" data-testid="catalogue-loading" class="card state-card">
          <div class="loading-mark" aria-hidden="true" />
          <h2>載入館藏中</h2>
          <p>正在取得最新館藏狀態…</p>
        </div>

        <div v-else-if="catalogueState === 'error'" data-testid="catalogue-error" class="card state-card state-card--error" role="alert">
          <div class="state-icon state-icon--error" aria-hidden="true">!</div>
          <h2>館藏載入失敗</h2>
          <p>{{ catalogueError?.message }}</p>
          <small v-if="catalogueError?.code">錯誤碼 {{ catalogueError.code }}<template v-if="catalogueError.traceId"> · {{ catalogueError.traceId }}</template></small>
          <button type="button" class="secondary-button" data-testid="catalogue-retry" :disabled="isLoading || isMutating" @click="void loadBooks()">
            {{ isLoading ? '重新載入中…' : '重新載入' }}
          </button>
        </div>

        <BookTable v-else :books="books" @quick-borrow="handleQuickBorrow" @quick-return="handleQuickReturn" />
      </section>
    </main>

    <Transition name="toast">
      <div
        v-if="toast"
        :data-testid="toast.type === 'success' ? 'catalogue-success-toast' : 'catalogue-error-toast'"
        class="toast"
        :class="`toast--${toast.type}`"
        role="status"
        aria-live="assertive"
      >
        <span class="feedback-icon" aria-hidden="true">{{ toast.type === 'success' ? '✓' : '!' }}</span>
        <div>
          <p>{{ toast.message }}</p>
          <small v-if="toast.code">錯誤碼 {{ toast.code }}<template v-if="toast.traceId"> · {{ toast.traceId }}</template></small>
        </div>
      </div>
    </Transition>
  </div>
</template>
