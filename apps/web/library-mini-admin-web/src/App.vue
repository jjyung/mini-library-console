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
  color: #dce9fb;
  background: #081426;
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  font-synthesis: none;
  text-rendering: optimizeLegibility;
}
* { box-sizing: border-box; }
body { margin: 0; min-width: 320px; min-height: 100vh; background: #081426; }
button, input, select { font: inherit; }
button { cursor: pointer; }
button:disabled { cursor: not-allowed; opacity: 0.5; }
.app-shell { min-height: 100vh; background: radial-gradient(circle at 11% 0%, #102c4e 0, #081426 36rem), #081426; }
.topbar { display: grid; grid-template-columns: 220px minmax(250px, 560px) 1fr; align-items: center; gap: 2rem; height: 76px; padding: 0 clamp(1.25rem, 4vw, 5rem); border-bottom: 1px solid rgba(119, 157, 203, 0.16); background: rgba(7, 20, 38, 0.8); }
.brand { display: inline-flex; align-items: center; gap: 0.7rem; color: #f3f7ff; font-size: 1rem; font-weight: 760; letter-spacing: -0.02em; text-decoration: none; }
.brand-mark { display: grid; width: 31px; height: 31px; place-items: center; border: 1px solid #58a9ff; border-radius: 8px; color: #65b4ff; font-size: 1.2rem; box-shadow: 0 0 18px rgba(45, 147, 255, 0.25); }
.search-form { display: flex; align-items: center; height: 40px; overflow: hidden; border: 1px solid #2c496b; border-radius: 9px; background: #0d2038; }
.search-icon { padding-left: 0.85rem; color: #83a0c4; font-size: 1.35rem; transform: rotate(-20deg); }
.search-form input { min-width: 0; flex: 1; border: 0; outline: 0; padding: 0 0.75rem; color: #e8f2ff; background: transparent; }
.search-form input::placeholder { color: #6d87a8; }
.search-button { align-self: stretch; border: 0; border-left: 1px solid #2c496b; padding: 0 1rem; color: #c8e5ff; background: #15365b; font-weight: 650; }
.topbar-actions { display: flex; align-items: center; justify-content: flex-end; gap: 1rem; }
.environment-chip { display: inline-flex; align-items: center; gap: 0.45rem; color: #95b4d8; font-size: 0.8rem; }
.dot { display: inline-block; width: 7px; height: 7px; border-radius: 50%; background: #55d4a2; box-shadow: 0 0 9px #55d4a2; }
.avatar { display: grid; width: 34px; height: 34px; place-items: center; border: 1px solid #325378; border-radius: 50%; color: #c9e3ff; background: #163456; font-size: 0.8rem; font-weight: 700; }
.page-content { width: min(1480px, calc(100% - 2.5rem)); margin: 0 auto; padding: 3.5rem 0 4rem; }
.hero-section { display: flex; align-items: flex-end; justify-content: space-between; padding: 0.5rem 0 2.3rem; }
.eyebrow { margin: 0 0 0.65rem; color: #58a9ff; font-size: 0.69rem; font-weight: 800; letter-spacing: 0.15em; text-transform: uppercase; }
h1, h2, p { margin-top: 0; }
h1 { margin-bottom: 1rem; color: #f1f6ff; font-size: clamp(2.35rem, 5vw, 4.4rem); line-height: 0.99; letter-spacing: -0.065em; }
h1 span { color: #70b9ff; }
.hero-copy { max-width: 470px; margin-bottom: 0; color: #8da6c4; font-size: 1rem; line-height: 1.7; }
.hero-stat { display: flex; flex-direction: column; align-items: flex-end; gap: 0.25rem; color: #7f9bbd; font-size: 0.8rem; }
.hero-stat-number { color: #eaf3ff; font-size: 3rem; font-weight: 760; line-height: 1; }
.workspace-grid { display: grid; grid-template-columns: minmax(0, 1fr) 380px; gap: 1.2rem; align-items: start; }
.workspace-side { display: grid; gap: 1.2rem; }
.panel { border: 1px solid rgba(100, 142, 190, 0.26); border-radius: 12px; background: linear-gradient(145deg, rgba(16, 38, 65, 0.9), rgba(10, 27, 48, 0.96)); box-shadow: 0 18px 45px rgba(0, 0, 0, 0.14); }
.panel-heading { display: flex; align-items: flex-start; justify-content: space-between; padding: 1.35rem 1.45rem 1.1rem; }
.panel-heading h2 { margin: 0; color: #e9f3ff; font-size: 1.02rem; letter-spacing: -0.02em; }
.panel-icon { display: grid; width: 31px; height: 31px; place-items: center; border: 1px solid #31547c; border-radius: 8px; color: #65b4ff; background: #133253; font-size: 1.1rem; }
.catalogue-heading { align-items: center; }
.count-badge { display: inline-grid; min-width: 27px; height: 22px; margin-left: 0.35rem; place-items: center; border-radius: 5px; color: #77beff; background: #153c65; font-size: 0.72rem; vertical-align: 2px; }
.sync-label { display: inline-flex; align-items: center; gap: 0.42rem; color: #79cbaa; font-size: 0.74rem; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; min-width: 680px; }
th { padding: 0.75rem 1.45rem; color: #7592b5; font-size: 0.67rem; font-weight: 700; letter-spacing: 0.08em; text-align: left; text-transform: uppercase; }
td { padding: 1rem 1.45rem; border-top: 1px solid rgba(100, 142, 190, 0.14); color: #b8cce4; font-size: 0.83rem; }
tbody tr { transition: background 0.18s ease; }
tbody tr:hover { background: rgba(47, 107, 166, 0.11); }
.book-cell { display: flex; align-items: center; gap: 0.7rem; min-width: 250px; }
.book-cell strong { display: block; color: #e6f0fd; font-size: 0.86rem; }
.book-cell small { display: block; margin-top: 0.25rem; color: #708bab; font-size: 0.7rem; }
.book-cover { display: grid; width: 34px; height: 43px; flex: 0 0 auto; place-items: center; border: 1px solid #3b6e9e; border-radius: 4px; color: #65b4ff; background: linear-gradient(145deg, #1c4b78, #102b4c); font-size: 1.1rem; }
.muted { color: #708bab; }
.status-pill { display: inline-block; padding: 0.34rem 0.58rem; border-radius: 5px; font-size: 0.7rem; font-weight: 700; }
.status-available { color: #79d2ac; background: rgba(55, 176, 132, 0.13); }
.status-borrowed { color: #f1c47c; background: rgba(221, 155, 60, 0.14); }
.status-inactive { color: #9eafc4; background: rgba(125, 145, 170, 0.16); }
.action-cell { white-space: nowrap; }
.text-button { border: 0; padding: 0.25rem 0.35rem; color: #63b3ff; background: transparent; font-size: 0.78rem; font-weight: 700; }
.text-button:hover { color: #b7e0ff; text-decoration: underline; }
.return-link { color: #7bd2af; }
.disabled-action { color: #607b9c; }
.pagination { display: flex; align-items: center; justify-content: space-between; padding: 0.9rem 1.45rem 1.1rem; color: #7894b5; font-size: 0.75rem; }
.pagination-actions { display: flex; gap: 0.45rem; }
.page-size-label { align-self: center; color: #7894b5; font-size: 0.72rem; }
.pagination select { min-height: 30px; width: auto; padding: 0.25rem 1.4rem 0.25rem 0.45rem; font-size: 0.72rem; }
.pagination button { border: 1px solid #2d4c6e; border-radius: 6px; padding: 0.4rem 0.65rem; color: #abc7e7; background: #112c4b; font-size: 0.72rem; }
.table-state { min-height: 275px; display: grid; place-items: center; color: #87a2c1; }
.empty-state { align-content: center; gap: 0.45rem; padding: 2rem; text-align: center; }
.empty-state strong { color: #d6e5f7; }
.empty-icon { color: #579fe0; font-size: 2.1rem; }
.tabs { display: grid; grid-template-columns: 1fr 1fr; gap: 0.3rem; margin: 0 1.45rem 1.2rem; padding: 0.25rem; border-radius: 7px; background: #0b1d34; }
.tabs button { border: 0; border-radius: 5px; padding: 0.6rem; color: #7493b7; background: transparent; font-size: 0.78rem; font-weight: 700; }
.tabs button.active { color: #d9edff; background: #1d5d97; box-shadow: 0 4px 14px rgba(31, 120, 204, 0.25); }
.transaction-form, .book-form { display: grid; gap: 0.55rem; padding: 0 1.45rem 1.25rem; }
.transaction-form label, .book-form label { color: #8faac9; font-size: 0.72rem; }
.transaction-form label span, .book-form label span { color: #607c9d; }
input, select { width: 100%; min-height: 38px; border: 1px solid #2b4b6d; border-radius: 6px; outline: none; padding: 0.55rem 0.7rem; color: #dcecff; background: #0c213b; font-size: 0.78rem; }
input:focus, select:focus { border-color: #56aaf5; box-shadow: 0 0 0 3px rgba(74, 164, 245, 0.13); }
.primary-button, .secondary-button { min-height: 40px; border: 1px solid #55a9f4; border-radius: 6px; color: #eef8ff; background: #246ca8; font-size: 0.79rem; font-weight: 760; }
.secondary-button { border-color: #3d9277; background: #247c65; }
.wide { width: 100%; margin-top: 0.45rem; }
.helper-text { margin: 0; padding: 0 1.45rem 1.3rem; color: #607d9f; font-size: 0.68rem; line-height: 1.5; }
.add-panel .panel-heading { padding-bottom: 0.85rem; }
.book-form { grid-template-columns: 1fr 1fr; }
.form-field { display: grid; gap: 0.45rem; }
.form-field-wide, .switch-field, .book-form .wide { grid-column: 1 / -1; }
.switch-field { display: flex !important; align-items: center; gap: 0.55rem; margin-top: 0.35rem; cursor: pointer; }
.switch-field input { position: absolute; width: 1px; height: 1px; opacity: 0; }
.switch { width: 28px; height: 16px; border-radius: 999px; background: #2c4766; transition: background 0.18s ease; }
.switch-field input:checked + .switch { background: #2c9b7a; }
.switch::after { display: block; width: 12px; height: 12px; margin: 2px; border-radius: 50%; background: white; content: ''; transition: transform 0.18s ease; }
.switch-field input:checked + .switch::after { transform: translateX(12px); }
.feedback { display: flex; align-items: center; gap: 0.65rem; margin-bottom: 1rem; border: 1px solid; border-radius: 8px; padding: 0.75rem 1rem; font-size: 0.82rem; }
.feedback-success { border-color: rgba(83, 202, 157, 0.4); color: #9be7c5; background: rgba(51, 154, 116, 0.12); }
.feedback-error { border-color: rgba(245, 126, 126, 0.4); color: #ffc2c2; background: rgba(180, 55, 55, 0.12); }
.feedback button { margin-left: auto; border: 0; color: inherit; background: transparent; font-size: 1.2rem; }
.app-footer { display: flex; justify-content: space-between; width: min(1480px, calc(100% - 2.5rem)); margin: 0 auto; border-top: 1px solid rgba(119, 157, 203, 0.13); padding: 1.25rem 0 2rem; color: #5d789a; font-size: 0.68rem; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; }
@media (max-width: 980px) {
  .topbar { grid-template-columns: 1fr auto; gap: 1rem; }
  .search-form { grid-column: 1 / -1; grid-row: 2; margin-bottom: 1rem; }
  .topbar { height: auto; padding-top: 1rem; padding-bottom: 1rem; }
  .workspace-grid { grid-template-columns: 1fr; }
  .workspace-side { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 620px) {
  .page-content { width: min(100% - 1.25rem, 1480px); padding-top: 2.3rem; }
  .hero-section { align-items: flex-start; flex-direction: column; gap: 1.5rem; }
  .hero-stat { align-items: flex-start; }
  .workspace-side { grid-template-columns: 1fr; }
  .book-form { grid-template-columns: 1fr; }
  .form-field-wide, .switch-field, .book-form .wide { grid-column: auto; }
  .app-footer { width: calc(100% - 1.25rem); }
}
</style>
