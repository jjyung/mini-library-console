<template>
  <header class="topbar" data-testid="topbar">
    <div class="topbar__brand">
      <div class="brand-mark" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20" />
          <path d="M8 6h8M8 10h8" />
        </svg>
      </div>
      <div>
        <p class="brand-title">Library Mini Admin</p>
        <p class="brand-subtitle">管理控制台</p>
      </div>
    </div>

    <div class="topbar__search">
      <label class="sr-only" for="catalogue-search">搜尋館藏</label>
      <span class="search-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-4-4" />
        </svg>
      </span>
      <input
        id="catalogue-search"
        data-testid="topbar-search"
        type="search"
        placeholder="搜尋書名、ISBN、作者..."
        aria-label="搜尋書名、ISBN、作者"
      />
    </div>

    <div class="topbar__admin" data-testid="admin-identity">
      <div class="admin-copy">
        <p>Admin</p>
        <span>管理員</span>
      </div>
      <div class="admin-avatar" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <circle cx="12" cy="8" r="3.5" />
          <path d="M5 20a7 7 0 0 1 14 0" />
        </svg>
      </div>
    </div>
  </header>
</template>

