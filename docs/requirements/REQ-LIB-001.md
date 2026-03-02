# REQ-LIB-001 需求分析（SA）

## 文件資訊

- 來源情境：SCN-LIB-001
- UI 設計來源：`docs/figma-make/library-mini-admin-console/`

## 1) 需求摘要

- 情境目標：提供「小型共享書櫃」最小可用管理流程，解決書籍去向不明、庫存不可視、長期借閱難追蹤問題。
- 使用者角色：管理員（Admin）。
- 核心流程：
  - 建立書籍（新增館藏）
  - 借書（登記借閱、扣減可借數）
  - 還書（完成歸還、回補可借數）
- 主要畫面區塊（依 Figma Make）：
  - TopBar（含搜尋輸入）
  - TransactionCard（借書/還書頁籤）
  - AddBookForm（新增書籍表單）
  - BookTable（館藏清單與快速借出/歸還）

## 2) Functional Requirements (FR)

### FR-001 新增書籍

- 系統需提供新增書籍功能，欄位至少包含：書名、ISBN、作者（選填）、分類、數量、上架狀態。
- 書名與 ISBN 為必填；數量需為正整數。
- 若 ISBN 已存在，系統需拒絕新增並回傳業務錯誤碼。
- 新增成功後，館藏清單需立即可見該書籍。
- 若上架狀態為「上架」，初始狀態為「可借閱」；若為「未上架」，初始狀態為「未上架」。

### FR-002 借書

- 系統需提供借書功能，欄位至少包含：讀者 ID、ISBN、到期日（選填）。
- 當 ISBN 不存在、書籍可借數為 0、或狀態為未上架時，系統需拒絕借閱並回傳業務錯誤碼。
- 借閱成功後，系統需更新：
  - 可借數量 -1
  - 借閱狀態（可借數為 0 時顯示為已借出）
  - 借閱人與到期日資訊
- 若未輸入到期日，系統需自動帶入預設借期（目前 UI 行為為 14 天）。

### FR-003 還書

- 系統需提供還書功能，欄位至少包含：ISBN、讀者 ID（選填）。
- 當 ISBN 不存在或該書目前無借出紀錄時，系統需拒絕還書並回傳業務錯誤碼。
- 還書成功後，系統需更新：
  - 可借數量 +1
  - 狀態（回到全數可借時顯示為可借閱）
  - 清除借閱人與到期日資訊

### FR-004 逾期提示與罰款資訊

- 還書時系統需依到期日判斷是否逾期。
- 逾期時需回傳逾期天數與罰款金額供 UI 顯示（目前 UI 行為為每日 NT$5）。
- 逾期屬「交易完成但需提示」情境，需以可辨識狀態回傳（非 HTTP 狀態判斷）。

### FR-005 館藏清單展示

- 系統需提供館藏列表，至少顯示：書名、ISBN、作者、分類、狀態、可借數/總數。
- 系統需提供每筆資料之快速操作：借出、歸還。
- 快速借出/歸還按鈕之可用性需與業務狀態一致（不可借時禁用借出、無借出紀錄時禁用歸還）。

### FR-006 搜尋（對齊 UI 區塊）

- TopBar 需提供「搜尋書名、ISBN、作者」之入口。
- 本情境僅確認「有搜尋入口」需求；搜尋條件、排序、分頁與查詢範圍細節列為待確認。

### FR-007 前端畫面對齊 Figma Make

- 前端實作必須以 `docs/figma-make/library-mini-admin-console/` 為唯一畫面參考來源。
- FE 需對齊以下畫面區塊與操作流：`TopBar`、`TransactionCard`、`AddBookForm`、`BookTable`。
- 若實作與 Figma Make 畫面不一致，需先提出差異清單並經需求確認後才能偏離。

## 3) Non-Functional Requirements (NFR)

### NFR-001 一致性與即時回饋

- 成功或失敗操作後，前端需在同一操作流程中顯示明確結果（成功/失敗/警示）。
- 清單顯示狀態需與最新交易結果一致，不可出現可借數與狀態不一致。

### NFR-002 業務錯誤碼治理

- 所有交易結果需包含業務錯誤碼，不可僅依 HTTP status 判斷。
- 錯誤碼需符合專案規範：`00000` 成功、`Axxxx` 客戶端業務錯誤、`Bxxxx` 系統錯誤、`Cxxxx` 第三方錯誤。

### NFR-003 可用性

- 必填欄位需可明確辨識。
- 不可執行操作需在 UI 明確禁用，避免無效送出。

### NFR-004 可測試性

- 每個核心流程（新增、借書、還書、逾期歸還）需可透過 Given/When/Then 驗證。
- UI 文案與狀態標示需穩定，以支持 QA 驗收腳本。

## 4) Acceptance Criteria (AC, Given/When/Then)

### AC-001 新增書籍成功

- Given 管理員在新增書籍表單輸入合法書名、唯一 ISBN、合法數量
- When 點擊「新增書籍」
- Then 系統建立書籍成功，館藏列表出現新資料，且可借數/總數等於輸入數量

