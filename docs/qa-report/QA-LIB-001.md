# QA-LIB-001

## Metadata

- Scenario ID: SCN-LIB-001
- Requirement ID: REQ-LIB-001
- Workflow ID: WF-LIB-001
- Report Type: QA verification report
- Status: blocked
- Execution date/time: 2026-09-04 18:18 CST
- Commit/build: 89b7721；working tree has uncommitted changes
- Environment: local；Node 22.21.0、npm 11.12.0、Java 21.0.3、Spring Boot 3.5.12-SNAPSHOT；Playwright 1.58.2；Chromium、Firefox、WebKit；test-only 免登入、預設 Admin、不加密；frontend 使用 VITE_API_BASE_URL=http://localhost:8080
- Evidence redaction: none present；僅使用 synthetic test IDs、ISBN 與 reader IDs，沒有 secrets 或 personal data

## Scope and decision

- Target user journey: 管理員建立館藏、在列表確認狀態、借出最後一本、確認已借出與動作 disabled、歸還後恢復可借，並驗證欄位驗證、業務錯誤、系統錯誤、空列表及窄螢幕 UI。
- Included: FR-001～FR-005、FR-UI-001、NFR-001～NFR-005、AC-001～AC-010、AC-UI-001。
- Excluded: 登入／權限、搜尋行為、預約、通知、罰款政策、書籍編輯／刪除、獨立增加 copies、正式視覺 pixel-diff；這些不在 SCN-LIB-001 的凍結範圍，或目前沒有可重現的 Figma diff 工具／核准差異清單。
- Decision: fail／blocked。12 項核心需求觀察到瀏覽器無法讀取館藏，10 項以 mock 或 client-only evidence 得到部分驗證；沒有任何需求達到完整 Pass。根因是 frontend 5173 對 backend 8080 的 cross-origin API request 被 CORS 拒絕，屬 Product defect，需 FE／BE 修正後重跑。

## Preflight

| Dependency | Endpoint/port | Status | Evidence | Notes |
| --- | --- | --- | --- | --- |
| Backend | http://localhost:8080 / 8080 | ready | lsof 顯示 Java PID 53133 listening；curl GET /api/books 回 HTTP 200、business code 00000 | OPTIONS preflight with Origin http://localhost:5173 回 HTTP 403 Invalid CORS request；response 沒有 Access-Control-Allow-Origin。 |
| Frontend | http://localhost:5173 / 5173 | ready | lsof 顯示 Node PID 53864 listening；curl / 回 HTTP 200 | 前端以 VITE_API_BASE_URL=http://localhost:8080 啟動；Vite config 沒有 proxy。 |

## Test architecture and data isolation

- Spec(s): apps/web/library-mini-admin-web/e2e/library.spec.ts。
- Fixtures and scopes: apps/web/library-mini-admin-web/e2e/fixtures.ts 提供 test-scoped LibraryAdminPage 與 LibraryDataClient；每個 test 結束後回傳本 test 記錄的 active loans。
- POM/component objects: apps/web/library-mini-admin-web/e2e/library-page.ts 封裝頁面開啟、欄位填寫、分頁、列狀態與快速動作；測試保留 business assertions 與 API response code assertions。
- API/service setup: apps/web/library-mini-admin-web/e2e/library-data.ts 透過 APIRequestContext 建立已知書籍與借閱前置資料，避免用 UI setup 污染 journey；使用 GET /api/books、POST /api/books、POST /api/loans/borrow、POST /api/loans/return。
- Data identity/namespace: createTestBook 以 run、project、worker、parallel index、test ID 與 hash 組成唯一 title／ISBN；reader ID 同樣使用 test namespace，避免依賴固定 mutable IDs。
- Cleanup and interrupted-run recovery: 沒有 delete 或 test-reset API；fixture 對 tracked active loans 做 best-effort return，剩餘 synthetic books 以唯一 namespace 隔離。中斷後不會清除資料，但不會留下預期中的 active loan；正式環境不應使用這組 QA data strategy。
- Locator contract: 使用既有 data-testid（例如 book-table、book-row-{isbn}、quick-borrow-{isbn}、quick-return-{isbn}、catalogue-error、catalogue-retry、catalogue-empty）及 role／label semantic locators；沒有 CSS selector、XPath、nth 或固定 timeout。既有 starter vue.spec.ts assertion 已由 QA-owned scenario spec 取代，未修改產品 locator。

## Evidence and execution

