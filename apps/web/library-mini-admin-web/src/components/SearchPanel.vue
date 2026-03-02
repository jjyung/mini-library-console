<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  search: [keyword: string]
  clear: []
}>()

withDefaults(
  defineProps<{
    compact?: boolean
  }>(),
  {
    compact: false,
  },
)

const keyword = ref('')

function onSearch() {
  emit('search', keyword.value)
}

function onClear() {
  keyword.value = ''
  emit('clear')
}
</script>

<template>
  <section class="search-panel" :class="{ compact }" data-testid="topbar-search">
    <h2 v-if="!compact">搜尋</h2>
    <input v-model="keyword" data-testid="book-search-keyword" placeholder="title / isbn / author" />
    <button type="button" data-testid="book-search-submit" @click="onSearch">搜尋</button>
    <button type="button" data-testid="book-search-clear" @click="onClear">清除</button>
  </section>
</template>

<style scoped>
.search-panel {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.search-panel h2 {
  margin: 0;
  font-size: 18px;
  width: 100%;
}

.search-panel input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  min-width: 280px;
  flex: 1;
  outline: none;
}

.search-panel input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px #dbeafe;
}

.search-panel button {
  border: 0;
  border-radius: 8px;
  padding: 9px 12px;
  background: #e5e7eb;
  cursor: pointer;
}

.search-panel button[data-testid='book-search-submit'] {
  background: #111827;
  color: #ffffff;
}

.search-panel.compact input {
  min-width: 180px;
}
</style>
