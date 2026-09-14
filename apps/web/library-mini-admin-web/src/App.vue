<script setup lang="ts">
import { onMounted, reactive } from 'vue'

import AddBookForm from '@/features/library/components/AddBookForm.vue'
import BookTable from '@/features/library/components/BookTable.vue'
import TopBar from '@/features/library/components/TopBar.vue'
import TransactionCard from '@/features/library/components/TransactionCard.vue'
import { useLibraryConsole } from '@/features/library/useLibraryConsole'

const library = reactive(useLibraryConsole())

onMounted(() => {
  void library.loadBooks()
})
</script>

<template>
  <div class="app-shell">
    <TopBar v-model:query="library.query" @search="library.search" />

    <main class="page-content">
      <section class="hero-section">
        <div>
          <p class="eyebrow">Mini administration console</p>
          <h1>圖書館管理<br /><span>簡單、清晰、即時。</span></h1>
          <p class="hero-copy">管理館藏、追蹤借閱狀態，讓每一本書都能被好好找到。</p>
        </div>
        <div class="hero-stat" aria-label="館藏總數">
          <span class="hero-stat-number">{{ library.total }}</span>
          <span>館藏書目</span>
        </div>
      </section>

      <div v-if="library.feedback" :class="['feedback', `feedback-${library.feedback.kind}`]" role="alert">
        <span aria-hidden="true">{{ library.feedback.kind === 'success' ? '✓' : '!' }}</span>
        {{ library.feedback.message }}
        <button type="button" aria-label="關閉訊息" @click="library.clearFeedback">×</button>
      </div>

      <section class="workspace-grid">
        <div class="workspace-main">
          <BookTable
            :books="library.books"
            :has-next-page="library.hasNextPage"
            :has-previous-page="library.hasPreviousPage"
            :is-loading="library.isLoading"
            :page="library.page"
            :page-size="library.pageSize"
            :total="library.total"
            :total-pages="library.totalPages"
            @borrow="library.selectBorrow"
            @page="library.goToPage"
            @page-size="library.changePageSize"
            @return="library.selectReturn"
          />
        </div>
        <aside class="workspace-side">
          <TransactionCard
            v-model:active-tab="library.activeTab"
            :borrow-form="library.borrowForm"
            :is-submitting="library.isSubmitting"
            :return-form="library.returnForm"
            @borrow="library.borrowBook"
            @return="library.returnBook"
            @update:borrow-form="Object.assign(library.borrowForm, $event)"
            @update:return-form="Object.assign(library.returnForm, $event)"
          />
          <AddBookForm :form="library.createForm" :is-submitting="library.isSubmitting" @submit="library.createBook" @update:form="Object.assign(library.createForm, $event)" />
        </aside>
      </section>
    </main>

    <footer class="app-footer">
      <span>Library Console · MVP</span>
      <span>免登入測試環境</span>
    </footer>
  </div>
</template>

