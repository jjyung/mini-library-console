# LIB-API-001 API Delivery Plan (BE/FE Split)

## 文件資訊

- Plan ID: `LIB-API-001`
- 來源需求: `REQ-LIB-001`
- 來源架構: `ARCH-LIB-001`
- 來源設計: `docs/openapi.yaml`, `docs/api/*.md`, `docs/schema/LIB-SCHEMA-001_library-domain-model.md`
- 版本: `1.0.0`
- 狀態: Ready for Execution

## 1. 交付目標

完成 MVP 借閱管理核心流程，涵蓋：

- 建立書籍（`library-books-001`）
- 查詢書籍清單（`library-books-002`）
- 借出書籍（`library-borrow-records-001`）
- 歸還書籍（`library-borrow-records-002`）

並滿足：

- SQLite 本地可跑
- 無外部服務依賴
- 無快取層
- 錯誤碼統一（`00000`/`A0000`/`B0000`/`C0000`）
- 前端保留 `data-testid`

## 2. 角色分工與責任邊界

### BE Owner

- 負責 API 與 domain rule 實作。
- 負責 SQLite schema/migration 與 repository。
- 負責 TDD（unit + integration）覆蓋借還書關鍵規則。
- 輸出可供 FE 串接的穩定 contract（對齊 `docs/openapi.yaml`）。

### FE Owner

- 負責管理介面流程與互動狀態。
- 透過集中 API client 呼叫後端（禁止在元件內直呼 fetch/axios）。
- 負責錯誤碼映射與 UI 文案。
- 負責 `data-testid` 佈點與 Playwright smoke 案例。

## 3. 任務拆分（BE）

### BE-001 基礎建設與環境設定

- 目標:
  - 建立 SQLite local/test 雙設定。
  - 建立資料初始化機制（schema.sql 或 migration）。
- 主要檔案:
  - `apps/api/library-mini-admin-api/src/main/resources/application.properties`
  - `apps/api/library-mini-admin-api/src/main/resources/application-local.properties`
  - `apps/api/library-mini-admin-api/src/test/resources/application-test.properties`
  - `apps/api/library-mini-admin-api/src/main/resources/schema.sql`
- 驗收:
  - 本地可啟動 API。
  - 測試環境使用獨立 DB 檔案。

### BE-002 Domain Model 與 Repository

- 目標:
  - 落地 `books`, `borrow_records`。
  - 實作查詢可借數量聚合。
- 主要檔案:
  - `apps/api/library-mini-admin-api/src/main/java/**/domain/**`
  - `apps/api/library-mini-admin-api/src/main/java/**/infrastructure/**`
- 驗收:
  - 可正確計算 `availableQuantity`。
  - 支援借書/還書的資料更新。

### BE-003 API `library-books-001` / `library-books-002`

- 目標:
  - `POST /books`, `GET /books` 依 OpenAPI 完整實作。
- 主要檔案:
  - `apps/api/library-mini-admin-api/src/main/java/**/controller/**`
  - `apps/api/library-mini-admin-api/src/main/java/**/application/**`
  - `apps/api/library-mini-admin-api/src/main/java/**/dto/**`
- 驗收:
  - 回應封裝含 `code/message/data`。
  - 錯誤情境回傳 `A0000` 或 `B0000`。

### BE-004 API `library-borrow-records-001` / `library-borrow-records-002`

- 目標:
  - `POST /borrow-records`、`PATCH /borrow-records/{borrowRecordId}/return`。
  - 交易邏輯保證一致性。
- 驗收:
  - 可借數量為 0 時拒絕借出（`A0000`）。
  - 已歸還紀錄不可重複歸還（`A0000`）。
  - 交易異常回 `B0000`。

### BE-005 全域例外與錯誤碼映射

- 目標:
  - 建立統一 exception handler。
  - 將 validation/domain/db error 映射至業務錯誤碼。
- 驗收:
  - 任一失敗 API 均具一致回應結構。

### BE-006 測試（TDD）

- 目標:
  - 補齊 unit + integration 測試。
- 測試重點:
  - 借書成功後可借數量下降。
  - 無庫存時借書失敗。
  - 歸還成功後可借數量恢復。
  - 重複歸還失敗。
