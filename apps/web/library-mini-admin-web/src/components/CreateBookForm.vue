<script setup lang="ts">
import { reactive } from 'vue'
import type { CreateBookRequest } from '../api/libraryApi'

const emit = defineEmits<{
  submit: [payload: CreateBookRequest]
}>()

const form = reactive<CreateBookRequest>({
  title: '',
  isbn: '',
  author: '',
  category: 'Software',
  quantity: 1,
  shelfStatus: 'ON_SHELF',
})

function onSubmit() {
  emit('submit', { ...form })
}
</script>

<template>
  <section class="create-book-form">
    <h2>新增書籍</h2>
    <label>
      書名
      <input v-model="form.title" data-testid="book-create-title" placeholder="Title" />
    </label>
    <label>
      ISBN
      <input v-model="form.isbn" data-testid="book-create-isbn" placeholder="ISBN" />
    </label>
    <label>
      作者
      <input v-model="form.author" data-testid="book-create-author" placeholder="Author" />
    </label>
    <label>
      分類
      <input v-model="form.category" data-testid="book-create-category" placeholder="Category" />
    </label>
    <label>
      數量
      <input v-model.number="form.quantity" data-testid="book-create-quantity" type="number" min="1" />
    </label>
    <label>
      上架狀態
      <select v-model="form.shelfStatus" data-testid="book-create-shelfStatus">
        <option value="ON_SHELF">ON_SHELF</option>
        <option value="OFF_SHELF">OFF_SHELF</option>
      </select>
    </label>
    <button type="button" data-testid="book-create-submit" @click="onSubmit">新增書籍</button>
  </section>
</template>

<style scoped>
.create-book-form {
  display: grid;
  gap: 12px;
}

.create-book-form h2 {
  margin: 0;
  font-size: 18px;
}

.create-book-form label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.create-book-form input,
.create-book-form select {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.create-book-form input:focus,
.create-book-form select:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px #e0e7ff;
}

.create-book-form button {
  margin-top: 6px;
  border: 0;
  border-radius: 10px;
  padding: 10px 12px;
  background: #4f46e5;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
}
</style>
