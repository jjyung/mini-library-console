# Workflow State: WF-LIB-001

## 1. Metadata

- Workflow ID: WF-LIB-001
- Scenario ID: SCN-LIB-001
- Title: 小型圖書櫃管理
- Owner Role: orchestrator
- Current Stage: S6
- Overall Status: blocked
- Priority: medium
- Created At: 2026-09-04
- Updated At: 2026-09-07
- Related Branch/Worktree: 目前工作區，未指定獨立分支
- Related Files:
  - `README.md`
  - `AGENTS.md`
  - `docs/scenarios/SCN-LIB-001.md`
  - `docs/figma/library-mini-admin-console/README.md`
  - `docs/figma/library-mini-admin-console/guidelines/Guidelines.md`
  - `docs/figma/library-mini-admin-console/src/app/App.tsx`
  - `docs/figma/library-mini-admin-console/src/app/components/TopBar.tsx`
  - `docs/figma/library-mini-admin-console/src/app/components/TransactionCard.tsx`
  - `docs/figma/library-mini-admin-console/src/app/components/AddBookForm.tsx`
  - `docs/figma/library-mini-admin-console/src/app/components/BookTable.tsx`
  - `docs/requirements/REQ-LIB-001.md`
  - `docs/architecture/ARCH-LIB-001.md`（S2 Ready for SD；test-only）
  - `docs/openapi.yaml`（S3 SD 已產出並通過 strict validator）
  - `docs/error-codes.md`（S3 SD 已產出）
  - `docs/schema/books.md`（S3 SD 已產出）
  - `docs/schema/loans.md`（S3 SD 已產出）
  - `docs/api/`（S3 SD 已產出四份 API flow）
  - `docs/tasks/TASK-LIB-001_mvp-delivery.md`（S4 PG task plan；Gate-A～C 完成）
  - `docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`（PG delivery summary）
  - `docs/traceability/FE-REQ-LIB-001.json`（FE requirement-test manifest）
  - `apps/api/library-mini-admin-api/src/main/generated/`（OpenAPI generated output）
  - `apps/api/library-mini-admin-api/src/main/java/com/example/library/`（BE implementation）
  - `apps/web/library-mini-admin-web/src/`（FE implementation）
  - `apps/web/library-mini-admin-web/e2e/library.spec.ts`（QA-owned SCN-LIB-001 journey）
  - `apps/web/library-mini-admin-web/e2e/fixtures.ts`、`library-page.ts`、`library-data.ts`（QA-owned E2E support）
  - `docs/qa-report/QA-LIB-001.md`（S6 QA report）

## 2. Business Goal

- Why this workflow exists：將 SCN-LIB-001 的共享書櫃管理流程以階段門檻推進，讓需求、架構、介面設計、實作與 QA 有可接手且可追溯的文件狀態。
- Expected business/user outcome：管理員能建立館藏、看見書籍目前可借狀態、記錄借出對象並在歸還後恢復可借數量。
- In-scope：新增書籍與初始數量、館藏狀態呈現、借出、歸還、成功／失敗回饋，以及前端對齊指定 Figma 畫面。
- Out-of-scope：登入／權限、預約、通知、罰款政策、搜尋行為、編輯／刪除與未經確認的獨立增加 copies 操作。

## 3. Workflow Graph

```text
S0 Scenario Discovery
  ↓
S1 SA
  ↓
S2 Archi
  ↓
S3 SD
  ↓
S4 PG
  ├─→ S5A FE
  └─→ S5B BE
       ↓
      S6 QA
       ↓
      S7 Done
```

## 4. Current Objective

- Current Goal: QA 依已完成的環境化 CORS runtime policy 重跑完整 SCN-LIB-001 UI journey、主要錯誤與必要 NFR。
- Why this is the next step：BE 已依 SD 的 `x-environment-cors-policy` 完成 dev／poc／test bypass；test profile integration test 與 `APP_ENV=dev` browser-origin OPTIONS probe 均已通過，現在需要 QA 重新驗證完整旅程。
- Expected Output：QA 重跑 `docs/qa-report/QA-LIB-001.md` 中的 commands 並更新結果；generated output cleanliness 仍由 implementation owner 處理。
- Exit Criteria：DEF-001 修正後，QA 以 synthetic data 通過新增→列表→借出→最後一本狀態→歸還→恢復可借的 scenario journey，確認 `data-testid`、business code 與 scope；generated output cleanliness 也完成後才可進 S7。