- 驗收:
  - `npm run check:api` 通過。

## 4. 任務拆分（FE）

### FE-001 API Client 與 DTO 對齊

- 目標:
  - 建立集中 API client。
  - 對齊 `docs/openapi.yaml` DTO。
- 主要檔案:
  - `apps/web/library-mini-admin-web/src/services/**`
  - `apps/web/library-mini-admin-web/src/types/**`
- 驗收:
  - 元件內無直接 `fetch/axios`。

### FE-002 狀態管理（Store）

- 目標:
  - 建立 books/borrow flow store（state/action/getter 分離）。
- 主要檔案:
  - `apps/web/library-mini-admin-web/src/stores/**`
- 驗收:
  - UI 狀態由 store 控制，無全域 mutable 變數。

### FE-003 UI 流程：新增書籍與清單

- 目標:
  - 實作新增書籍表單與清單顯示。
  - 顯示 `totalQuantity`、`availableQuantity`、`status`。
- 主要檔案:
  - `apps/web/library-mini-admin-web/src/components/**`
  - `apps/web/library-mini-admin-web/src/views/**`
- 驗收:
  - 對齊 Figma 參考（`docs/figma/library-mini-admin-console/`）。
  - 所有互動元件具 `data-testid`。

### FE-004 UI 流程：借書/還書

- 目標:
  - 實作借書、還書操作與列表即時更新。
- 驗收:
  - 借出成功後可借數量下降。
  - 還書成功後可借數量恢復。
  - 可借數量 0 時操作受阻並顯示對應錯誤。

### FE-005 錯誤處理與映射

- 目標:
  - 建立業務錯誤碼對應 UI 文案（至少 `A0000/B0000/C0000`）。
- 驗收:
  - UI 不只依 HTTP status 判斷結果。

### FE-006 E2E Smoke

- 目標:
  - 新增 1 條 smoke：create book -> borrow -> return。
- 主要檔案:
  - `apps/web/library-mini-admin-web/e2e/*.spec.ts`
- 驗收:
  - 只用 `page.getByTestId()` 定位。
  - `npm run e2e` 通過。

## 5. 任務依賴關係

1. `BE-001` -> `BE-002` -> `BE-003`/`BE-004` -> `BE-005` -> `BE-006`
2. `FE-001` -> `FE-002` -> `FE-003`/`FE-004` -> `FE-005` -> `FE-006`
3. `FE-003`/`FE-004` 依賴 `BE-003`/`BE-004` contract 穩定

## 6. 里程碑與交付批次

### Milestone 1: Contract-Ready

- 完成 BE API skeleton + FE client 型別對齊。
- 輸出可串接 mock/real API。

### Milestone 2: Feature-Complete

- 完成新增/借出/歸還全流程。
- 完成錯誤碼映射與主要測試。

### Milestone 3: QA-Ready

- `npm run check`、`npm run e2e` 通過。
- 文件與程式行為一致，可移交 QA 驗收。

## 7. Definition of Done（DoD）

- OpenAPI 與實作一致，無未定義欄位。
- API 全部回應結構統一為 `code/message/data`。
- 關鍵業務規則（不可超借、不可重複歸還）已測試覆蓋。
- 前端互動元件均保留 `data-testid`。
- `npm run check` 與 `npm run e2e` 可在本地通過。

## 8. 風險與緩解

- 風險: BE/FE DTO 欄位命名不一致。
- 緩解: 以 `docs/openapi.yaml` 為單一契約來源，PR 要求對照。

- 風險: 借還書交易競態造成超借。
- 緩解: BE 以 transaction 包覆檢查與寫入，補 integration test。

- 風險: UI 調整破壞測試定位。
- 緩解: PR checklist 強制檢查 `data-testid` 與 E2E smoke。

## 9. 建議執行順序（可直接分派）

1. BE Owner: `BE-001` 到 `BE-004`（先完成可串接 API）
2. FE Owner: `FE-001` 到 `FE-004`（同步串接 UI 流程）
3. BE Owner: `BE-005`、`BE-006`
4. FE Owner: `FE-005`、`FE-006`
5. 共同: 跑 `npm run check`、`npm run e2e`，修正後移交 QA
