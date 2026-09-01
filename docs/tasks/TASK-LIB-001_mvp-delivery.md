# TASK-LIB-001｜Library Mini Admin MVP Delivery Plan

## 1. 任務資訊

- Scenario：`SCN-LIB-001`
- Requirement：`REQ-LIB-001`
- Architecture：`ARCH-LIB-001`
- SD contract：`docs/openapi.yaml`
- Owner：PG
- Status：`blocked_by_environment`
- Created：2026-09-01
- Scope：新增書籍、館藏列表、借書、還書、業務結果回饋與 Figma UI 對齊。
- Out of scope：登入／權限、搜尋行為、預約、通知、罰款與借閱歷史查詢 UI。

## 2. Contract 與共用決策

- API IDs：`library-books-001`、`library-books-002`、`library-loans-001`、`library-loans-002`。
- API source of truth：`docs/openapi.yaml`；FE/BE 不得自行改 path、欄位或 business code。
- Global error source：`docs/error-codes.md`；UI 依 `code` 映射，不以 HTTP status 單獨判斷。
- Store profile：本次先使用 API process-local repository，支援本地 smoke；不宣稱跨 API 重啟保存。Q-005 若要求耐久性，另開 persistence implementation task。
- 借閱人：先使用 `readerId`，符合目前 Figma export；Q-001 若決定使用姓名或強制比對，回 SD 更新 contract 後再實作。
- 到期日：接受 optional `dueDate` 並保存；不自動補預設期限、不計算逾期／罰款。Q-003 決策前不得加入罰款行為。
- 還書：在未凍結 Q-001/Q-002 前，依 SD flow 以 ISBN 找 active loan；若有多筆，使用最早借出紀錄（FIFO）作為暫定策略。

## 3. Ownership Split

### 3.1 BE：`apps/api/**`

- 負責 Spring Boot controller、application service、in-memory repository、DTO／mapper、business code response 與測試。
- 必須遵循 TDD：先新增／更新測試，再實作 production code。
- 必須維持書籍數量與 loan 狀態一致；借出／歸還是同一一致性邊界。
- 不可修改 `docs/` contract、`apps/web/**` 或 `.codex/**`。

### 3.2 FE：`apps/web/**`

- 負責 Vue SPA、集中式 API client、store/composable、Figma 對齊 UI、錯誤碼 mapper 與 E2E locator。
- Development API base URL 由 `apps/web/library-mini-admin-web/.env.development` 的 `VITE_API_BASE_URL` 提供，目前設定為 `http://localhost:8080`。
- 所有 API 呼叫只能經 centralized API client；元件不得直接使用 fetch／axios。
- 不可修改 `docs/` contract、`apps/api/**` 或 `.codex/**`。
- 既有 root smoke locator 必須維持；新增 locator 使用本節定義的穩定 `data-testid`。

### 3.3 PG：協調與整合

- 維護本計畫、workflow state、gate 狀態與 delivery summary。
- 不直接取代 BE/FE 的責任；負責 contract review、整合檢查與阻塞升級。

## 4. Task Breakdown

| Task | Owner | Scope | Depends on | Exit criteria |
| --- | --- | --- | --- | --- |
| BE-001 | BE | 先寫 domain/application service 測試：新增書籍、列表、借出、還書、重複 ISBN、無庫存、未上架、無 active loan。 | SD contract | 測試明確覆蓋 `00000`、`A0001`～`A0006`、`B0001` 與數量／狀態轉移。 |
| BE-002 | BE | 實作 in-memory store、書籍／loan domain、service 與一致性更新。 | BE-001 | `availableCount`、status、active loan 在成功／失敗後符合 REQ；無 partial update。 |
| BE-003 | BE | 實作 REST controller、DTO mapper、統一 success/error envelope、traceId 與錯誤處理。 | BE-002 | 四個 API path／API ID／DTO 名稱與 OpenAPI 一致。 |
| BE-004 | BE | Backend integration tests 與 readiness note。 | BE-003 | `npm run check:api` 通過，記錄測試與 local run 假設。 |
| FE-001 | FE | 建立 centralized API client、models/mapper、business error mapper 與 store/composable。 | SD contract | 元件不直接呼叫 HTTP；錯誤依 business code 顯示。 |
| FE-002 | FE | 實作頁首、借還操作卡、新增書籍表單、館藏列表與空狀態，對齊 Figma export。 | FE-001 | 雙欄／窄畫面單欄、表格水平捲動、控制項狀態與文案符合 REQ。 |
| FE-003 | FE | 綁定新增／借出／歸還互動、成功／錯誤回饋與穩定 `data-testid`。 | FE-001, FE-002 | 成功後重新同步列表；失敗不改變本地館藏狀態。 |
| FE-004 | FE | Frontend type-check、lint、E2E smoke。 | BE-004, FE-003 | `npm run check:web` 與核心 smoke E2E 結果已記錄。 |
| PG-001 | PG | Contract review、整合啟動與 Gate-A/B/C/D 管理。 | BE-004, FE-004 | FE/BE 變更不超出 contract，所有 blockers 有 owner/remediation。 |

## 5. Stable UI Test IDs

FE 應優先使用下列 IDs；動態 row 使用 book ID 作為後綴，禁止依文字或 CSS class 作為唯一契約：