## 5. Stage Status

- S0 Scenario Discovery: done

  - Summary：已讀取 README、AGENTS、SCN-LIB-001、Figma README、Figma Guidelines 及情境相關匯出元件；確認 scenario、README scope 與 Figma snapshot 的差異。
  - Output Files：輸入檢視完成；沒有在 S0 修改文件。
  - Open Questions：Figma 額外欄位、讀者識別、到期／罰款、搜尋及多副本歸還規則需後續確認。
- S1 SA: done

  - Summary：已完成繁體中文 REQ，包含 FR、NFR、Given／When／Then AC、業務規則、狀態轉移、例外邊界、錯誤碼、UI → API 候選、Figma 對齊條款及風險／待確認事項。
  - Output Files：`docs/requirements/REQ-LIB-001.md`
  - Open Questions：REQ 的 Q-001 至 Q-008 為未決業務／流程問題；Q-009 為目前 Guidelines 僅有模板註解的假設。
- S2 Archi: done

  - Summary：已產出 test-only architecture，選定同步三層式單體方向、relational persistence、無 MQ／cache 的 MVP baseline；使用者已確認 test、免登入／預設 Admin、不加密，且不要求 HA／SLO／RTO／RPO／backup／DR／on-call。
  - Output Files：`docs/architecture/ARCH-LIB-001.md`
  - Open Questions：架構 gate 已解除；REQ Q-001、Q-002、Q-003、Q-005、Q-008 仍由 SA／SD 在 contract freeze 前處理。
- S3 SD: done

  - Summary：已依 REQ、ARCH 與 test-only 邊界產出 OpenAPI、全域錯誤碼、兩張 schema 與四份 API flow；四個 API 均有唯一 API ID、Request/Response DTO、成功與錯誤 business code mapping。2026-09-07 補充 `x-environment-cors-policy`，定義 dev／poc／test bypass、OPTIONS preflight 與 staging／production allowlist guardrail。
  - Output Files：`docs/openapi.yaml`、`docs/error-codes.md`、`docs/schema/books.md`、`docs/schema/loans.md`、`docs/api/library-books-001_get-books.md`、`docs/api/library-books-002_create-book.md`、`docs/api/library-loans-001_borrow-loan.md`、`docs/api/library-loans-002_return-loan.md`
  - Validation：`node .codex/skills/sd-docs-producer/scripts/validate_sd_artifacts.js --project-root . --requirement docs/requirements/REQ-LIB-001.md --architecture docs/architecture/ARCH-LIB-001.md --strict` → 4 API operation(s), 0 error, 0 warning；`git diff --check` passed。
  - Open Questions：REQ Q-001、Q-002、Q-005、Q-008 尚未回寫需求文件；SD 以 bounded contract assumptions 定稿，PG 必須在 task 與 contract freeze review 時確認，不得自行擴大 scope。
- S4 PG: done

  - Summary：已完成 contract freeze review、FE／BE ownership split、Gate-A～D 規則、locator 契約與 QA handoff；依 SD bounded assumptions 執行 test MVP，未修改上游需求／架構／SD artifacts。
  - Output Files：`docs/tasks/TASK-LIB-001_mvp-delivery.md`、`docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`
  - Validation：Gate-A、Gate-B、Gate-C complete；API smoke journey passed；Gate-D 交 QA 進行 S6。
  - Open Questions：正式 QA 仍需確認 QA-owned E2E 已改為 SCN-LIB-001 journey；generated output commit 後需重跑 strict cleanliness。
- S5A FE: done

  - Summary：已完成 Figma-aligned Vue page、borrow／return／create／catalogue states、centralized typed API client、stable `data-testid` 與 FE requirement traceability。
  - Output Files：`apps/web/library-mini-admin-web/src/**`、`docs/traceability/FE-REQ-LIB-001.json`
  - Validation：unit 26 pass、component 17 pass、requirement verifier 17/17 pass、coverage branch 92.3%、lint/type-check/build pass。
  - Open Questions：QA scenario journey 已完成；正式驗證需以明確的 lower-environment 設定重跑。Figma difference list remains documented in FE handoff/task scope。
