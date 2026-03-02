<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { BorrowBookRequest } from '../api/libraryApi'

const emit = defineEmits<{
  submit: [payload: BorrowBookRequest]
}>()

const props = defineProps<{
  seededIsbn?: string
}>()

const form = reactive<BorrowBookRequest>({
  readerId: '',
  isbn: '',
  dueDate: '',
})

watch(
  () => props.seededIsbn,
  (value) => {
    if (value) {
      form.isbn = value
    }
  },
)

function onSubmit() {
  emit('submit', {
    readerId: form.readerId,
    isbn: form.isbn,
    dueDate: form.dueDate ? form.dueDate : undefined,
  })
}
</script>

<template>
  <section class="borrow-form">
    <h2>借書</h2>
    <label>
      讀者 ID
      <input v-model="form.readerId" data-testid="loan-borrow-readerId" placeholder="Reader ID" />
    </label>
    <label>
      ISBN
      <input v-model="form.isbn" data-testid="loan-borrow-isbn" placeholder="ISBN" />
    </label>
    <label>
      到期日
      <input v-model="form.dueDate" data-testid="loan-borrow-dueDate" type="date" />
    </label>
    <button type="button" data-testid="loan-borrow-submit" @click="onSubmit">確認借出</button>
  </section>
</template>

<style scoped>
.borrow-form {
  display: grid;
  gap: 12px;
}

.borrow-form h2 {
  margin: 0;
  font-size: 18px;
}

.borrow-form label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.borrow-form input {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.borrow-form input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px #dbeafe;
}

.borrow-form button {
  margin-top: 6px;
  border: 0;
  border-radius: 10px;
  padding: 10px 12px;
  background: #2563eb;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
}
</style>
