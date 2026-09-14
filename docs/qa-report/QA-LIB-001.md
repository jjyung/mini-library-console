# QA Report: QA-LIB-001

## 1. 報告摘要

- Scenario：SCN-LIB-001
- Requirement：REQ-LIB-001
- Workflow：WF-LIB-001
- 執行日期：2026-09-14
- 分支／commit：`demo/20260908-qa` / `ac94fa8`
- QA 判定：Pass with limitations — S6 gate 可關閉，保留未執行的非阻塞性 audit 項目
- 本輪結果：AC-008 targeted test 1 passed；完整 Chromium 6 tests，6 passed；2 workers 下無跨測試資料污染

先前 `AC-008` 的多複本歸還失敗已由 BE 修正並驗證關閉：同一書目兩筆借閱皆建立後，
兩次歸還均成功，庫存依序回到 `1 / 2`、`2 / 2`，狀態最後為可借閱。核心
acceptance criteria 已通過；限制僅包含本輪未執行 pixel-diff、axe／contrast audit、
loading／submit-disabled 的獨立斷言與完整 server-log correlation。

## 2. 執行環境與 Preflight

- Node `v22.21.0`、npm `11.12.0`、Java `21.0.3`
- Playwright `1.58.2`、Vite `7.3.1`、Vue `3.5.29`
- Backend：`http://localhost:8080`，`/actuator/health` HTTP 200／`UP`
- Frontend：`http://localhost:5173`，HTTP 200（IPv6 localhost listener）
- 瀏覽器：Chromium Desktop；另於測試內使用 `390x844` 窄螢幕 viewport
- Firefox／WebKit：本輪未執行；NFR-005 依需求先完成 Chromium 桌面與窄螢幕 smoke
- Retry policy：指令明確使用 `--retries=0`；沒有以 retry-after-pass 代替穩定性證據

## 3. 測試架構與資料隔離

- Spec：`apps/web/library-mini-admin-web/e2e/vue.spec.ts`
- Page Object：`e2e/LibraryConsolePage.ts`
- Fixture／API setup：`e2e/fixtures.ts`、`e2e/data.ts`
- 所有 UI locator 使用既有 `data-testid`、label、role 或 row 的
  `data-book-id`，未修改產品 locator。
- 每個 test 以 timestamp、worker index、test title 產生唯一 namespace；ISBN、書名與
  reader ID 不共用。Playwright describe 設為 parallel，並以 2 workers 驗證隔離。
- OpenAPI 沒有 DELETE books／loans endpoint，因此未執行破壞性清理；測試資料以唯一
  namespace 建立並可查詢追蹤。歷史失敗的 AC-008 曾留下該測試專屬資料與未歸還 loan，
  但本輪 targeted 與完整 suite 均使用新的唯一 namespace，沒有觀察到跨測試資料污染，
  亦未使用真實個資或 secrets。

## 4. 執行證據

### 4.1 最終完整 suite

```text
PATH=/Users/cfh00902455/.nvm/versions/node/v22.21.0/bin:$PATH ./node_modules/.bin/playwright test e2e/vue.spec.ts --project=chromium --workers=1 --retries=0 --grep "changes one copy at a time"
Running 1 test using 1 worker
1 passed (28.3s)

PATH=/Users/cfh00902455/.nvm/versions/node/v22.21.0/bin:$PATH ./node_modules/.bin/playwright test e2e --project=chromium --workers=2 --retries=0
Running 6 tests using 2 workers
6 passed (1.1m)
```

Targeted test 驗證兩複本逐筆借出／歸還與 `1 / 2` → `2 / 2` 狀態；完整 suite
覆蓋 AC-001 至 AC-009、搜尋／分頁、keyboard／responsive、錯誤 mapping 與 performance
smoke。兩次執行均使用 Chromium、`--retries=0`，完整 suite 通過後未再重跑。

### 4.2 其他驗證

- Responsive／search／pagination／keyboard assertions：完整 suite 內通過。
- 2-worker isolation run：6 tests，6 passed；沒有跨測試資料污染。
- `npx oxlint .`：0 warnings、0 errors。
- `npx eslint . --no-cache`：通過。
- `npx playwright test --list`：列出 Chromium／Firefox／WebKit 各 6 tests；本輪只執行 Chromium。

