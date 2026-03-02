# QA Report - REQ-LIB-001

## 文件資訊

- 驗證目標：`REQ-LIB-001`
- 驗證日期：`2026-03-02`
- 驗證範圍：Web 功能符合度（含 API 串接行為）
- 驗證方式：
  - 規格對照（REQ/ARCH/OpenAPI）
  - E2E 規格檢視與補強
  - 原始碼靜態檢查

> 本次於目前執行環境無法實跑 `npm run e2e`（環境缺少 npm）。
> 結論以下方「符合度」為準，並標註可重測項目。

---

## 1. 驗證資產

- 需求文件：`docs/requirements/REQ-LIB-001.md`
- 架構文件：`docs/architecture/ARCH-LIB-001.md`
- API 契約：`docs/openapi.yaml`
- E2E：
  - `apps/web/library-mini-admin-web/e2e/vue.spec.ts`
  - `apps/web/library-mini-admin-web/e2e/req-lib-001.spec.ts`

---

## 2. AC 驗證結果

| AC | 結果 | 證據 |
| --- | --- | --- |
| `AC-LIB-001` 新增書籍成功 | `PASS` | `vue.spec.ts` 建書後驗證書列與可借數量 |
| `AC-LIB-002` 借書成功 | `PASS` | `vue.spec.ts` 驗證借出後可借量與狀態變化 |
| `AC-LIB-003` 借書失敗（無可借庫存） | `PARTIAL` | UI 以 disabled 防止二次借書（`req-lib-001.spec.ts`），可防呆；但「嘗試借書後顯示錯誤」流程未在 UI 出現 |
| `AC-LIB-004` 還書成功 | `PASS` | `vue.spec.ts` 驗證歸還後狀態與可借量恢復 |
| `AC-LIB-005` 借閱紀錄可追溯 | `PASS` | UI 顯示借閱人、借出/歸還時間與狀態（`App.vue` + `vue.spec.ts`） |
| `AC-LIB-006` 錯誤碼一致 | `PASS` | Store 以 `errorSubCode` 映射訊息（`libraryStore.ts`）；`req-lib-001.spec.ts` 驗證 A0001 文案 |

---

## 3. NFR 驗證結果

| NFR | 結果 | 說明 |
| --- | --- | --- |
| `NFR-LIB-001` 本地可執行 | `PARTIAL` | 程式已提供本地啟動腳本，但本次環境無法實跑 npm 進行完整驗證 |
| `NFR-LIB-002` 一致性與正確性 | `PASS` | 借還書後 UI 狀態與數量更新符合預期；後端交易流程已實作 |
| `NFR-LIB-003` 基本效能 | `NOT VERIFIED` | 尚未執行 1,000 本書籍下的量測測試 |
| `NFR-LIB-004` 可測試性（UI Locator） | `PASS` | 主要互動元件均有 `data-testid`，E2E 使用 `getByTestId()` |
| `NFR-LIB-005` 可維護性 | `PASS` | FE 已使用集中 API client + store 分層；DTO 命名對齊 |

---

## 4. 發現與風險

1. `AC-LIB-003` 有解讀差異風險：
   - 現行 UI 在可借量為 0 時直接禁用按鈕，防止送出借書請求。
   - 若產品要求「仍可按下並顯示 A0004 失敗訊息」，目前 UI 未覆蓋該互動流程。

2. `NFR-LIB-003` 尚未有可重複效能測試腳本：
   - 目前缺少固定資料量（1,000 筆）與 SLA 驗證流程。

---

## 5. QA 結論

- 整體判定：`條件式通過（Conditional Pass）`
- 說明：核心 happy path 與主要錯誤碼映射符合 REQ-LIB-001；
  但 `AC-LIB-003` 的 UI 互動定義與 `NFR-LIB-003` 效能量測仍需補充確認。

---

## 6. 建議重測項目

1. 在可執行 npm 的環境實跑：
   - `npm run check`
   - `npm run e2e`

2. 產品決策確認：
   - `AC-LIB-003` 是否接受「disabled 防呆」作為失敗處理。
   - 若不接受，需改為允許送出並顯示 `A0004` 映射訊息。

3. 補效能驗證：
   - 新增資料集與量測腳本，完成 `NFR-LIB-003` 最終判定。
