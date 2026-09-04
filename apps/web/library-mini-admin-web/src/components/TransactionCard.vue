<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'

import { validateBorrow, validateReturn } from '@/core/domain/libraryValidators'

import type { BorrowForm, Feedback, ReturnForm, ValidationErrors } from '@/core/domain/libraryTypes'

type Props = {
  borrowStatus?: Feedback | null
  returnStatus?: Feedback | null
  busy?: boolean
  borrowIsbnPrefill?: string
  returnIsbnPrefill?: string
}

const props = withDefaults(defineProps<Props>(), {
  borrowStatus: null,
  returnStatus: null,
  busy: false,
  borrowIsbnPrefill: '',
  returnIsbnPrefill: '',
})

const emit = defineEmits<{
  borrow: [form: BorrowForm]
  return: [form: ReturnForm]
}>()

const activeTab = ref<'borrow' | 'return'>('borrow')
const borrowForm = reactive<BorrowForm>({ readerId: '', isbn: '', dueDate: '' })
const returnForm = reactive<ReturnForm>({ isbn: '', readerId: '' })
const borrowErrors = ref<ValidationErrors>({})
const returnErrors = ref<ValidationErrors>({})

const borrowFeedback = computed<Feedback | null>(() => {
  if (Object.keys(borrowErrors.value).length > 0) {
    return { type: 'error', message: '請填寫讀者 ID 與 ISBN。', code: 'A0000' }
  }
  return props.borrowStatus
})

const returnFeedback = computed<Feedback | null>(() => {
  if (Object.keys(returnErrors.value).length > 0) {
    return { type: 'error', message: '請填寫 ISBN。', code: 'A0000' }
  }
  return props.returnStatus
})

watch(
  () => props.borrowIsbnPrefill,
  (isbn) => {
    if (!isbn) return
    activeTab.value = 'borrow'
    borrowForm.isbn = isbn
    borrowErrors.value = {}
  },
)

watch(
  () => props.returnIsbnPrefill,
  (isbn) => {
    if (!isbn) return
    activeTab.value = 'return'
    returnForm.isbn = isbn
    returnErrors.value = {}
  },
)

function clearBorrowErrors() {
  borrowErrors.value = {}
}

function clearReturnErrors() {
  returnErrors.value = {}
}

function submitBorrow() {
  const errors = validateBorrow(borrowForm)
  borrowErrors.value = errors
  if (Object.keys(errors).length > 0) return
  emit('borrow', { ...borrowForm })
}

function submitReturn() {
  const errors = validateReturn(returnForm)
  returnErrors.value = errors
  if (Object.keys(errors).length > 0) return
  emit('return', { ...returnForm })
}
</script>

