<script setup lang="ts">
import { categories } from '../libraryTypes'

type BookForm = {
  title: string
  isbn: string
  author?: string | null
  category: (typeof categories)[number]['value']
  quantity: number
  isActive: boolean
}

const props = defineProps<{
  form: BookForm
  isSubmitting: boolean
}>()

const emit = defineEmits<{
  submit: []
  'update:form': [value: Partial<BookForm>]
}>()

const updateText = (field: 'title' | 'isbn' | 'author', event: Event) => {
  emit('update:form', { [field]: (event.target as HTMLInputElement).value })
}

const updateCategory = (event: Event) => {
  emit('update:form', { category: (event.target as HTMLSelectElement).value as BookForm['category'] })
}

const updateQuantity = (event: Event) => {
  emit('update:form', { quantity: Number((event.target as HTMLInputElement).value) })
}

const updateActive = (event: Event) => {
  emit('update:form', { isActive: (event.target as HTMLInputElement).checked })
}
</script>

<template>
  <section class="panel add-panel">
    <div class="panel-heading">
      <div>
        <p class="eyebrow">Catalogue management</p>
        <h2>新增書籍</h2>
      </div>
      <span class="panel-icon" aria-hidden="true">＋</span>
    </div>
    <form class="book-form" data-testid="library-add-book-form" @submit.prevent="emit('submit')">
      <div class="form-field form-field-wide">
        <label for="book-title">書名</label>
        <input id="book-title" :value="props.form.title" data-testid="library-book-title-input" required placeholder="輸入書籍名稱" @input="updateText('title', $event)" />
      </div>
      <div class="form-field">
        <label for="book-isbn">ISBN</label>
        <input id="book-isbn" :value="props.form.isbn" data-testid="library-book-isbn-input" required placeholder="978-..." @input="updateText('isbn', $event)" />
      </div>
      <div class="form-field">
        <label for="book-author">作者 <span>(選填)</span></label>
        <input id="book-author" :value="props.form.author ?? ''" data-testid="library-book-author-input" placeholder="作者姓名" @input="updateText('author', $event)" />
      </div>
      <div class="form-field">
        <label for="book-category">分類</label>
        <select id="book-category" :value="props.form.category" @change="updateCategory">
          <option v-for="category in categories" :key="category.value" :value="category.value">{{ category.label }}</option>
        </select>
      </div>
      <div class="form-field">
        <label for="book-quantity">初始數量</label>
        <input id="book-quantity" :value="props.form.quantity" data-testid="library-book-quantity-input" min="1" required type="number" @input="updateQuantity" />
      </div>
      <label class="switch-field" for="book-active">
        <input id="book-active" :checked="props.form.isActive" type="checkbox" @change="updateActive" />
        <span class="switch" aria-hidden="true"></span>
        <span>立即上架</span>
      </label>
      <button class="primary-button wide" data-testid="library-add-book-submit" :disabled="isSubmitting" type="submit">
        {{ isSubmitting ? '建立中…' : '建立書籍' }}
      </button>
    </form>
  </section>
</template>
