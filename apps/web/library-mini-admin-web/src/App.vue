<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AddBookForm from '@/components/AddBookForm.vue'
import BookTable from '@/components/BookTable.vue'
import TopBar from '@/components/TopBar.vue'
import TransactionCard from '@/components/TransactionCard.vue'
import { useLibraryStore } from '@/composables/useLibraryStore'
import type { CheckoutBookRequest, CreateBookRequest, ReturnBookRequest } from '@/types/library'

const store = useLibraryStore()
const prefilledIsbn = ref('')
const isBusy = computed(() => store.isLoading.value)

onMounted(() => store.loadBooks())
function addBook(payload: CreateBookRequest) { void store.addBook(payload) }
function borrowBook(payload: CheckoutBookRequest) { void store.borrowBook(payload) }
function returnBook(payload: ReturnBookRequest) { void store.returnBook(payload) }
function quickBorrow(isbn: string) { prefilledIsbn.value = isbn }
function quickReturn(isbn: string) { void store.returnBook({ isbn }) }
</script>

<template>
  <div class="app-shell" data-testid="library-admin-page">
    <TopBar />
    <main class="page-content">
      <div class="page-intro"><div><p class="eyebrow">SHARED LIBRARY</p><h2>管理共享書櫃</h2><p>建立館藏、追蹤借閱，讓每一本書都有清楚的狀態。</p></div><span class="book-count">{{ store.books.value.length }} 本館藏</span></div>
      <p v-if="store.listError.value" class="global-feedback error" data-testid="global-error">{{ store.listError.value.message }}</p>
      <div class="top-grid">
        <TransactionCard :busy="isBusy" :borrow-feedback="store.operationFeedback.borrow" :return-feedback="store.operationFeedback.return" :prefilled-isbn="prefilledIsbn" @borrow="borrowBook" @return="returnBook" />
        <AddBookForm :busy="isBusy" :feedback="store.operationFeedback.addBook" @submit="addBook" />
      </div>
      <BookTable :books="store.books.value" @quick-borrow="quickBorrow" @quick-return="quickReturn" />
    </main>
  </div>
</template>
