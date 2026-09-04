<script setup lang="ts">
import { reactive, ref, watch } from 'vue'

import { validateCreateBook } from '@/core/domain/libraryValidators'

import type { CreateBookForm, Feedback, ValidationErrors } from '@/core/domain/libraryTypes'

type Props = {
  status?: Feedback | null
  busy?: boolean
  resetKey?: number
}

const props = withDefaults(defineProps<Props>(), {
  status: null,
  busy: false,
  resetKey: 0,
})

const emit = defineEmits<{
  submit: [form: CreateBookForm]
}>()

const initialForm = (): CreateBookForm => ({
  title: '',
  isbn: '',
  author: '',
  category: 'literature',
  quantity: 1,
  isActive: true,
})

const form = reactive<CreateBookForm>(initialForm())
const errors = ref<ValidationErrors>({})

watch(
  () => props.resetKey,
  () => {
    Object.assign(form, initialForm())
    errors.value = {}
  },
)

function clearErrors() {
  errors.value = {}
}

function submitForm() {
  const nextErrors = validateCreateBook(form)
  errors.value = nextErrors
  if (Object.keys(nextErrors).length > 0) return
  emit('submit', { ...form })
}
</script>

<template>
  <section class="card add-book-card">
    <div class="card-heading">
      <div class="section-icon section-icon--indigo" aria-hidden="true">＋</div>
      <h2>新增書籍</h2>
    </div>

    <div class="card-body">
      <form data-testid="add-book-form" class="form-stack" novalidate @submit.prevent="submitForm">
        <div class="field-group">
          <label for="add-book-title">書名 <span class="required-mark">*</span></label>
          <input
            id="add-book-title"
            v-model="form.title"
            data-testid="add-book-title"
            type="text"
            placeholder="請輸入書名"
            :aria-invalid="Boolean(errors.title)"
            @input="clearErrors"
          />
          <p v-if="errors.title" class="field-error">{{ errors.title }}</p>
        </div>

        <div class="field-group">
          <label for="add-book-isbn">ISBN <span class="required-mark">*</span></label>
          <input
            id="add-book-isbn"
            v-model="form.isbn"
            data-testid="add-book-isbn"
            type="text"
            placeholder="請輸入 ISBN"
            :aria-invalid="Boolean(errors.isbn)"
            @input="clearErrors"
          />
          <p v-if="errors.isbn" class="field-error">{{ errors.isbn }}</p>
        </div>

        <div class="field-group">
          <label for="add-book-author">作者（可選）</label>
          <input
            id="add-book-author"
            v-model="form.author"
            data-testid="add-book-author"
            type="text"
            placeholder="請輸入作者"
            :aria-invalid="Boolean(errors.author)"
            @input="clearErrors"
          />
          <p v-if="errors.author" class="field-error">{{ errors.author }}</p>
        </div>

        <div class="field-group">
          <label for="add-book-category">分類 <span class="required-mark">*</span></label>
          <select
            id="add-book-category"
            v-model="form.category"
            data-testid="add-book-category"
            :aria-invalid="Boolean(errors.category)"
            @change="clearErrors"
          >
            <option value="literature">文學</option>
            <option value="science">科學</option>
            <option value="technology">科技</option>
            <option value="history">歷史</option>
            <option value="art">藝術</option>
            <option value="philosophy">哲學</option>
            <option value="business">商業</option>
            <option value="education">教育</option>
          </select>
          <p v-if="errors.category" class="field-error">{{ errors.category }}</p>
        </div>

        <div class="field-group">
          <label for="add-book-quantity">數量 <span class="required-mark">*</span></label>
          <input
            id="add-book-quantity"
            v-model.number="form.quantity"
            data-testid="add-book-quantity"
            type="number"
            min="1"
            step="1"
            :aria-invalid="Boolean(errors.quantity)"
            @input="clearErrors"
          />
          <p v-if="errors.quantity" class="field-error">{{ errors.quantity }}</p>
        </div>

        <div class="active-toggle">
          <div>
            <label for="add-book-active">上架狀態</label>
            <p>書籍是否可供借閱</p>
          </div>
          <button
            id="add-book-active"
            type="button"
            role="switch"
            data-testid="add-book-active"
            :aria-checked="form.isActive"
            class="switch"
            :class="{ 'switch--on': form.isActive }"
            @click="form.isActive = !form.isActive"
          >
            <span />
          </button>
        </div>

        <div
          v-if="errors.title || errors.isbn || errors.category || errors.quantity || errors.author || status"
          data-testid="add-book-status"
          class="inline-feedback"
          :class="`inline-feedback--${status?.type ?? 'error'}`"
          role="status"
          aria-live="polite"
        >
          <span class="feedback-icon" aria-hidden="true">{{ status?.type === 'success' ? '✓' : '!' }}</span>
          <div>
            <p v-if="status">{{ status.message }}</p>
            <p v-else>請修正表單中的欄位。</p>
            <small v-if="status?.code">錯誤碼 {{ status.code }}<template v-if="status.traceId"> · {{ status.traceId }}</template></small>
          </div>
        </div>

        <button class="primary-button primary-button--indigo" type="submit" data-testid="add-book-submit" :disabled="busy">
          {{ busy ? '處理中…' : '新增書籍' }}
        </button>
      </form>
    </div>
  </section>
</template>

