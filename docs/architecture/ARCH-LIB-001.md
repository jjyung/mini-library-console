# ARCH-LIB-001

## 文件資訊

- 架構 ID：`ARCH-LIB-001`
- 需求來源：`REQ-LIB-001`
- 來源情境：`SCN-LIB-001`
- 架構目標：MVP（sqlite、本地可跑、不依賴外部服務、不含快取）

---

## 1. 架構目標與限制

### 1.1 目標

- 以最小成本完成建書、借書、還書、借閱追蹤。
- 保證借還書操作的資料一致性（庫存與借閱紀錄同步）。
- 提供可被前端穩定使用的 API（含業務錯誤碼）。

### 1.2 強制限制

- 資料庫：僅使用 `SQLite`（單機檔案）。
- 執行環境：本機開發機可直接啟動（API + Web）。
- 外部依賴：不使用第三方 SaaS、雲端 DB、訊息佇列。
- 快取：不引入 Redis / in-memory read cache / HTTP cache 層。

---

## 2. 系統脈絡（C4-Context 簡化）

- 使用者：管理者（同一角色）
- 前端：`apps/web/library-mini-admin-web`（Vue + Vite）
- 後端：`apps/api/library-mini-admin-api`（Spring Boot）
- 資料層：`SQLite`（本地檔案）

資料流：

1. 管理者在 Web 執行新增、借出、歸還。
2. Web 經由 HTTP 呼叫 API。
3. API 在單一交易中更新 SQLite。
4. API 回傳標準回應（含業務錯誤碼）。

---

## 3. 容器與模組設計（C4-Container / Component）

### 3.1 Web（Vue）

職責：

- 呈現書籍清單與借閱紀錄
- 提供新增書籍、借書、還書操作
- 僅透過集中 API client 呼叫後端
- 依業務錯誤碼做 UI 訊息映射

前端分層：

- View Components：只負責畫面與 `data-testid`
- Store：state / getter / action 分離
- Service/API Client：統一管理 HTTP 與 DTO 映射

### 3.2 API（Spring Boot）

職責：

- 驗證請求
- 執行借還書領域規則
- 維持交易一致性
- 回傳統一業務錯誤碼結構

後端分層：

- Controller：接收/回傳 DTO
- Application Service：用例協調（create/list/borrow/return）
- Domain Service：借還書規則（可借判斷、狀態轉換）
- Repository：SQLite 存取
- Exception Mapper：例外轉業務錯誤碼

### 3.3 SQLite

職責：

- 儲存書籍主檔
- 儲存借閱紀錄
- 透過交易保障寫入一致性

---

## 4. 資料設計（邏輯）

### 4.1 主要實體

- `Book`
- `BorrowRecord`

### 4.2 Book 欄位（邏輯）

- `id`（主鍵）
- `title`
- `author`
- `totalCopies`
- `availableCopies`
- `status`（`AVAILABLE` / `CHECKED_OUT`）
- `createdAt`
- `updatedAt`

### 4.3 BorrowRecord 欄位（邏輯）

- `id`（主鍵）
- `bookId`（FK -> Book）
- `borrowerName`
- `borrowedAt`
- `returnedAt`（nullable）
- `status`（`BORROWED` / `RETURNED`）

### 4.4 關鍵資料規則

- `availableCopies` 必須介於 `0` 與 `totalCopies`。
- 借書成功：新增 `BorrowRecord(BORROWED)` 並 `availableCopies - 1`。
- 還書成功：更新 `BorrowRecord(RETURNED)` 並 `availableCopies + 1`。
- `availableCopies == 0` 時，`Book.status = CHECKED_OUT`，否則 `AVAILABLE`。

---

## 5. API 邊界（MVP）

以下 API 名稱需在後續 OpenAPI 文件保留 API ID：

1. `library-books-001`：新增書籍
2. `library-books-002`：查詢書籍清單
3. `library-borrow-records-001`：借書
4. `library-borrow-records-002`：還書
5. `library-borrow-records-003`：查詢借閱紀錄

回應封裝原則：

- 成功：`code = 00000`
- 客戶端錯誤：`code = A0000`（可再細分子碼）
- 系統錯誤：`code = B0000`
- 第三方錯誤：`code = C0000`（MVP 預留，當前流程預期不使用）

---

## 6. 一致性與交易策略

### 6.1 交易邊界

借書與還書都必須在單一 DB transaction 內完成：

- 借書：檢查可借量 -> 新增借閱紀錄 -> 更新書籍可借量/狀態
- 還書：檢查借閱紀錄狀態 -> 更新借閱紀錄 -> 更新書籍可借量/狀態

### 6.2 併發策略（單機 SQLite）

- 以資料庫交易與更新條件保護可借量不可為負。
- 若更新筆數為 0（競爭失敗），回傳 `A0000` 類業務錯誤。

---

## 7. 非功能需求對應

| NFR | 架構對策 |
| --- | --- |
| `NFR-LIB-001` 本地可執行 | API 與 Web 均以本機程序啟動，DB 為本地 SQLite 檔案 |
| `NFR-LIB-002` 一致性 | 借還書採單交易，庫存與紀錄一起提交 |
| `NFR-LIB-003` 基本效能 | 單機 SQLite + 必要索引（Book.title、BorrowRecord.bookId/status） |
| `NFR-LIB-004` 可測試性 | Web 元件保留穩定 `data-testid`，E2E 僅用 testid 定位 |
| `NFR-LIB-005` 可維護性 | 前後端分層、DTO 契約對齊、集中 API client |

---

## 8. 部署與執行（Local MVP）

- API：`http://localhost:8080`
- Web：`http://localhost:5173`
- DB：專案目錄下 SQLite 檔（如 `data/library.db`）

啟動方式（根目錄）：

- `npm run dev:api`
- `npm run dev:web`
- 或 `npm run dev`

---

## 9. 可觀測性與錯誤處理

- API 記錄關鍵操作日誌：新增書籍、借書、還書（含 recordId/bookId）。
- 例外處理：Controller 層統一轉為標準錯誤回應。
- 日誌需保留 stack trace（符合 AGENTS.md 例外規範）。

---

## 10. 安全與邊界（MVP）

- 不含登入授權（Out of Scope）。
- 仍需做基本輸入驗證：必填、長度、數值範圍。
- 不信任前端傳入狀態欄位，狀態一律由後端規則推導。

---

## 11. 風險與後續決策

1. SQLite 在高併發下延展性有限：MVP 接受，後續可替換為 PostgreSQL。
2. 書名是否唯一尚未定義：暫定不唯一，改以 `id` 當唯一識別。
3. API 子錯誤碼尚待 SD 定義：需在 `docs/openapi.yaml` 補齊。

---

## 12. Traceability（REQ -> ARCH）

| REQ | 架構落點 |
| --- | --- |
| `FR-LIB-001` | Book 建立用例 + `library-books-001` |
| `FR-LIB-002` | Book 查詢用例 + `library-books-002` |
| `FR-LIB-003` | Borrow 用例 + transaction + `library-borrow-records-001` |
| `FR-LIB-004` | Return 用例 + transaction + `library-borrow-records-002` |
| `FR-LIB-005` | BorrowRecord 資料模型 + `library-borrow-records-003` |
| `FR-LIB-006` | 統一回應包裝與 exception mapper |
| `NFR-LIB-001~005` | 第 7 章對應表 |