- S5B BE: done

  - Summary：已完成 generated OpenAPI boundary、controller-service-dao flow、H2＋Liquibase persistence、business code mapping 與 atomic borrow／return。
  - Output Files：`apps/api/library-mini-admin-api/src/main/generated/**`、`apps/api/library-mini-admin-api/src/main/java/com/example/library/**`、`apps/api/library-mini-admin-api/src/main/resources/db/**`、`apps/api/library-mini-admin-api/src/test/**`
  - Validation：backend check 15 tests pass、DB 2 changesets pass、API generation pass、manual create→list→borrow→return smoke pass；test profile preflight test 與 `APP_ENV=dev` browser-origin OPTIONS probe pass。
  - Open Questions：generated directory is currently modified by regeneration and must be committed before strict generated cleanliness can pass；不得手改 generated files。
- S6 QA: blocked

  - Summary：QA 已以 synthetic data 完成 scenario E2E、preflight、targeted、full、三次 stability 與 parallelism diagnostic；mocked UI tests 通過，但既有 real API UI journeys 因舊版 frontend 5173 呼叫 backend 8080 的 CORS preflight 403 而失敗。BE runtime policy 現已完成並通過 test profile 與 `APP_ENV=dev` probe，QA 尚未重跑完整矩陣。
  - Output Files：`docs/qa-report/QA-LIB-001.md`、`apps/web/library-mini-admin-web/e2e/library.spec.ts`、`fixtures.ts`、`library-page.ts`、`library-data.ts`
  - Validation：Full 27 tests 使用 3 workers，12 passed、15 failed；Stability 1～3 均為 12 passed、15 failed；workers=2 與 workers=1 diagnostic 均為 5 failed。失敗一致在 `book-table` catalogue load gate，頁面顯示 `B0000`。
  - Open Questions：QA 必須以 `APP_ENV=dev`、`APP_ENV=poc` 或 test profile 重跑完整矩陣，確認 DEF-001 已解除。generated output commit／strict cleanliness 仍待 implementation owner。
- S7 Done: not_started

  - Summary：待 QA 通過、所有 artifact 一致且未有未核准 scope drift 後標記完成。

## 6. Dependency / Blocking Status

- Blocking Issues：QA 尚未以修正後 runtime policy 重跑完整矩陣；另 `api:verify-generated` 需在 generated output commit 後才可通過。既有 QA report 中的 DEF-001 403 證據保留作為修正前基線。
- Missing Decisions：REQ Q-001、Q-002、Q-005、Q-008 尚未由 SA／業務正式回寫；本次 PG 依 SD bounded assumptions 執行，若業務否決需回送 SA／SD。
- Waiting For：QA 以明確 lower-environment 設定重跑並更新報告；implementation owner commit generated OpenAPI output 後重跑 strict cleanliness。
- Safe Assumptions：核心流程為新增、列表、借出、歸還；本次實際環境為 local + test，test 免登入、預設 Admin、不加密、無 HA／SLO／RTO／RPO／backup／DR／on-call；書籍以 ISBN 唯一識別；借閱使用 synthetic `readerId`；到期日只保存、不計算罰款；同 ISBN 多筆 active loan 時，歸還需提供 `readerId`；Figma URL 與本地匯出元件是 UI 對齊來源；Figma 四筆資料是展示 mock；搜尋、罰款、獨立增加 copies 暫不納入；local／test 僅使用 synthetic data。
- Risks：多副本與多管理員同時異動可能造成借閱誤配或數量競爭；scenario 與 Figma 欄位差異可能造成 FE／BE 返工；視覺偏離若未形成差異清單，QA 無法判斷是否可接受。

## 7. Parallel Work Plan

- FE can start when：已滿足；S4 task plan、S3 contract、Figma UI scope、field rules 與 `data-testid` contract 均已確認，S5A 已完成。
- BE can start when：已滿足；S4 task plan、S3 OpenAPI／error code／schema／flow 與 `readerId`／ISBN／transaction assumptions 均已確認，S5B 已完成。
- Shared dependencies：REQ-LIB-001 的 FR／NFR／AC、業務錯誤碼全域規則、書籍／副本／借閱／狀態的共享術語、Figma 指定 UI 區塊、測試資料與 scenario scope。
- Contract freeze point：S3 SD 已完成並通過 SD 驗證；正式 freeze 由 S4 PG 在確認 bounded assumptions 與未決事項責任後宣告。四個 API ID、path 與 DTO 已可作為實作輸入，但若 SA／業務否決 assumptions，需回送 SD 更新契約。
- Merge criteria：FE／BE 已符合 REQ 的 AC 與錯誤碼規則，locator review、SD strict validation、unit/component/backend checks 與 API smoke 已通過；仍待 QA 使用隔離資料重現完整 UI journey。

