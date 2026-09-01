<script setup lang="ts">
import type { Book } from '@/types/library'
defineProps<{ books: Book[] }>()
const emit = defineEmits<{ quickBorrow: [isbn: string]; quickReturn: [isbn: string] }>()
const categoryLabels: Record<string, string> = { literature: '文學', science: '科學', technology: '科技', history: '歷史', art: '藝術', philosophy: '哲學', business: '商業', education: '教育' }
const statusLabels: Record<string, string> = { available: '可借閱', borrowed: '已借出', inactive: '未上架' }
</script>

<template>
  <section class="panel catalogue" data-testid="book-table">
    <div class="panel-header table-heading"><h2>館藏列表</h2><span>共 {{ books.length }} 本</span></div>
    <div v-if="books.length === 0" data-testid="book-empty-state" class="empty-state"><div class="empty-icon">▱</div><strong>尚無館藏</strong><p>請使用右側表單新增書籍</p></div>
    <div v-else class="table-scroll"><table><thead><tr><th>書名</th><th>ISBN</th><th>作者</th><th>分類</th><th>狀態</th><th>可借數/總數</th><th>動作</th></tr></thead><tbody><tr v-for="book in books" :key="book.bookId" :data-testid="`book-row-${book.bookId}`"><td><strong>{{ book.title }}</strong></td><td class="mono">{{ book.isbn }}</td><td>{{ book.author || '-' }}</td><td>{{ categoryLabels[book.category] || book.category }}</td><td><span :data-testid="`book-status-${book.bookId}`" class="badge" :class="book.status">{{ statusLabels[book.status] }}</span></td><td><strong :class="{ zero: book.availableCount === 0 }">{{ book.availableCount }}</strong> / {{ book.totalCount }}</td><td class="actions"><button :data-testid="`quick-borrow-${book.bookId}`" class="small-button borrow" :disabled="book.availableCount === 0 || !book.isActive" @click="emit('quickBorrow', book.isbn)">借出</button><button :data-testid="`quick-return-${book.bookId}`" class="small-button return" :disabled="book.availableCount === book.totalCount" @click="emit('quickReturn', book.isbn)">歸還</button></td></tr></tbody></table></div>
  </section>
</template>