| Run | Command | Result | First failure/retry | Artifacts |
| --- | --- | --- | --- | --- |
| Targeted | QA_API_BASE_URL=http://localhost:8080 npm --prefix apps/web/library-mini-admin-web run test:e2e -- --grep 'client-side\|empty catalogue\|catalogue system failure\|narrow viewport' | 12 passed（3 browsers × 4 tests）；Chromium empty/system targeted 2 passed | Mocked empty、B0000/retry、narrow viewport 與 client validation 可重現通過；這些不等同於 real API integration pass。 | apps/web/library-mini-admin-web/e2e/library.spec.ts；apps/web/library-mini-admin-web/playwright-report/ |
| Full | QA_API_BASE_URL=http://localhost:8080 npm run e2e | 27 tests：12 passed、15 failed，使用 3 workers | 5 個 real API tests 在 library-page.ts:10 等待 book-table 時失敗；頁面顯示 館藏載入失敗、錯誤碼 B0000。 | apps/web/library-mini-admin-web/test-results/**/error-context.md；apps/web/library-mini-admin-web/playwright-report/ |
| Stability 1 | QA_API_BASE_URL=http://localhost:8080 npm run e2e | 12 passed、15 failed | 與 Full 相同；不是 retry-only failure。 | apps/web/library-mini-admin-web/test-results/**/error-context.md |
| Stability 2 | QA_API_BASE_URL=http://localhost:8080 npm run e2e | 12 passed、15 failed | 與 Stability 1 相同，三個 browser 均重現 CORS 導致的 catalogue B0000。 | apps/web/library-mini-admin-web/test-results/**/error-context.md |
| Stability 3 | QA_API_BASE_URL=http://localhost:8080 npm run e2e | 12 passed、15 failed | 與 Stability 1、2 相同；未觀察到可疑 flaky 分歧。 | apps/web/library-mini-admin-web/test-results/**/error-context.md |
| Parallelism diagnostic | QA_API_RUN_ID=par2 QA_API_BASE_URL=http://localhost:8080 npm --prefix apps/web/library-mini-admin-web run test:e2e -- --project=chromium --fully-parallel --workers=2 --grep 'creates, borrows\|unknown book\|duplicate ISBN\|fully borrowed\|no active loan'；對照 workers=1 | workers=2：5 failed；workers=1：5 failed | 兩種 worker 設定均在同一個 book-table／catalogue load gate 失敗；normal full run 顯示 3 workers。 | 同一組 test-results error-context.md；失敗根因仍為 Product CORS，不是資料競爭。 |

## Coverage Matrix

| Item | Type | Risk | Status | Journey/oracle | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| FR-001 | FR | high | Fail | 新增後列表出現書籍，00000，初始可借數等於總數 | library.spec.ts:18-63；Full 15 real API failures | UI 開啟館藏時先因 CORS 顯示 B0000，未能完成 real create journey。 |
| FR-002 | FR | high | Partial | 列表呈現欄位／狀態；空資料顯示尚無館藏；讀取失敗不可冒充空列表 | library.spec.ts:140-185；preflight curl | empty 與 B0000/retry mock 於三瀏覽器通過；真實 GET 在瀏覽器被 CORS 阻擋。 |
| FR-003 | FR | high | Fail | 借出成功減少 1；最後一本變已借出且 quick borrow disabled；A0000 不改狀態 | library.spec.ts:18-48、74-89、106-121；Full | real API 借出流程均未通過 catalogue load gate。 |
| FR-004 | FR | high | Fail | 歸還成功增加 1 並恢復狀態；無借閱歸還回 A0000 且數量不變 | library.spec.ts:49-62、123-138；Full | real API 歸還流程均未通過 catalogue load gate。 |
| FR-005 | FR | high | Partial | 成功／業務錯誤／系統錯誤都顯示可理解回饋並帶 business code，失敗不部分更新 | library.spec.ts:65-72、74-89、156-185；Full | client validation 及 mocked B0000/retry 通過；real mutation feedback 未能驗證。 |
| FR-UI-001 | FR-UI | medium | Partial | Figma 主要區塊、借還卡、新增表單、列表、窄螢幕排列與 disabled 狀態可辨識 | library.spec.ts:187-217；local Figma export files | 390×844 mocked fixture 通過結構可見性；完整 real catalogue、正式 pixel-diff 與差異核准未完成。 |
| NFR-001 | NFR | high | Fail | 成功異動同時更新借閱關係、數量、狀態；失敗不部分更新 | library.spec.ts:18-63、74-138；Full | real browser journey 被 CORS 阻斷，無法證明一致性；因此不能判定通過。 |
| NFR-002 | NFR | medium | Partial | 欄位標籤／必填提示／錯誤訊息清楚；不可操作 quick action disabled | library.spec.ts:65-72、106-121、156-185 | client-only、mocked error、mocked disabled state 有證據；真實資料頁面不可讀取。 |
| NFR-003 | NFR | medium | Partial | 桌面／窄螢幕區塊順序、表單、分頁、表格與回饋符合 Figma 基準 | library.spec.ts:187-217；Figma export | 窄螢幕結構 smoke 通過；未完成 full visual comparison 或 real data states。 |
| NFR-004 | NFR | high | Partial | API/UI 映射 00000、A0000、B0000、C0000，HTTP status 不取代 business code | library.spec.ts:25-29、35-39、50-54、81-87、96-103、129-135、156-185；preflight curl | mock B0000、client error code assertions 與直接 API 00000 有證據；瀏覽器 real API 受 CORS 影響，C0000 在本情境不應觸發。 |
| NFR-005 | NFR | medium | Fail | 一次有效新增／借出／歸還在同一流程顯示結果，不需手動刷新 | library.spec.ts:18-63；Full | real operations 未能由瀏覽器完成，正常流程回應要求未達成。 |
| AC-001 | AC | high | Fail | 有效書籍新增回 00000，列表出現並有正確初始狀態 | library.spec.ts:18-33；Full | catalogue load CORS failure prevents acceptance journey. |
| AC-002 | AC | medium | Partial | 缺少必填或數量無效回 A0000，保留可修正輸入 | library.spec.ts:65-72；lint passed | client-side validation 與 input preservation 通過；未能透過 UI 驗證 server A0000。 |
| AC-003 | AC | high | Fail | 重複 ISBN 回 A0000，列表只保留一筆 | library.spec.ts:91-104；Full | API setup 可建立 fixture，但 browser catalogue load 在實際驗證前失敗。 |
| AC-004 | AC | high | Fail | 有可借副本時借出成功、數量減 1 且仍可借 | library.spec.ts:18-48；Full | browser cannot load catalogue due CORS. |
| AC-005 | AC | high | Fail | 借出最後一本後 0、已借出、quick borrow disabled | library.spec.ts:18-48；Full | browser cannot load catalogue due CORS. |
| AC-006 | AC | high | Fail | 不存在／未上架／0 庫存不得借出，disabled 或 A0000 且狀態不變 | library.spec.ts:74-89、106-121；Full | real UI scenarios blocked at list load; no false Pass assigned. |
| AC-007 | AC | high | Fail | 已借出書籍歸還成功、數量增加，全部歸還後恢復可借 | library.spec.ts:49-62；Full | browser cannot load catalogue due CORS. |
| AC-008 | AC | high | Fail | 無借閱歸還回 A0000，數量與借閱狀態不變 | library.spec.ts:123-138；Full | browser cannot load catalogue due CORS. |
| AC-009 | AC | medium | Partial | 成功空列表回 00000，顯示尚無館藏及新增引導 | library.spec.ts:140-154；targeted 12 passed | 目前是 route mock evidence；真實 backend empty response 未能在 browser 驗證。 |
| AC-010 | AC | high | Partial | 系統錯誤回 B0000，原狀態不變並可 retry | library.spec.ts:156-185；targeted 12 passed | GET catalogue failure/retry mock 通過；新增／借出／歸還 mutation system failure 未完成 real integration 驗證。 |
| AC-UI-001 | AC | medium | Partial | Figma 主要布局、欄位、狀態、空／錯誤回饋與窄螢幕可用 | library.spec.ts:187-217；Figma export | 結構 smoke 通過，沒有 formal pixel-diff／差異核准，且 real catalogue integration blocked。 |

