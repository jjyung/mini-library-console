# ARCH-LIB-001

## 文件資訊

- Architecture ID: `ARCH-LIB-001`
- 文件名稱: Library Mini Admin Console MVP 架構設計
- 來源需求: `REQ-LIB-001`（`docs/requirements/REQ-LIB-001.md`）
- 版本: `1.0.0`
- 狀態: Draft

## 1. 架構目標與約束

### 1.1 目標

- 支援 MVP 核心流程：新增書籍、借書、還書。
- 以最小複雜度滿足資料一致性與可測試性。
- 保持前後端可獨立開發與本地快速啟動。

### 1.2 強制約束

- 資料庫使用 `SQLite`。
- 系統必須可本地執行，不依賴外部服務。
- 不使用快取層（含 Redis、in-memory domain cache）。
- API 錯誤需輸出業務錯誤碼（`00000`/`A0000`/`B0000`/`C0000`）。
- 前端 UI 實作需對齊 `docs/figma/library-mini-admin-console/`，且保留 `data-testid`。

## 2. 系統邊界

```text
[Admin Browser]
    |
    | HTTP/JSON (localhost)
    v
[Web SPA: Vue 3 + Vite]
    |
    | REST API
    v
[API: Spring Boot]
    |
    | JDBC / JPA
    v
[SQLite file DB]
```

- 部署拓樸為單機雙程序：`web` 與 `api`。
- 無訊息佇列、無外部身分驗證、無第三方整合。

## 3. 邏輯架構

### 3.1 Backend（Spring Boot）

採分層單體（Layered Monolith）：

- `controller`：處理 HTTP request/response 與 DTO 驗證。
- `application`：用例協調（CreateBook、BorrowBook、ReturnBook、ListBooks）。
- `domain`：業務規則（可借數量計算、不可超借、不可重複歸還）。
- `infrastructure`：SQLite persistence（Repository/Mapper）。

設計原則：

- Controller 不放業務規則。
- 業務規則集中在 domain/application，便於單元測試。
- Repository 只負責資料存取，不做流程判斷。

### 3.2 Frontend（Vue 3）

採 UI 與業務邏輯分離：

- `views/components`：純 UI 呈現與互動事件。
- `services/apiClient`：集中 API 呼叫，不在元件內直接 `fetch/axios`。
- `store`（Pinia）：管理 state/action/getter。
- `mappers`：DTO 與 UI model 映射（若欄位轉換需要）。

設計原則：

- 所有可互動元件提供穩定 `data-testid`。
- 不在模板層放業務規則。
- 前端錯誤顯示基於業務錯誤碼，不僅依賴 HTTP status。

## 4. 交易與一致性策略（SQLite）

### 4.1 實體模型（MVP）

- `books`
- `borrow_records`

建議核心欄位：

- `books`: `id`, `title`, `author`, `total_quantity`, `created_at`, `updated_at`
- `borrow_records`: `id`, `book_id`, `borrower_name`, `borrowed_at`, `returned_at`, `status`, `created_at`, `updated_at`

狀態建議：

- `borrow_records.status`: `BORROWED`, `RETURNED`

### 4.2 一致性規則

- `available_quantity = total_quantity - count(status = BORROWED)`
- 借書與還書需在單一 transaction 內完成。
- 借書流程先檢查可借數量，再寫入借閱紀錄。
- 歸還流程需驗證紀錄狀態為 `BORROWED` 才可更新為 `RETURNED`。

### 4.3 併發策略（本地 MVP）

- 使用資料庫 transaction 保證原子性。
- 借書交易建議使用 `BEGIN IMMEDIATE`（或等效寫鎖策略）降低超借風險。
- 若交易衝突，回傳系統錯誤碼（`B0000`）並記錄 stack trace。

## 5. API 架構與契約方向

### 5.1 API 清單（MVP）

- `library-books-001`：建立書籍
- `library-books-002`：查詢書籍清單（含可借數量與狀態）
- `library-borrow-records-001`：借出書籍
- `library-borrow-records-002`：歸還書籍

### 5.2 命名規範（供 SD/OpenAPI 落地）

- API ID 使用 `{service-name}-{resource-name-plural}-{seq}`。
- `operationId` 使用 path style，不使用 API ID。
- DTO 使用 `{HttpMethod}{ResourcePlural}RequestDTO/ResponseDTO`。

### 5.3 回應封裝（業務錯誤碼）

所有 API 回應需含：

- `code`：業務錯誤碼（`00000`/`A0000`/`B0000`/`C0000`）
- `message`：可讀訊息
- `data`：業務資料（成功時）

## 6. 部署與執行架構（Local）

### 6.1 程序與埠

- Web：Vite dev server（預設 `5173`）
- API：Spring Boot（預設 `8080`）
- DB：SQLite file（建議存於 API 專案內 `data/`）

### 6.2 啟動方式

- `npm run dev:api`
- `npm run dev:web`
- 或 `npm run dev`

### 6.3 設定建議

- `application.properties` 使用 profile 區分：`local`、`test`。
- `test` profile 使用獨立 SQLite 檔案，避免污染本地資料。

## 7. 安全與非功能考量（MVP）

- 本階段不實作登入授權（依 REQ out-of-scope）。
- API 輸入需基本驗證（必填、長度、數量不得小於 0）。
- 例外處理採全域 handler，統一轉為業務錯誤碼。
- 保留 actuator 基礎健康檢查供本地診斷。

## 8. REQ 對應矩陣

| REQ | 架構對應 |
| --- | --- |
| FR-LIB-001 建立書籍 | `PostBooks` 用例 + `books` persistence + UI 新增表單 |
| FR-LIB-002 清單與可借狀態 | `GetBooks` 用例 + 聚合計算 available quantity |
| FR-LIB-003 借出書籍 | `PostBorrowRecords` 用例 + transaction + BORROWED 紀錄 |
| FR-LIB-004 歸還書籍 | `PatchBorrowRecords` 用例 + status 轉換 RETURNED |
| FR-LIB-005 一致性保護 | domain rule + transaction + 全域錯誤碼封裝 |
| NFR-LIB-001 本地可跑 | 單機雙程序 + SQLite file |
| NFR-LIB-002 一致性正確性 | 交易邊界 + 規則集中於 domain/application |
| NFR-LIB-003 可測試性 | `data-testid` 保留 + API/client 分層 |
| NFR-LIB-004 錯誤標準化 | 統一 response envelope + 全域 exception handling |
| NFR-LIB-005 UI 對齊 | 以前端分層方式對齊 Figma 參考並維持 locator |

## 9. 風險與對策

- 風險: SQLite 併發寫入衝突導致借書失敗。
- 對策: 縮小交易範圍、實作重試上限、失敗轉標準錯誤碼。

- 風險: 前端直接顯示 HTTP 錯誤訊息造成語意不一致。
- 對策: 建立錯誤碼映射表，統一 UI error 文案。

- 風險: UI 調整破壞 E2E selector。
- 對策: 將 `data-testid` 納入 PR 檢查清單與 E2E smoke case。

## 10. 後續交付邊界（給 SD/PG）

- SD 需依本文件產出：`docs/openapi.yaml`、`docs/schema/*.md`、`docs/api/*.md`。
- PG 需依 API 與資料模型落地程式，並以 TDD 驗證借還書一致性。
- QA 需以 `REQ-LIB-001` AC 與 NFR 驗收，並優先使用 `data-testid`。
