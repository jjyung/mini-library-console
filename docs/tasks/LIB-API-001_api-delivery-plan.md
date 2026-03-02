# LIB-API-001 API Delivery Plan

## 文件資訊

- 計畫 ID：`LIB-API-001`
- 對應需求：`REQ-LIB-001`
- 對應架構：`ARCH-LIB-001`
- 對應契約：`docs/openapi.yaml`
- 目標：完成 MVP API 交付，並拆分 BE/FE 明確分派

---

## 1. 範圍與原則

### In Scope

- `library-books-001` 新增書籍
- `library-books-002` 查詢書籍清單
- `library-borrow-records-001` 借書
- `library-borrow-records-002` 還書
- `library-borrow-records-003` 查詢借閱紀錄

### Out of Scope

- 登入/授權
- 逾期規則、通知、罰款
- 外部服務整合與快取

### 強制原則

- 資料庫使用 `SQLite`，本地可啟動。
- 後端遵循 TDD（unit + integration）。
- 所有 API 回應必含業務錯誤碼（`00000/A0000/B0000/C0000`）。
- 前端 UI 調整不得破壞 `data-testid`。

---

## 2. 工作分解結構（WBS）

| Task ID | 類型 | 任務名稱 | Owner | 依賴 |
| --- | --- | --- | --- | --- |
| `LIB-API-001-BE-01` | BE | 建立 SQLite schema/migration（books, borrow_records, indexes） | BE | 無 |
| `LIB-API-001-BE-02` | BE | 建立 domain model/repository/service 骨架 | BE | `BE-01` |
| `LIB-API-001-BE-03` | BE | 實作 `library-books-001`（Create Book）+ 測試 | BE | `BE-02` |
| `LIB-API-001-BE-04` | BE | 實作 `library-books-002`（List Books）+ 測試 | BE | `BE-02` |
| `LIB-API-001-BE-05` | BE | 實作 `library-borrow-records-001`（Borrow）+ 交易測試 | BE | `BE-02`, `BE-03` |
| `LIB-API-001-BE-06` | BE | 實作 `library-borrow-records-002`（Return）+ 交易測試 | BE | `BE-02`, `BE-05` |
| `LIB-API-001-BE-07` | BE | 實作 `library-borrow-records-003`（List Borrow Records）+ 測試 | BE | `BE-02`, `BE-05` |
| `LIB-API-001-BE-08` | BE | 實作錯誤碼映射與全域 exception handler | BE | `BE-03`~`BE-07` |
| `LIB-API-001-BE-09` | BE | API 整合測試與契約一致性檢查 | BE | `BE-08` |
| `LIB-API-001-FE-01` | FE | 建立集中 API client 與 DTO mapper | FE | `docs/openapi.yaml` |
| `LIB-API-001-FE-02` | FE | 書籍清單 + 新增書籍 UI 串接 (`books-001`,`books-002`) | FE | `FE-01`, `BE-03`, `BE-04` |
| `LIB-API-001-FE-03` | FE | 借書 UI 串接 (`borrow-records-001`) | FE | `FE-01`, `BE-05` |
| `LIB-API-001-FE-04` | FE | 還書 UI 串接 (`borrow-records-002`) | FE | `FE-01`, `BE-06` |
| `LIB-API-001-FE-05` | FE | 借閱紀錄 UI 串接 (`borrow-records-003`) | FE | `FE-01`, `BE-07` |
| `LIB-API-001-FE-06` | FE | 錯誤碼映射與提示文案（不依賴 HTTP status） | FE | `FE-02`~`FE-05`, `BE-08` |
| `LIB-API-001-FE-07` | FE | `data-testid` 補齊與回歸確認 | FE | `FE-02`~`FE-06` |
| `LIB-API-001-JOINT-01` | Joint | BE/FE E2E smoke（create -> borrow -> return） | BE+FE | `BE-09`, `FE-07` |

---

## 3. BE 任務分派明細

### `LIB-API-001-BE-01` SQLite schema/migration

- 交付：建立 `books`、`borrow_records`、必要 index。
- 完成條件：
  - 可在本地初始化資料庫。
  - 欄位與約束符合 `docs/schema/*.md`。

### `LIB-API-001-BE-02` 分層骨架

