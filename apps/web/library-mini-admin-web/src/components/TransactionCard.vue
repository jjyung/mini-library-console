<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { CheckoutBookRequest, ReturnBookRequest } from '@/types/library'
import type { Feedback } from '@/types/api'

const props = defineProps<{ busy: boolean; borrowFeedback: Feedback | null; returnFeedback: Feedback | null; prefilledIsbn: string }>()
const emit = defineEmits<{ borrow: [payload: CheckoutBookRequest]; return: [payload: ReturnBookRequest] }>()
const activeTab = ref<'borrow' | 'return'>('borrow')
const borrowForm = reactive<CheckoutBookRequest>({ readerId: '', isbn: '', dueDate: '' })
const returnForm = reactive<ReturnBookRequest>({ isbn: '', readerId: '' })
watch(() => props.prefilledIsbn, (isbn) => { if (isbn) { activeTab.value = 'borrow'; borrowForm.isbn = isbn } })
function submitBorrow() { emit('borrow', { ...borrowForm, dueDate: borrowForm.dueDate || undefined }) }
function submitReturn() { emit('return', { ...returnForm, readerId: returnForm.readerId || undefined }) }
</script>

<template>
  <section class="panel" data-testid="transaction-card">
    <div class="tabs"><button data-testid="borrow-tab" :class="{ active: activeTab === 'borrow' }" type="button" @click="activeTab = 'borrow'">▣ 借書</button><button data-testid="return-tab" :class="{ active: activeTab === 'return' }" type="button" @click="activeTab = 'return'">↻ 還書</button></div>
    <form v-if="activeTab === 'borrow'" data-testid="borrow-form" class="form-body" @submit.prevent="submitBorrow">
      <label>讀者 ID <span>*</span><input data-testid="borrow-reader-id" v-model="borrowForm.readerId" required placeholder="請輸入讀者 ID" /></label>
      <label>ISBN <span>*</span><input data-testid="borrow-isbn" v-model="borrowForm.isbn" required placeholder="請輸入 ISBN" /></label>
      <label>到期日（可選）<input data-testid="borrow-due-date" v-model="borrowForm.dueDate" type="date" /></label>
      <p v-if="borrowFeedback" data-testid="borrow-result" class="feedback" :class="borrowFeedback.type">{{ borrowFeedback.message }}</p>
      <button data-testid="borrow-submit" class="primary-button blue-button" type="submit" :disabled="busy">確認借出</button>
    </form>
    <form v-else data-testid="return-form" class="form-body" @submit.prevent="submitReturn">
      <label>ISBN <span>*</span><input data-testid="return-isbn" v-model="returnForm.isbn" required placeholder="請輸入 ISBN" /></label>
      <label>讀者 ID（可選）<input data-testid="return-reader-id" v-model="returnForm.readerId" placeholder="請輸入讀者 ID（可選）" /></label>
      <p v-if="returnFeedback" data-testid="return-result" class="feedback" :class="returnFeedback.type">{{ returnFeedback.message }}</p>
      <button data-testid="return-submit" class="primary-button green-button" type="submit" :disabled="busy">確認歸還</button>
    </form>
  </section>
</template>