## 5. Coverage Matrix

| Requirement / AC | 結果 | 證據／備註 |
| --- | --- | --- |
| FR-001 | Partial | AC-001 建立成功；AC-002 驗證重複 ISBN。未涵蓋所有欄位格式錯誤。 |
| FR-002 | Pass | AC-003 最後一複本、AC-004 兩複本借出、AC-005 未上架 disabled。 |
| FR-003 | Pass | AC-006 單複本歸還與 AC-008 多複本逐筆歸還通過。 |
| FR-004 | Partial | 搜尋、空結果、列表、分頁、狀態與錯誤讀取回饋已驗證；未單獨斷言 loading 文案。 |
| FR-005 | Partial | 成功／A0000／B0000 回饋已驗證；未對每個 submit button 的處理中 disabled 做獨立斷言。 |
| FR-UI-001 / AC-UI-001 | Partial | Figma 結構語意、locator、窄螢幕堆疊與 disabled smoke 通過；本輪未做 pixel-diff。 |
| AC-001 | Pass | 建立後呈現 `1 / 1`、可借閱與成功回饋。 |
| AC-002 | Pass | 重複 ISBN 顯示錯誤，既有列與數量維持不變。 |
| AC-003 | Pass | 借出最後一複本後為 `0 / 1`、借閱中、借出 disabled。 |
| AC-004 | Pass | 兩複本各借出一次，數量依序 `1 / 2`、`0 / 2`。 |
| AC-005 | Pass | 未上架書籍的借出按鈕 disabled，資料未變更。 |
| AC-006 | Pass | 單複本歸還後恢復 `1 / 1`、可借閱、無借閱歸還 disabled。 |
| AC-007 | Pass | 無效 loan 顯示 A0000 對應訊息，庫存維持不變。 |
| AC-008 | Pass | targeted 與完整 suite 均通過；兩複本借出後逐筆歸還，庫存依序為 `1 / 2`、`2 / 2`。 |
| AC-009 | Pass | duplicate／invalid return 的 A0000 與 mock B0000 均映射為可理解訊息，不暴露原始碼。 |
| NFR-001 | Pass | 單複本與多複本借閱／歸還不變量均通過。 |
| NFR-002 | Partial | label、keyboard tab、Enter、disabled semantic 通過；未執行 axe／contrast audit。 |
| NFR-003 | Pass | acceptance journey 的新增／借出／歸還 performance assertions 通過；完整 suite 6/6。 |
| NFR-004 | Partial | UI 驗證 A0000／B0000 mapping；未在本輪檢查完整 server log correlation。 |
| NFR-005 | Pass | Chromium desktop、390px viewport、堆疊順序、無 root horizontal overflow、pagination 通過。 |

## 6. Open Issues

| ID | 分類 | Severity | Owner | 狀態 | 證據與 rework target |
| --- | --- | --- | --- | --- | --- |
| DEF-QA-001 | Product／BE | High | BE | Verified／Closed | 根因為既有 `CK_BOOKS_STATUS_MAPPING` 在仍有 active loan 時拒絕 `BORROWED` 狀態；BE 以 `books-002` Liquibase changeset 修正，並由 targeted AC-008、完整 Chromium 6/6 與 backend integration coverage 驗證。 |

後續成功 rerun 已清除歷史 failure artifact；本輪 6/6 通過，因此沒有新的 failure
artifact。缺陷根因另由 BE red integration test 的 `CK_BOOKS_STATUS_MAPPING` violation
與 `books-002` migration／AC-008 integration coverage 佐證。

## 7. QA Decision 與後續

- Decision：Pass with limitations；DEF-QA-001 已 Verified／Closed，S6 QA gate 可關閉。
- 本輪已確認兩複本每次只增加一個可借數、每次只結束一筆 loan，第二次完成後為
  `2 / 2`／可借閱；AC-001 至 AC-009 皆通過。
- 保留限制：未執行 pixel-diff、axe／contrast audit、loading／submit-disabled 獨立斷言、
  完整 server-log correlation；這些不阻擋本次核心 scenario gate。
- 後續由 orchestrator 判斷 S7 Done；若產品確認 Q-001 至 Q-004 導致契約改動，才需回
  SA／SD 重新檢查，現行 QA 不開新 defect。
