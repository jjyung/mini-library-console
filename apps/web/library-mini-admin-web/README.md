# library-mini-admin-web

Library mini admin console 前端應用（Vue 3 + Vite + TypeScript），搭配 `apps/api` 的 Spring Boot 後端。完整的 subagent workflow、角色規範與指令請見根目錄 [README.md](../../../README.md) 與 [AGENTS.md](../../../AGENTS.md)。

## Prerequisites

- Node.js `^20.19.0 || >=22.12.0`（見 `package.json` 的 `engines`）

## Commands

於本目錄（`apps/web/library-mini-admin-web`）：

```sh
npm install        # 安裝前端依賴；repo 根目錄可直接用 npm run setup
npm run dev        # 啟動 Vite dev server（http://localhost:5173）
npm run build      # type-check + production build
npm run preview    # 預覽 production build（http://localhost:4173）
npm run type-check # vue-tsc 型別檢查
npm run lint       # oxlint + eslint（皆帶 --fix）
npm run format     # Prettier 格式化 src/
```

於 repo 根目錄：

```sh
npm run dev        # 同時啟動 API 與 web
npm run dev:web    # 只啟動 web
npm run check:web  # frontend lint + type-check
npm run e2e        # Playwright smoke E2E
```

## E2E Tests (Playwright)

- 測試目錄為 `e2e/`（`playwright.config.ts` 的 `testDir`），檔名 `*.spec.ts`
- dev 模式 baseURL 為 `http://localhost:5173`；CI 使用 preview server `http://localhost:4173`，需先執行 `npm run build`
- 首次執行需安裝瀏覽器：`npx playwright install`

```sh
# 執行所有 E2E
npm run test:e2e
# 只跑 Chromium
npm run test:e2e -- --project=chromium
# 只跑特定檔案
npm run test:e2e -- e2e/vue.spec.ts
# debug 模式
npm run test:e2e -- --debug
```

## UI Test Locator Rule

UI 變更 MUST 保留 `data-testid` 的穩定性；E2E 也應優先使用 `getByTestId()`（見根目錄 `AGENTS.md`）。

## IDE Setup

VS Code + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar)（請停用 Vetur）。`.vue` 匯入的型別由 `vue-tsc` 負責，編輯器端則由 Volar 提供 TypeScript language service 支援。