| Area | `data-testid` |
| --- | --- |
| App shell | `library-admin-page`, `topbar`, `topbar-search` |
| Transaction tabs | `borrow-tab`, `return-tab` |
| Borrow form | `borrow-form`, `borrow-reader-id`, `borrow-isbn`, `borrow-due-date`, `borrow-submit`, `borrow-result` |
| Return form | `return-form`, `return-isbn`, `return-reader-id`, `return-submit`, `return-result` |
| Add book | `add-book-form`, `add-book-title`, `add-book-isbn`, `add-book-author`, `add-book-category`, `add-book-quantity`, `add-book-active`, `add-book-submit`, `add-book-result` |
| Catalogue | `book-table`, `book-empty-state`, `book-row-{bookId}`, `book-status-{bookId}`, `quick-borrow-{bookId}`, `quick-return-{bookId}` |

## 6. Gate Plan

### Gate-A：BE contract-critical implementation

- Entry：SD artifacts 已存在且 BE 只讀 `docs/openapi.yaml`。
- Required：BE-001～BE-003 完成，所有 controller path 與 business code 有測試。
- Review：PG 檢查 API ID、DTO、錯誤 envelope、狀態轉移與 no partial update。
- Status：`implemented_unverified`（BE 測試與 production code 已完成；Java runtime 尚未可用）

### Gate-B：BE integration readiness

- Entry：Gate-A 通過。
- Required：BE-004 完成，`npm run check:api` 通過，API 可由 repo script 啟動。
- Review：PG 確認 FE 使用的 response data 與 error mapping 可落地。
- Status：`blocked`（`npm run check:api` 被缺少 Java/JAVA_HOME 阻塞）

### Gate-C：FE implementation and checks

- Entry：Gate-B 通過且 API contract 未變更。
- Required：FE-001～FE-004 完成，Figma UI、responsive layout、stable test IDs 與 business-code mapping 完成。
- Review：PG 檢查無 direct HTTP call、無 locator regression、失敗不樂觀更新。
- Status：`complete_with_e2e_blocker`（`check:web` 與 production build 通過；Playwright browsers 未安裝）

### Gate-D：PG integration delivery

- Entry：Gate-B 與 Gate-C 通過。
- Required：執行 root `npm run check`；必要時執行 `npm run e2e`；確認 REQ AC traceability 與 workflow handoff。
- Output：`docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`。
- Status：`blocked`（須先解除 Gate-B 與 Playwright browser blocker）

## 7. Blockers and Decisions

| ID | Item | Owner | Impact | Remediation |
| --- | --- | --- | --- | --- |
| Q-001 | `readerId` 與借閱人姓名語意、還書是否強制比對未定。 | Product / PG | 可能影響 DTO、UI 文案與 return rule。 | 本次採 `readerId`；決策改變時回 SD。 |
| Q-002 | 多副本同 ISBN 的精準歸還策略未定。 | Product / PG | 目前 FIFO 是暫定行為。 | 本次以 active loan FIFO；若需精準指定，回 SD 增加 contract。 |
| Q-003 | due date 預設、逾期與罰款未定。 | Product / PG | 不可實作罰款與逾期提示。 | 本次只保存 optional dueDate，不計算費用。 |
| Q-005 | 跨重啟／跨 session 持久化未定。 | Product / PG | process-local Store 不具耐久性。 | 本次明確標示 local smoke；持久化另開 task。 |
| LOC-001 | 現有 Web skeleton 僅有 root smoke，無既有 `data-testid`。 | FE / QA | 需建立穩定 locator 合約。 | 依第 5 節新增並由 E2E 固定。 |
| ENV-001 | API checks 無法啟動：Java/JAVA_HOME 未設定。 | PG / 開發環境 | Gate-B/D 無法完成。 | 安裝 JDK 21，設定 `JAVA_HOME` 與 PATH，重跑 `npm run check:api`。 |
| ENV-002 | Playwright Chromium/Firefox/WebKit executable 未安裝。 | PG / 開發環境 | E2E smoke 無法執行。 | 執行 `npm --prefix apps/web/library-mini-admin-web exec playwright install` 後重跑 `npm run e2e`。 |

## 8. Acceptance Traceability

| Requirement AC | BE coverage | FE/E2E coverage | Gate |
| --- | --- | --- | --- |
| AC-001/002 新增與拒絕 | BE-001～004 | FE-002/003/004 | A/B/C |
| AC-003/004 列表與空狀態 | BE-004 | FE-002/004 | B/C |
| AC-005/006 借書 | BE-001～004 | FE-003/004 | A/B/C |
| AC-007/008 還書 | BE-001～004 | FE-003/004 | A/B/C |
| AC-009 business code feedback | BE-003/004 | FE-001/003/004 | B/C |
| AC-UI-001/002 Figma/responsive | — | FE-002～004 | C/D |

## 9. Handoff

- PG plan status：`implementation_integrated_blocked_by_environment`
- Completed：BE/FE implementation 已整合；FE static checks 已通過；stable `data-testid` 已保留並加入頁面互動。
- Next action：先解除 ENV-001，執行 `npm run check:api`；再解除 ENV-002，執行 `npm run e2e`，最後重跑 root `npm run check` 並交 QA。
- Contract change rule：任何 API、schema、錯誤碼或未決問題導致的契約變更，停止平行實作並回 SD。
- QA entry：Gate-D 完成後交 QA，QA 使用本計畫 locator 與 REQ AC 驗證；目前不得宣告 QA ready。
