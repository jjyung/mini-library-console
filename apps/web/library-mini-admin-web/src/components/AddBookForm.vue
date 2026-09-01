<script setup lang="ts">
import { reactive } from 'vue'
import type { CreateBookRequest } from '@/types/library'
import type { Feedback } from '@/types/api'

defineProps<{ busy: boolean; feedback: Feedback | null }>()
const emit = defineEmits<{ submit: [payload: CreateBookRequest] }>()
const categoryOptions = [
  { value: 'literature', label: '文學' }, { value: 'science', label: '科學' }, { value: 'technology', label: '科技' },
  { value: 'history', label: '歷史' }, { value: 'art', label: '藝術' }, { value: 'philosophy', label: '哲學' },
  { value: 'business', label: '商業' }, { value: 'education', label: '教育' },
]
const form = reactive<CreateBookRequest>({ title: '', isbn: '', author: '', category: 'literature', quantity: 1, isActive: true })
function submitForm() { emit('submit', { ...form, author: form.author?.trim() || undefined }) }
</script>

<template>
  <section class="panel" data-testid="add-book-form">
    <div class="panel-header"><span class="panel-icon blue">＋</span><h2>新增書籍</h2></div>
    <form class="form-body" @submit.prevent="submitForm">
      <label>書名 <span>*</span><input data-testid="add-book-title" v-model="form.title" required placeholder="請輸入書名" /></label>
      <label>ISBN <span>*</span><input data-testid="add-book-isbn" v-model="form.isbn" required placeholder="請輸入 ISBN" /></label>
      <label>作者（可選）<input data-testid="add-book-author" v-model="form.author" placeholder="請輸入作者" /></label>
      <label>分類 <span>*</span><select data-testid="add-book-category" v-model="form.category"><option v-for="option in categoryOptions" :key="option.value" :value="option.value">{{ option.label }}</option></select></label>
      <label>數量 <span>*</span><input data-testid="add-book-quantity" v-model.number="form.quantity" type="number" min="1" required /></label>
      <div class="toggle-row"><div><strong>上架狀態</strong><small>書籍是否可供借閱</small></div><button data-testid="add-book-active" type="button" class="switch" :class="{ on: form.isActive }" role="switch" :aria-checked="form.isActive" @click="form.isActive = !form.isActive"><span /></button></div>
      <p v-if="feedback" data-testid="add-book-result" class="feedback" :class="feedback.type">{{ feedback.message }}</p>
      <button data-testid="add-book-submit" class="primary-button blue-button" type="submit" :disabled="busy">新增書籍</button>
    </form>
  </section>
</template>