<template>
  <section class="card transaction-card" data-testid="transaction-card">
    <div class="tab-list" role="tablist" aria-label="借還操作">
      <button
        type="button"
        class="tab-button"
        :class="{ 'tab-button--borrow-active': activeTab === 'borrow' }"
        role="tab"
        :aria-selected="activeTab === 'borrow'"
        data-testid="borrow-tab"
        @click="activeTab = 'borrow'"
      >
        <span class="tab-icon tab-icon--blue" aria-hidden="true">↗</span>
        借書
      </button>
      <button
        type="button"
        class="tab-button"
        :class="{ 'tab-button--return-active': activeTab === 'return' }"
        role="tab"
        :aria-selected="activeTab === 'return'"
        data-testid="return-tab"
        @click="activeTab = 'return'"
      >
        <span class="tab-icon tab-icon--green" aria-hidden="true">↙</span>
        還書
      </button>
    </div>

    <div class="card-body">
      <form
        v-if="activeTab === 'borrow'"
        data-testid="borrow-form"
        class="form-stack"
        novalidate
        @submit.prevent="submitBorrow"
      >
        <div class="field-group">
          <label for="borrow-reader-id">讀者 ID <span class="required-mark">*</span></label>
          <input
            id="borrow-reader-id"
            v-model="borrowForm.readerId"
            data-testid="borrow-reader-id"
            type="text"
            placeholder="請輸入讀者 ID"
            :aria-invalid="Boolean(borrowErrors.readerId)"
            @input="clearBorrowErrors"
          />
          <p v-if="borrowErrors.readerId" class="field-error">{{ borrowErrors.readerId }}</p>
        </div>

        <div class="field-group">
          <label for="borrow-isbn">ISBN <span class="required-mark">*</span></label>
          <input
            id="borrow-isbn"
            v-model="borrowForm.isbn"
            data-testid="borrow-isbn"
            type="text"
            placeholder="請輸入 ISBN"
            :aria-invalid="Boolean(borrowErrors.isbn)"
            @input="clearBorrowErrors"
          />
          <p v-if="borrowErrors.isbn" class="field-error">{{ borrowErrors.isbn }}</p>
        </div>

        <div class="field-group">
          <label for="borrow-due-date" class="label-with-icon">
            <span aria-hidden="true">◷</span>
            到期日（可選）
          </label>
          <input id="borrow-due-date" v-model="borrowForm.dueDate" data-testid="borrow-due-date" type="date" />
          <p class="field-hint">到期日僅保存於借閱紀錄，不計算罰款。</p>
        </div>

        <div
          v-if="borrowFeedback"
          data-testid="borrow-status"
          class="inline-feedback"
          :class="`inline-feedback--${borrowFeedback.type}`"
          role="status"
          aria-live="polite"
        >
          <span class="feedback-icon" aria-hidden="true">{{ borrowFeedback.type === 'success' ? '✓' : '!' }}</span>
          <div>
            <p>{{ borrowFeedback.message }}</p>
            <small v-if="borrowFeedback.code">錯誤碼 {{ borrowFeedback.code }}<template v-if="borrowFeedback.traceId"> · {{ borrowFeedback.traceId }}</template></small>
          </div>
        </div>

        <button class="primary-button primary-button--blue" type="submit" data-testid="borrow-submit" :disabled="busy">
          {{ busy ? '處理中…' : '確認借出' }}
        </button>
      </form>

      <form
        v-else
        data-testid="return-form"
        class="form-stack"
        novalidate
        @submit.prevent="submitReturn"
      >
        <div class="field-group">
          <label for="return-isbn">ISBN <span class="required-mark">*</span></label>
          <input
            id="return-isbn"
            v-model="returnForm.isbn"
            data-testid="return-isbn"
            type="text"
            placeholder="請輸入 ISBN"
            :aria-invalid="Boolean(returnErrors.isbn)"
            @input="clearReturnErrors"
          />
          <p v-if="returnErrors.isbn" class="field-error">{{ returnErrors.isbn }}</p>
        </div>

        <div class="field-group">
          <label for="return-reader-id">讀者 ID（可選）</label>
          <input
            id="return-reader-id"
            v-model="returnForm.readerId"
            data-testid="return-reader-id"
            type="text"
            placeholder="請輸入讀者 ID（可選）"
            :aria-invalid="Boolean(returnErrors.readerId)"
            @input="clearReturnErrors"
          />
          <p v-if="returnErrors.readerId" class="field-error">{{ returnErrors.readerId }}</p>
          <p class="field-hint">同一本書有多筆借閱時，請提供讀者 ID。</p>
        </div>

        <div
          v-if="returnFeedback"
          data-testid="return-status"
          class="inline-feedback"
          :class="`inline-feedback--${returnFeedback.type}`"
          role="status"
          aria-live="polite"
        >
          <span class="feedback-icon" aria-hidden="true">{{ returnFeedback.type === 'success' ? '✓' : '!' }}</span>
          <div>
            <p>{{ returnFeedback.message }}</p>
            <small v-if="returnFeedback.code">錯誤碼 {{ returnFeedback.code }}<template v-if="returnFeedback.traceId"> · {{ returnFeedback.traceId }}</template></small>
          </div>
        </div>

        <button class="primary-button primary-button--green" type="submit" data-testid="return-submit" :disabled="busy">
          {{ busy ? '處理中…' : '確認歸還' }}
        </button>
      </form>
    </div>
  </section>
</template>