## 8. Auto QA Loop

- QA Trigger Condition：已滿足；S5A／S5B 完成、SD contract frozen、服務可啟動，PG 已交付 QA。
- Latest QA Result：blocked
- Defects：

  - DEF-001：修正前 frontend 5173 → backend 8080 的 browser CORS preflight 回 403，catalogue request 進入 `B0000`，Full／Stability 1～3 每次均有 15 個 real API UI tests 失敗；BE 已依核准邊界完成 runtime CORS 修正，QA 待重跑確認。
  - DATA-001：沒有 delete 或 test-reset API；QA 已用唯一 namespace 與 tracked active-loan best-effort return 降低 interrupted-run 影響，屬 mitigated data-isolation caveat，非本次 blocker。

- Re-entry Rule：

  - implementation bug → FE／BE
  - contract gap → SD
  - design conflict → Archi
  - requirement ambiguity → SA

- Loop Limit：同一類缺陷重複失敗時不得無限重跑；連續重現後需在 workflow blocker 中升級給對應角色與人員確認。

## 9. Session Handoff Notes

- Last completed action：BE 已依 `x-environment-cors-policy` 完成 runtime CORS policy；`npm run backend:check` 15 tests pass，test profile preflight integration test 與 `APP_ENV=dev` browser-origin OPTIONS probe 均回 HTTP 200；task/summary 已更新 handoff。
- Recommended next action: QA 以 `APP_ENV=dev`、`APP_ENV=poc` 或 test profile 重跑 report commands，確認 real journey、錯誤碼、state consistency 與 NFR；implementation owner 同步 commit generated output 後重跑 `api:verify-generated`，完成後才評估 S7。
- Files to read first: `README.md`、`AGENTS.md`、`docs/scenarios/SCN-LIB-001.md`、`docs/requirements/REQ-LIB-001.md`、`docs/architecture/ARCH-LIB-001.md`、`docs/openapi.yaml`、`docs/error-codes.md`、`docs/schema/`、`docs/api/`、`docs/tasks/TASK-LIB-001_mvp-delivery.md`、`docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`、`docs/traceability/FE-REQ-LIB-001.json`、`docs/qa-report/QA-LIB-001.md`、`apps/web/library-mini-admin-web/e2e/library.spec.ts`、`fixtures.ts`、`library-page.ts`、`library-data.ts`。
- Questions to resolve：QA 是否已在 browser 端重現 DEF-001 修正；generated output 是否已被 commit。CORS bypass 僅適用明確的 dev／poc／test lower-tier environment，不得延伸到 staging／PROD；也不得把 test-only 免登入／不加密決策延伸到未來 PROD，或自行加入搜尋、罰款、獨立增加 copies。
- Notes for next agent/session：REQ 與 ARCH 都遵守 SA／Archi 邊界；SD 已明確定義四個 API、兩張資料表、同步交易邊界、business code mapping 與 `x-environment-cors-policy`。低階環境的 preflight 是 transport handshake，不產生 business code；真正進入 API operation 的 response 仍遵守 `00000`／`A0000`／`B0000`／`C0000`。ARCH 的 PostgreSQL 16、p95 500ms、20 concurrent admins 與 10,000 rows 是 preliminary assumptions，不是已核准 NFR；test 的免登入、不加密、無 HA／backup／DR／on-call 是已確認的範圍限制。Figma Guidelines 目前沒有額外內容；任何 UI 偏離必須留下差異清單。

## 10. Completion Checklist

- [x] Scenario exists and is valid
- [x] Requirements are complete
- [x] Architecture is complete for test scope
- [x] API / schema is complete
- [x] PG plan is complete
- [x] FE implementation is complete
- [x] BE implementation is complete
- [ ] QA verification is complete
- [x] Artifacts are consistent through implementation handoff; QA report records the pre-fix S6 blocker and BE remediation evidence
- [x] Scope has not drifted
