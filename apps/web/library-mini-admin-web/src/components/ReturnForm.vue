<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ReturnBookRequest } from '../api/libraryApi'

const emit = defineEmits<{
  submit: [payload: ReturnBookRequest]
}>()

const props = defineProps<{
  seededIsbn?: string
}>()

const form = reactive<ReturnBookRequest>({
  isbn: '',
  readerId: '',
  returnedAt: '',
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
    isbn: form.isbn,
    readerId: form.readerId ? form.readerId : undefined,
    returnedAt: form.returnedAt ? form.returnedAt : undefined,
  })
}
</script>

<template>
  <section class="return-form">
    <h2>還書</h2>
    <label>
      ISBN
      <input v-model="form.isbn" data-testid="loan-return-isbn" placeholder="ISBN" />
    </label>
    <label>
      讀者 ID（選填）
      <input v-model="form.readerId" data-testid="loan-return-readerId" placeholder="Reader ID (optional)" />
    </label>
    <label>
      歸還時間（選填）
      <input v-model="form.returnedAt" data-testid="loan-return-returnedAt" placeholder="2026-02-26T00:00:00Z" />
    </label>
    <button type="button" data-testid="loan-return-submit" @click="onSubmit">確認歸還</button>
  </section>
</template>

<style scoped>
.return-form {
  display: grid;
  gap: 12px;
}

.return-form h2 {
  margin: 0;
  font-size: 18px;
}

.return-form label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.return-form input {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.return-form input:focus {
  border-color: #15803d;
  box-shadow: 0 0 0 3px #dcfce7;
}

.return-form button {
  margin-top: 6px;
  border: 0;
  border-radius: 10px;
  padding: 10px 12px;
  background: #15803d;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
}
</style>