Status meaning: Pass 為可重現完整驗證；Partial 為只有部分 journey、mock 或 client-only evidence；Fail 為瀏覽器觀察結果違反需求或核心驗收未能成立；Blocked 為依賴 gate 使項目無法驗證。本報告使用 Fail 表示觀察到的產品行為（catalogue 顯示 B0000），並在 notes 標記其 CORS blocker。

## Open Issues

| Issue | Classification | Evidence | Owner/rework target | Severity/status |
| --- | --- | --- | --- | --- |
| DEF-001：frontend 由 5173 呼叫 backend 8080 時，browser CORS preflight 回 403，catalogue request 進入 B0000；所有 real API UI tests 在 book-table gate 失敗。 | Product | preflight curl OPTIONS 回 403 Invalid CORS request；Full／Stability 1-3 各 15 failures；test-results error-context.md 顯示 館藏載入失敗、錯誤碼 B0000 | FE／BE；依部署邊界採用核准的 backend CORS、frontend proxy 或 runtime API-base 配置，確保 browser 可讀取 API，再由 QA 重跑完整矩陣 | blocker / open |
| DATA-001：沒有 delete 或 test-reset API，interrupted run 後 synthetic books 會保留。 | Data isolation | library-data.ts 使用唯一 namespace 與 tracked active-loan return；本次沒有 secrets／personal data | BE／SD 若需長期 CI cleanup；目前以 namespace isolation 與 best-effort cleanup 降低風險 | low / mitigated, not blocking |

## Next Action

- FE／BE 先修正 DEF-001，並提供可由 browser 從 5173 讀取 8080 API 的可重現啟動方式；QA 隨後重跑 targeted、full、三次 stability 與 workers=2／1 diagnostic。
- 只有在 real API journey 通過後，才能重新判定 FR-001、FR-003、FR-004、NFR-001、NFR-005、AC-001、AC-003～AC-008 等核心項目；generated OpenAPI output 的 repository cleanliness 仍由 implementation owner 另行處理，不屬 QA defect。
- Workflow update: WF-LIB-001 維持 S6、狀態 blocked；已完成 QA-owned scenario E2E、preflight、targeted/full/stability/parallelism execution 與 evidence report，下一 session 應先讀本報告、library.spec.ts、library-page.ts、library-data.ts、REQ-LIB-001 及 PG delivery summary。