<style>
:root {
  color: #111827;
  background: #f9fafb;
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  font-synthesis: none;
  text-rendering: optimizeLegibility;

  --color-background: #f9fafb;
  --color-surface: #ffffff;
  --color-border: #e5e7eb;
  --color-border-strong: #d1d5db;
  --color-text: #111827;
  --color-text-muted: #6b7280;
  --color-text-subtle: #9ca3af;
  --color-primary: #2563eb;
  --color-primary-hover: #1d4ed8;
  --color-success: #16a34a;
  --color-success-hover: #15803d;
}
* { box-sizing: border-box; }
body { margin: 0; min-width: 320px; min-height: 100vh; background: var(--color-background); }
button, input, select { font: inherit; }
button { cursor: pointer; }
button:disabled { cursor: not-allowed; opacity: 0.5; }
.app-shell { min-height: 100vh; background: var(--color-background); }

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  min-height: 72px;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}
.brand { display: inline-flex; flex: 0 0 auto; align-items: center; gap: 0.5rem; color: var(--color-text); font-size: 1rem; font-weight: 600; letter-spacing: -0.02em; text-decoration: none; }
.brand-mark { display: grid; width: 40px; height: 40px; place-items: center; border: 0; border-radius: 0.5rem; color: #fff; background: linear-gradient(135deg, #2563eb, #1d4ed8); font-size: 1.15rem; box-shadow: none; }
.search-form { display: flex; flex: 1; align-items: center; max-width: 42rem; height: 40px; margin: 0 3rem; overflow: hidden; border: 1px solid var(--color-border); border-radius: 0.5rem; background: #f9fafb; }
.search-icon { padding-left: 0.75rem; color: var(--color-text-subtle); font-size: 1.2rem; transform: none; }
.search-form input { min-width: 0; flex: 1; min-height: 38px; border: 0; outline: 0; padding: 0 0.75rem; color: var(--color-text); background: transparent; }
.search-form input::placeholder { color: var(--color-text-subtle); }
.search-button { display: none; }
.topbar-actions { display: flex; flex: 0 0 auto; align-items: center; justify-content: flex-end; gap: 0.75rem; }
.environment-chip { display: inline-flex; align-items: center; gap: 0.45rem; color: var(--color-text-muted); font-size: 0.75rem; }
.dot { display: inline-block; width: 7px; height: 7px; border-radius: 50%; background: #22c55e; }
.avatar { display: grid; width: 40px; height: 40px; place-items: center; border: 0; border-radius: 50%; color: #fff; background: linear-gradient(135deg, #9333ea, #db2777); font-size: 0.8rem; font-weight: 700; }

.page-content { width: min(1280px, 100%); margin: 0 auto; padding: 2rem 1.5rem 3rem; }
.hero-section { display: none; }
h1, h2, p { margin-top: 0; }
.workspace-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); grid-template-rows: auto auto; gap: 1.5rem; align-items: start; margin-bottom: 2rem; }
.workspace-main { grid-column: 1 / -1; grid-row: 2; min-width: 0; }
.workspace-side { display: contents; }
.transaction-panel { grid-column: 1; grid-row: 1; }
.add-panel { grid-column: 2; grid-row: 1; }
.panel { overflow: hidden; border: 1px solid var(--color-border); border-radius: 0.5rem; color: var(--color-text); background: var(--color-surface); box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); }
.panel-heading { display: flex; align-items: flex-start; justify-content: space-between; padding: 1rem 1.5rem; }
.panel-heading h2 { margin: 0; color: var(--color-text); font-size: 1rem; font-weight: 600; letter-spacing: 0; }
.panel-heading .eyebrow { display: none; }
.panel-icon { display: grid; width: 20px; height: 20px; place-items: center; border: 0; border-radius: 0; color: #4f46e5; background: transparent; font-size: 1.2rem; }
.transaction-panel .panel-heading { display: none; }
.add-panel .panel-heading { align-items: center; justify-content: flex-start; gap: 0.5rem; border-bottom: 1px solid var(--color-border); }
.add-panel .panel-icon { order: -1; }
.catalogue-heading { position: relative; align-items: center; border-bottom: 1px solid var(--color-border); }
.catalogue-heading h2 { padding-right: 5rem; }
.count-badge { position: absolute; top: 50%; right: 1.5rem; display: inline; min-width: 0; height: auto; margin: 0; padding: 0; transform: translateY(-50%); color: var(--color-text-muted); background: transparent; font-size: 0.875rem; font-weight: 400; }
.count-badge::before { content: '共 '; }
.count-badge::after { content: ' 本'; }
.sync-label { display: none; }

.table-wrap { overflow-x: auto; }
table { width: 100%; min-width: 680px; border-collapse: collapse; }
th { padding: 0.75rem 1.5rem; color: var(--color-text-muted); background: #f9fafb; font-size: 0.75rem; font-weight: 500; letter-spacing: 0.04em; text-align: left; text-transform: uppercase; }
td { padding: 1rem 1.5rem; border-top: 1px solid var(--color-border); color: #374151; font-size: 0.875rem; }
tbody tr { transition: background-color 0.18s ease; }
tbody tr:hover { background: #f9fafb; }
.book-cell { display: flex; align-items: center; gap: 0; min-width: 230px; }
.book-cell strong { display: block; color: var(--color-text); font-size: 0.875rem; font-weight: 500; }
.book-cell small { display: block; margin-top: 0.25rem; color: var(--color-text-muted); font-size: 0.75rem; }
.book-cover { display: none; }
.muted { color: var(--color-text-muted); }
.status-pill { display: inline-block; padding: 0.125rem 0.625rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 500; }
.status-available { color: #166534; background: #dcfce7; }
.status-borrowed { color: #1e40af; background: #dbeafe; }
.status-inactive { color: #374151; background: #f3f4f6; }
.action-cell { white-space: nowrap; }
.text-button { display: inline-flex; align-items: center; border: 0; border-radius: 0.375rem; padding: 0.375rem 0.75rem; color: #1d4ed8; background: #eff6ff; font-size: 0.75rem; font-weight: 500; }
.text-button:hover:not(:disabled) { color: #1e40af; background: #dbeafe; }
.return-link { color: #15803d; background: #f0fdf4; }
.return-link:hover:not(:disabled) { color: #166534; background: #dcfce7; }
.disabled-action { color: #6b7280; background: #f3f4f6; }
.pagination { display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--color-border); padding: 0.75rem 1.5rem; color: var(--color-text-muted); font-size: 0.75rem; }
.pagination-actions { display: flex; align-items: center; gap: 0.45rem; }
.page-size-label { align-self: center; color: var(--color-text-muted); font-size: 0.75rem; }
.pagination select { width: auto; min-height: 30px; padding: 0.25rem 1.4rem 0.25rem 0.45rem; font-size: 0.75rem; }
.pagination button { border: 1px solid var(--color-border-strong); border-radius: 0.375rem; padding: 0.375rem 0.625rem; color: #374151; background: var(--color-surface); font-size: 0.75rem; }
.pagination button:hover:not(:disabled) { background: #f9fafb; }
.table-state { display: grid; min-height: 220px; place-items: center; color: var(--color-text-muted); }
.empty-state { align-content: center; gap: 0.25rem; padding: 2rem 1.5rem; text-align: center; }
.empty-state strong { color: var(--color-text); font-size: 0.875rem; font-weight: 500; }
.empty-icon { display: grid; width: 56px; height: 56px; margin-bottom: 0.5rem; place-items: center; border-radius: 50%; color: var(--color-text-subtle); background: #f3f4f6; font-size: 1.75rem; }

.tabs { display: flex; gap: 0; margin: 0; padding: 0; border-bottom: 1px solid var(--color-border); border-radius: 0; background: var(--color-surface); }
.tabs button { flex: 1; display: inline-flex; align-items: center; justify-content: center; border: 0; border-bottom: 2px solid transparent; border-radius: 0; padding: 0.75rem 1rem; color: var(--color-text-muted); background: transparent; font-size: 0.875rem; font-weight: 500; }
.tabs button:hover { color: #374151; background: #f9fafb; }
.tabs button.active { border-bottom-color: var(--color-primary); color: var(--color-primary); background: var(--color-surface); box-shadow: none; }
.transaction-form, .book-form { display: grid; gap: 0.25rem; padding: 1.5rem; }
.transaction-form label, .book-form label { color: #374151; font-size: 0.875rem; font-weight: 500; line-height: 1.5; }
.transaction-form label span, .book-form label span { color: var(--color-text-muted); font-weight: 400; }
.transaction-form input + label { margin-top: 0.75rem; }
input, select { width: 100%; min-height: 38px; border: 1px solid var(--color-border-strong); border-radius: 0.375rem; outline: none; padding: 0.5rem 0.75rem; color: var(--color-text); background: var(--color-surface); font-size: 0.875rem; }
input::placeholder { color: var(--color-text-subtle); }
input:focus, select:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15); }
.primary-button, .secondary-button { min-height: 38px; border: 1px solid transparent; border-radius: 0.375rem; color: #fff; background: var(--color-primary); font-size: 0.875rem; font-weight: 500; }
.primary-button:hover:not(:disabled) { background: var(--color-primary-hover); }
.secondary-button { background: var(--color-success); }
.secondary-button:hover:not(:disabled) { background: var(--color-success-hover); }
.wide { width: 100%; margin-top: 0.75rem; }
.helper-text { display: none; }
.book-form { grid-template-columns: 1fr; gap: 1rem; }
.form-field { display: grid; gap: 0.25rem; }
.form-field-wide, .switch-field, .book-form .wide { grid-column: auto; }
.switch-field { display: flex !important; align-items: center; justify-content: space-between; gap: 0.75rem; min-height: 58px; margin: 0; cursor: pointer; border: 1px solid var(--color-border); border-radius: 0.375rem; padding: 0.75rem 1rem; background: #f9fafb; }
.switch-field input { position: absolute; width: 1px; height: 1px; opacity: 0; }
.switch-field > span:last-child { order: -1; color: #374151; font-size: 0.875rem; font-weight: 500; }
.switch { width: 44px; height: 24px; flex: 0 0 auto; border-radius: 999px; background: #d1d5db; transition: background 0.18s ease; }
.switch-field input:checked + .switch { background: #4f46e5; }
.switch::after { display: block; width: 16px; height: 16px; margin: 4px; border-radius: 50%; background: #fff; content: ''; transition: transform 0.18s ease; }
.switch-field input:checked + .switch::after { transform: translateX(20px); }
.switch-field input:focus-visible + .switch { box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.2); }
.feedback { display: flex; align-items: center; gap: 0.65rem; margin-bottom: 1.5rem; border: 1px solid; border-radius: 0.375rem; padding: 0.75rem 1rem; font-size: 0.875rem; }
.feedback-success { border-color: #bbf7d0; color: #166534; background: #f0fdf4; }
.feedback-error { border-color: #fecaca; color: #991b1b; background: #fef2f2; }
.feedback button { margin-left: auto; border: 0; color: inherit; background: transparent; font-size: 1.2rem; }
.app-footer { display: none; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; }

@media (max-width: 1023px) {
  .workspace-grid { grid-template-columns: 1fr; grid-template-rows: auto auto auto; }
  .transaction-panel { grid-column: 1; grid-row: 1; }
  .add-panel { grid-column: 1; grid-row: 2; }
  .workspace-main { grid-column: 1; grid-row: 3; }
}

@media (max-width: 720px) {
  .topbar { flex-wrap: wrap; gap: 1rem; padding: 1rem; }
  .search-form { order: 3; flex-basis: 100%; max-width: none; margin: 0; }
  .page-content { padding: 1.5rem 1rem 2rem; }
  .pagination { align-items: flex-start; flex-direction: column; gap: 0.75rem; }
  .pagination-actions { width: 100%; flex-wrap: wrap; }
  .app-footer { width: 100%; padding-right: 1rem; padding-left: 1rem; }
}

@media (max-width: 420px) {
  .environment-chip { display: none; }
  .panel-heading, .transaction-form, .book-form { padding-right: 1rem; padding-left: 1rem; }
  th, td { padding-right: 1rem; padding-left: 1rem; }
}
</style>
