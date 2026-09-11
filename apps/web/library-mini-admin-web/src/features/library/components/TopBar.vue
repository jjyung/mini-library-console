<script setup lang="ts">
defineProps<{ query: string }>()

const emit = defineEmits<{
  'update:query': [value: string]
  search: []
}>()
</script>

<template>
  <header class="topbar">
    <a class="brand" href="#" aria-label="Library Console 首頁">
      <span class="brand-mark" aria-hidden="true">▥</span>
      <span>Library Console</span>
    </a>
    <form class="search-form" role="search" @submit.prevent="emit('search')">
      <label class="sr-only" for="library-search">搜尋書名、ISBN 或作者</label>
      <span class="search-icon" aria-hidden="true">⌕</span>
      <input
        id="library-search"
        :value="query"
        data-testid="library-topbar-search"
        placeholder="搜尋書名、ISBN 或作者..."
        type="search"
        @input="emit('update:query', ($event.target as HTMLInputElement).value)"
      />
      <button class="search-button" type="submit">搜尋</button>
    </form>
    <div class="topbar-actions">
      <span class="environment-chip"><span class="dot"></span> MVP 預覽</span>
      <span class="avatar" aria-label="管理員">管</span>
    </div>
  </header>
</template>