### AC-002 新增書籍失敗（ISBN 重複）

- Given 系統已存在某 ISBN
- When 管理員以相同 ISBN 再次新增
- Then 系統拒絕新增並回傳 `A0002`（ISBN 重複）

### AC-003 借書成功（有庫存）

- Given 館藏存在指定 ISBN，且狀態為可借閱、可借數 > 0
- When 管理員輸入讀者 ID 與 ISBN 並送出借書
- Then 系統完成借閱，該書可借數減 1，並記錄借閱人與到期日

### AC-004 借書失敗（庫存不足）

- Given 館藏存在指定 ISBN，但可借數為 0
- When 管理員送出借書
- Then 系統拒絕借閱並回傳 `A0004`（庫存不足）

### AC-005 借書失敗（未上架）

- Given 館藏存在指定 ISBN，且狀態為未上架
- When 管理員送出借書
- Then 系統拒絕借閱並回傳 `A0005`（書籍未上架）

### AC-006 還書成功（未逾期）

- Given 指定 ISBN 有借出紀錄，且未逾期
- When 管理員送出還書
- Then 系統完成歸還，可借數加 1，清除借閱資訊，並回傳成功碼 `00000`

### AC-007 還書成功（逾期）

- Given 指定 ISBN 有借出紀錄，且已逾期
- When 管理員送出還書
- Then 系統完成歸還，並回傳逾期天數與罰款資訊（含業務碼 `A0007` 供警示）

### AC-008 還書失敗（無借出紀錄）

- Given 指定 ISBN 目前可借數等於總數（無借出）
- When 管理員送出還書
- Then 系統拒絕處理並回傳 `A0006`（無借出紀錄）

### AC-009 還書或借書失敗（ISBN 不存在）

- Given 系統查無指定 ISBN
- When 管理員送出借書或還書
- Then 系統回傳 `A0001`（書籍不存在）

### AC-010 前端畫面對齊 Figma Make

- Given FE 依本需求文件進行前端實作
- When 檢查 `TopBar`、`TransactionCard`、`AddBookForm`、`BookTable` 之版面與操作
- Then 前端畫面需與 `docs/figma-make/library-mini-admin-console/` 一致，若有差異需附差異清單並經確認

## 錯誤碼（業務層）

| Code | Type | 說明 |
| --- | --- | --- |
| 00000 | Success | 成功 |
| A0001 | Client Error | 書籍不存在（ISBN 查無資料） |
| A0002 | Client Error | ISBN 重複，禁止新增 |
| A0003 | Client Error | 必填欄位缺漏或格式不符 |
| A0004 | Client Error | 庫存不足，無法借閱 |
| A0005 | Client Error | 書籍未上架，無法借閱 |
| A0006 | Client Error | 無借出紀錄，無法歸還 |
| A0007 | Client Error | 逾期歸還（交易成功但需警示） |
| B0000 | System Error | 系統內部錯誤 |
| C0000 | Third-party Error | 第三方服務錯誤 |

## UI → API 對照表

| UI 區塊/動作 | API ID | 建議路徑（operation path） | 成功碼 | 主要錯誤碼 |
| --- | --- | --- | --- | --- |
| AddBookForm - 新增書籍 | library-books-001 | `POST /library/books` | 00000 | A0002, A0003 |
| TransactionCard - 借書 | library-loans-001 | `POST /library/loans` | 00000 | A0001, A0004, A0005 |
| TransactionCard - 還書 | library-loans-002 | `POST /library/loans/returns` | 00000 | A0001, A0006, A0007 |
| BookTable - 館藏列表 | library-books-002 | `GET /library/books` | 00000 | B0000 |
| TopBar - 搜尋（書名/ISBN/作者） | library-books-003 | `GET /library/books/search` | 00000 | A0003 |
| BookTable - 快速借出 | library-loans-001 | `POST /library/loans` | 00000 | A0001, A0004, A0005 |
| BookTable - 快速歸還 | library-loans-002 | `POST /library/loans/returns` | 00000 | A0001, A0006, A0007 |

## 5) 風險與待確認事項

- 逾期是否應視為「成功+警示」或「失敗」：目前 UI 顯示偏向成功但提醒，需 PO/QA 最終確認。
- 罰款規則是否固定為每日 NT$5：目前為畫面行為，需業務規範正式化。
- 借閱人欄位命名：UI 使用「讀者 ID」，情境文字為「借閱人姓名」，需統一識別規則。
- 借期預設值（14 天）是否固定：目前為前端行為，需確認是否可配置。
- 搜尋需求僅見 UI 入口，尚缺排序、分頁、模糊比對、大小寫/全半形等規則。
- 多副本借閱追蹤粒度：目前行為為 ISBN 層級彙總，是否需到單冊（copy）層級待確認。
- 同一本書多人同時借閱/歸還的競態處理規範未定義，可能造成可借數不一致風險。
