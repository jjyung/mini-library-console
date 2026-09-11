<script setup lang="ts">
const props = defineProps<{
  activeTab: 'borrow' | 'return'
  isSubmitting: boolean
  borrowForm: { bookId: string; readerId: string; dueDate: string }
  returnForm: { bookId: string; loanId: string; readerId: string }
}>()

const emit = defineEmits<{
  'update:activeTab': [value: 'borrow' | 'return']
  'borrow': []
  'return': []
  'update:borrow-form': [value: Partial<{ bookId: string; readerId: string; dueDate: string }>]
  'update:return-form': [value: Partial<{ bookId: string; loanId: string; readerId: string }>]
}>()

const updateBorrow = (field: 'bookId' | 'readerId' | 'dueDate', event: Event) => {
  emit('update:borrow-form', { [field]: (event.target as HTMLInputElement).value })
}

const updateReturn = (field: 'bookId' | 'loanId' | 'readerId', event: Event) => {
  emit('update:return-form', { [field]: (event.target as HTMLInputElement).value })
}
</script>

<template>
  <section class="panel transaction-panel">
    <div class="panel-heading">
      <div>
        <p class="eyebrow">Transaction desk</p>
        <h2>借閱與歸還</h2>
      </div>
      <span class="panel-icon" aria-hidden="true">↔</span>
    </div>
    <div class="tabs" role="tablist" aria-label="交易類型">
      <button
        :aria-selected="activeTab === 'borrow'"
        :class="{ active: activeTab === 'borrow' }"
        data-testid="library-borrow-tab"
        role="tab"
        type="button"
        @click="emit('update:activeTab', 'borrow')"
      >
        借出書籍
      </button>
      <button
        :aria-selected="activeTab === 'return'"
        :class="{ active: activeTab === 'return' }"
        data-testid="library-return-tab"
        role="tab"
        type="button"
        @click="emit('update:activeTab', 'return')"
      >
        歸還書籍
      </button>
    </div>

    <form v-if="activeTab === 'borrow'" class="transaction-form" @submit.prevent="emit('borrow')">
      <label for="borrow-book-id">書籍 ID</label>
      <input id="borrow-book-id" :value="props.borrowForm.bookId" required placeholder="選擇館藏後自動帶入" @input="updateBorrow('bookId', $event)" />
      <label for="borrow-reader-id">借閱人識別值</label>
      <input
        id="borrow-reader-id"
        :value="props.borrowForm.readerId"
        data-testid="library-borrow-reader-input"
        required
        placeholder="例如：reader-001"
        @input="updateBorrow('readerId', $event)"
      />
      <label for="borrow-due-date">到期日 <span>(選填)</span></label>
      <input id="borrow-due-date" :value="props.borrowForm.dueDate" type="date" @input="updateBorrow('dueDate', $event)" />
      <button class="primary-button wide" data-testid="library-borrow-submit" :disabled="isSubmitting" type="submit">
        {{ isSubmitting ? '處理中…' : '確認借出' }}
      </button>
    </form>

    <form v-else class="transaction-form" @submit.prevent="emit('return')">
      <label for="return-book-id">書籍 ID</label>
      <input id="return-book-id" :value="props.returnForm.bookId" required placeholder="選擇館藏後自動帶入" @input="updateReturn('bookId', $event)" />
      <label for="return-loan-id">借閱紀錄 ID</label>
      <input id="return-loan-id" :value="props.returnForm.loanId" data-testid="library-return-loan-input" required placeholder="例如：loan UUID" @input="updateReturn('loanId', $event)" />
      <label for="return-reader-id">借閱人識別值 <span>(選填)</span></label>
      <input id="return-reader-id" :value="props.returnForm.readerId" data-testid="library-return-reader-input" placeholder="用於確認借閱人" @input="updateReturn('readerId', $event)" />
      <button class="secondary-button wide" data-testid="library-return-submit" :disabled="isSubmitting" type="submit">
        {{ isSubmitting ? '處理中…' : '確認歸還' }}
      </button>
    </form>
    <p class="helper-text">MVP 免登入模式 · 交易結果會即時同步到館藏清單</p>
  </section>
</template>
