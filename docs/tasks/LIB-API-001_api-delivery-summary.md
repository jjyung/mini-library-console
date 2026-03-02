# LIB-API-001 API Delivery Summary

## 1. 任務目標
- 依 `docs/openapi.yaml`、`docs/schema/*.md`、`docs/api/*.md` 完成 API 交付。
- 採 BE/FE 分工執行，PG 負責分派、協調與整合驗證。

## 2. 分派與執行概況
- PG：完成任務拆分、依賴排序與交接 Gate 管理。
- BE：完成 SQLite schema 初始化、5 支 API 後端實作、錯誤碼映射、整合測試。
- FE：完成最小可用 UI、集中式 API client、business code 映射與 `data-testid` 覆蓋。

## 3. 主要完成項目

### 3.1 Backend（BE）
- 建立 SQLite 初始化腳本與設定：
  - `apps/api/library-mini-admin-api/src/main/resources/schema.sql`
  - `apps/api/library-mini-admin-api/src/main/resources/application.properties`
- 建立 API/Service/Repository/Exception 結構（books + loans）：
  - `apps/api/library-mini-admin-api/src/main/java/com/example/library/api/**`
  - `apps/api/library-mini-admin-api/src/main/java/com/example/library/service/**`
  - `apps/api/library-mini-admin-api/src/main/java/com/example/library/repository/**`
  - `apps/api/library-mini-admin-api/src/main/java/com/example/library/exception/**`
- 建立 RequestDTO/ResponseDTO（對齊 OpenAPI 命名）。
- 建立 business code 回應治理：`00000/A0001~A0007/B0000/C0000`。
- 移除示範用 `HelloController`。

### 3.2 Frontend（FE）
- 建立集中式 API client（無元件直連 fetch/axios）：
  - `apps/web/library-mini-admin-web/src/api/client.ts`
  - `apps/web/library-mini-admin-web/src/api/libraryApi.ts`
- 建立 store（state/action/getter）與錯誤碼映射：
  - `apps/web/library-mini-admin-web/src/store/libraryStore.ts`
  - `apps/web/library-mini-admin-web/src/mappers/errorCodeMapper.ts`
- 建立最小 UI 元件（新增、借書、還書、搜尋、列表、訊息）。
- 全面補齊 `data-testid`（create/list/search/borrow/return/result）。
- 將 API base URL 改為環境變數（避免 hard-coded URL）：
  - `VITE_API_BASE_URL`。

### 3.3 PG 協調與文件
- 更新任務分派與狀態：
  - `docs/tasks/LIB-API-001_api-delivery-plan.md`
- 建立 BE/FE Assignment Matrix 與 Handoff Gates。

## 4. 驗證結果
- Backend：`./mvnw -Dmaven.repo.local=/tmp/m2 test` 綠燈。
  - Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
- Frontend：
  - `npm run type-check` 通過
  - `npm run lint` 通過
  - `npm run test:e2e` 通過（3 passed）

## 5. 契約與規範對齊
- OpenAPI 路徑與 API ID：對齊 `docs/openapi.yaml`。
- DTO 命名：採 `*RequestDTO/*ResponseDTO`。
- 錯誤處理：以 business code 為主，不依賴 HTTP status 做業務判斷。
- 前端定位器：互動元素皆提供 `data-testid`，符合 QA/E2E 要求。

## 6. 本次調整重點（品質收斂）
- 修正後端全域例外處理，避免直接攔截 `Exception`。
- 補齊整合測試邊界（包含 `A0007` 逾期歸還與 query 缺參數 `A0003`）。
- 修正前端 `@click` 型別錯誤，並完成 FE 全量檢查。

## 7. 最終狀態
- `LIB-API-001` 任務已達成可交付狀態。
- 目前可進入下一階段：QA 驗收與必要的收尾優化。