- 交付：Controller / Service / Repository / DTO / Exception skeleton。
- 完成條件：
  - 專案可編譯。
  - 契約物件命名符合 AGENTS.md。

### `LIB-API-001-BE-03`~`BE-07` API 實作

- 交付：5 支 API 完整可用。
- 完成條件：
  - `operationId` 對應功能可被測試觸發。
  - 成功回傳 `00000`。
  - 失敗情境回傳 `A0000/B0000` 與子碼。

### `LIB-API-001-BE-08` 錯誤處理

- 交付：全域 exception handler + business code mapper。
- 完成條件：
  - 不以 HTTP status 單獨判斷成功失敗。
  - 例外 log 含 stack trace。

### `LIB-API-001-BE-09` 後端驗證

- 交付：整合測試報告。
- 完成條件：
  - 借還書交易一致性測試通過。
  - 空庫存借書回 `A0000` 類錯誤。

---

## 4. FE 任務分派明細

### `LIB-API-001-FE-01` API client

- 交付：集中 API 呼叫模組（無 component 內直呼 fetch/axios）。
- 完成條件：
  - 所有 API 呼叫從同一 client 進出。
  - DTO 與 `docs/openapi.yaml` 對齊。

### `LIB-API-001-FE-02`~`FE-05` 頁面串接

- 交付：新增書籍、書籍清單、借書、還書、借閱紀錄 UI 串接。
- 完成條件：
  - 完整支援 REQ happy path。
  - 顯示欄位符合 FR（書名/作者/庫存/可借/狀態/借閱資訊）。

### `LIB-API-001-FE-06` 錯誤映射

- 交付：`A0000/B0000/C0000` 對應 UI 訊息。
- 完成條件：
  - 不只看 HTTP status。
  - 對 `A0004`（庫存不足）顯示可理解文案。

### `LIB-API-001-FE-07` test locator

- 交付：互動元件 `data-testid` 完整。
- 完成條件：
  - 新增或調整元件不破壞既有 test id。
  - E2E 全部改用 `getByTestId()`。

---

## 5. 里程碑與時序

1. M1（Day 1）：`BE-01`~`BE-04` + `FE-01` 完成
2. M2（Day 2）：`BE-05`~`BE-08` + `FE-02`~`FE-04` 完成
3. M3（Day 3）：`BE-09` + `FE-05`~`FE-07` + `JOINT-01` 完成

---

## 6. 測試策略

### Backend

- Unit tests：
  - 借書/還書規則
  - 狀態轉換（AVAILABLE/CHECKED_OUT）
- Integration tests：
  - SQLite transaction 行為
  - API response business code

### Frontend

- Component/integration：
  - 表單驗證與錯誤訊息映射
- E2E smoke：
  - `create book -> borrow -> return`
  - 全程使用 `data-testid`

---

## 7. 需求追溯（Task -> REQ）

| Task | REQ 對應 |
| --- | --- |
| `BE-03`, `FE-02` | `FR-LIB-001`, `AC-LIB-001` |
| `BE-04`, `FE-02` | `FR-LIB-002` |
| `BE-05`, `FE-03` | `FR-LIB-003`, `AC-LIB-002`, `AC-LIB-003` |
| `BE-06`, `FE-04` | `FR-LIB-004`, `AC-LIB-004` |
| `BE-07`, `FE-05` | `FR-LIB-005`, `AC-LIB-005` |
| `BE-08`, `FE-06` | `FR-LIB-006`, `AC-LIB-006` |
| `BE-09`, `FE-07`, `JOINT-01` | `NFR-LIB-001`~`NFR-LIB-005` |

---

## 8. 風險與緩解

1. 風險：SQLite 併發下庫存扣減競爭。
   - 緩解：借還書使用單交易與條件更新；新增競爭測試。
2. 風險：FE/BE DTO 不一致。
   - 緩解：以 `docs/openapi.yaml` 為唯一契約源，PR 檢查必比對。
3. 風險：UI 調整破壞 E2E。
   - 緩解：`data-testid` 作為固定介面，不隨樣式變動。

---

## 9. 交付物清單

- `apps/api/library-mini-admin-api/**`（API 實作 + 測試）
- `apps/web/library-mini-admin-web/**`（UI 串接 + 測試）
- `docs/tasks/LIB-API-001_api-delivery-summary.md`（交付總結，下一階段產出）
