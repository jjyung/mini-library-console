# Workflow State: WF-LIB-001

## 1. Metadata

- Workflow ID: WF-LIB-001
- Scenario ID: SCN-LIB-001
- Title: 小型圖書櫃管理 MVP
- Owner Role: orchestrator
- Current Stage: S6
- Overall Status: in_progress
- Priority: medium
- Created At: 2026-09-08
- Updated At: 2026-09-14
- Related Branch/Worktree: current worktree
- Related Files:
  - docs/scenarios/SCN-LIB-001.md
  - docs/requirements/REQ-LIB-001.md
  - docs/figma/library-mini-admin-console/README.md
  - docs/figma/library-mini-admin-console/guidelines/Guidelines.md
  - docs/architecture/ARCH-LIB-001.md
  - docs/openapi.yaml
  - docs/tasks/TASK-LIB-001_mvp-delivery.md
  - docs/tasks/TASK-LIB-001_mvp-delivery-summary.md
  - docs/traceability/FE-REQ-LIB-001.json
  - docs/qa-report/QA-LIB-001.md

## 2. Business Goal

- Why this workflow exists：將共享書櫃的新增、借閱與歸還流程轉成可追蹤、可接手的 scenario-driven delivery。
- Expected business/user outcome：管理員能建立館藏並可靠掌握誰借了書、目前可借數量與歸還後的狀態。
- In-scope：新增書籍、館藏列表、借出、歸還、數量／狀態更新及主要 UI 回饋。
- Out-of-scope：登入授權、書目編輯刪除、批次匯入、報表、通知、完整逾期催收與未確認的複本增補流程。

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

- Current Goal: 由 QA 依已完成的 PG handoff 驗證 TEST-only Library Mini Admin MVP 的完整前後端 journey、NFR 與 locator 契約。
- Why this is the next step: PG 已完成 FE／BE 實作、contract alignment、unit/integration checks 與 QA 測試資料 handoff；剩餘工作是實際瀏覽器驗證。
- Expected Output: docs/qa-report/QA-LIB-001.md
- Exit Criteria: QA 完成新增、借出、歸還、搜尋／分頁、錯誤、響應式、可存取性與 performance smoke，並依缺陷分類觸發有限 rework loop。

## 5. Stage Status

- S0 Scenario Discovery: done

  - Summary: 已讀取 README、AGENTS、scenario 與 Figma export README／Guidelines／核心元件。
  - Output Files: docs/scenarios/SCN-LIB-001.md；docs/figma/library-mini-admin-console/README.md
  - Open Questions: Figma guidelines 為預設模板，未提供額外品牌或元件規範。
- S1 SA: done

  - Summary: 已產出 FR、NFR、Given/When/Then AC、業務規則、狀態轉移、例外、錯誤碼、UI→API 候選與待確認事項。
  - Output Files: docs/requirements/REQ-LIB-001.md
  - Open Questions: Q-001 至 Q-007 需由後續角色或產品決策確認；Q-008 為目前安全假設。
- S2 Archi: done

  - Summary: 已依使用者確認選定 TEST profile、MVP 免登入、H2 2.3.232；完成 C4、部署拓撲、三層式架構、NFR 控制面與 SD handoff。
  - Output Files: docs/architecture/ARCH-LIB-001.md
  - Open Questions: Q-001 至 Q-004、Q-006、Q-007 仍由 SA／SD／PG 定案，但目前不阻擋 SD 開始；Q-004 需在 API freeze 前解決。
- S3 SD: done

  - Summary: 已產出並驗證 OpenAPI、global error codes、books／loans schema 與四份 API flow；library-books-001 已補充 1-based page-number pagination；TEST CORS shortcut 已記錄。contract freeze 前仍需確認 Q-001 至 Q-004。
  - Output Files: docs/openapi.yaml；docs/error-codes.md；docs/schema/books.md；docs/schema/loans.md；docs/api/library-books-001_list.md；docs/api/library-books-002_create.md；docs/api/library-books-003_borrow.md；docs/api/library-books-004_return.md
  - Open Questions: SD baseline 建議 author optional、readerId 字串、MVP 不計逾期罰款、return 使用 loanId；需由 SA／產品在 implementation freeze 前確認。
- S4 PG: done

  - Summary: 已完成 FE／BE ownership split、API contract freeze、分頁行為、locator contract、測試資料與 QA handoff；已產出 delivery plan 與 summary。
  - Output Files: docs/tasks/TASK-LIB-001_mvp-delivery.md；docs/tasks/TASK-LIB-001_mvp-delivery-summary.md
  - Open Questions: Q-001 至 Q-004 使用 SD baseline 實作，仍需產品確認；不阻擋本次 TEST MVP handoff。
- S5A FE: done

  - Summary: 已完成 Vue 3 Figma-aligned admin console、typed OpenAPI client、館藏搜尋／分頁、表單交易、錯誤回饋與穩定 locator；本次補完成 Figma light card layout 的 CSS rework 與 desktop／390px responsive visual smoke；unit/component coverage gate 通過。
  - Output Files: apps/web/library-mini-admin-web/src/App.vue；apps/web/library-mini-admin-web/src/core/；apps/web/library-mini-admin-web/src/features/；docs/traceability/FE-REQ-LIB-001.json
  - Open Questions: QA 需驗證 Chromium、窄螢幕與實際 API journey。
- S5B BE: done

  - Summary: 已完成 generated Spring boundary、H2/Liquibase schema、JDBC DAO、service transaction rules、business-code envelope、correlation ID、TEST localhost CORS allowlist、JsonNullable Jackson mapping 與 API integration tests。
  - Output Files: apps/api/library-mini-admin-api/pom.xml；apps/api/library-mini-admin-api/src/main/generated/；apps/api/library-mini-admin-api/src/main/java/；apps/api/library-mini-admin-api/src/main/resources/db/；apps/api/library-mini-admin-api/src/test/java/
  - Open Questions: generated source 尚未 staged/committed；QA 需驗證實際啟動、瀏覽器 preflight 與完整 journey。
- S6 QA: not_started

  - Summary: FE／BE 可執行且 locator、API、測試資料隔離契約已穩定；FE visual rework 已完成，等待 QA skill 執行實際 API journey、responsive 與 locator 驗證。
  - Output Files: docs/qa-report/QA-LIB-001.md
  - Open Questions: 需執行 QA Playwright journey、NFR checks 與差異清單。
- S7 Done: not_started

  - Summary: 尚未完成所有 stage 與 QA gate。

## 6. Dependency / Blocking Status

- Blocking Issues: 沒有產品程式實作 blocker；`npm run api:verify-generated` 在未 staged/committed 的 worktree 會因 generated output untracked 而 fail，需 commit 後重跑 source-control gate。
- Missing Decisions: Q-001 必填欄位、Q-002 reader identity、Q-003 逾期規則、Q-004 歸還定位、Q-005 持久化、Q-006 搜尋行為、Q-007 複本增補範圍。
- Waiting For: QA 執行 S6；產品後續確認 Q-001 至 Q-004 時需回送 SA／SD，不得把目前 baseline 擴大為正式產品決策。
- Safe Assumptions: TEST 是唯一 deployment profile；MVP 免登入且只允許 private TEST ingress；H2 2.3.232 作為 TEST embedded persistence；管理員流程先涵蓋單次單複本交易；所有 API 遵守 00000／A0000／B0000／C0000 業務碼契約；Figma export 作為 UI 視覺與互動語意基準，而非未確認業務規則的唯一來源。
- Risks: 以 ISBN 直接歸還可能無法定位多複本的特定借閱；前端 mock state 不足以支援共享資料；Figma 搜尋欄與 scenario 範圍尚未一致。

## 7. Parallel Work Plan

- FE can start when: S4 PG 完成切分，且 S3 SD 已凍結 OpenAPI、DTO、錯誤碼與 locator 契約；此條件已滿足。
- BE can start when: S4 PG 完成切分，且 S3 SD 已凍結 OpenAPI、schema、錯誤碼與交易邊界；此條件已滿足。
- Shared dependencies: REQ-LIB-001 的 AC、業務規則、狀態模型、API response envelope、錯誤碼與測試資料識別策略。
- Contract freeze point: S3 SD exit gate 通過並由 PG 在 TASK-LIB-001_mvp-delivery.md 記錄 freeze。
- Merge criteria: FE／BE 均通過各自 check；API contract 與 UI locator 不漂移；AC-001 至 AC-009 具備可驗證實作；交由 QA 前 workflow 與 handoff 文件更新完成。

## 8. Auto QA Loop

- QA Trigger Condition: FE／BE 實作完成、API contract freeze、前端 `data-testid` locator 穩定，且本地前後端可啟動；目前已達成。
- Latest QA Result: not_run; backend CORS and borrow JSON mapping rework locally verified; FE Figma visual rework locally verified
- Defects:

  - DEF-001:

    - Severity: medium
    - Owner: BE
    - Status: fixed_pending_qa
    - Fix Plan: 已新增明確 `http://localhost:5173,http://localhost:4173` allowlist、GET/POST/OPTIONS methods、Content-Type/X-Correlation-Id/Idempotency-Key headers；以 MockMvc 驗證 preflight、實際 GET 與拒絕未列來源。
  - DEF-002:

    - Severity: high
    - Owner: BE
    - Status: fixed_pending_qa
    - Fix Plan: 註冊 OpenAPI Generator 所需的 `JsonNullableModule`，並以 integration test 覆蓋含 author 與 dueDate 的新增／借出 journey，避免 request 在 controller 前轉換失敗並回傳 `B0000`。
  - DEF-003:

    - Severity: medium
    - Owner: FE
    - Status: fixed_pending_qa
    - Fix Plan: 將 `apps/web/library-mini-admin-web/src/App.vue` 的深色自訂 dashboard CSS 重整為 `docs/figma/library-mini-admin-console` 的 light card layout，補上桌面兩欄／館藏下排與窄螢幕堆疊規則；保留既有 locator 與交易狀態。
- Re-entry Rule:

  - implementation bug -> FE/BE
  - contract gap -> SD
  - design conflict -> Archi
  - requirement ambiguity -> SA

## 9. Session Handoff Notes

- Last completed action: FE 完成 Figma light card layout CSS rework，並以 Chromium desktop／390px viewport 確認表單、館藏空狀態、分頁與 responsive 排版；BE JsonNullable JSON mapping 及含 author／dueDate 的借書 integration test 仍已通過。
- Recommended next action: 請 QA 讀取 TASK-LIB-001_mvp-delivery.md、REQ-LIB-001、OpenAPI、locator contract，啟動前後端並執行 S6 QA Playwright journey，產出 docs/qa-report/QA-LIB-001.md。
- Files to read first: README.md；AGENTS.md；docs/workflows/WF-LIB-001.md；docs/tasks/TASK-LIB-001_mvp-delivery.md；docs/requirements/REQ-LIB-001.md；docs/architecture/ARCH-LIB-001.md；docs/openapi.yaml；docs/traceability/FE-REQ-LIB-001.json。
- Questions to resolve: QA 需驗證 Q-001 至 Q-004 baseline 是否可接受；Q-006、Q-007 仍列為後續產品決策，實作依 frozen SD baseline。
- Notes for next agent/session: `library-books-001` 使用 page=1、pageSize=20 API defaults，前端預設每頁 10 並提供 10／20／50 selector；API IDs 為 library-books-001 至 004；所有 API envelope 使用 business code；TEST CORS shortcut 只限 test，H2 只限 TEST MVP；不得將免登入或 embedded DB 推廣到 UAT／PROD。

## 10. Completion Checklist

- [x] Scenario exists and is valid
- [x] Requirements are complete
- [x] Architecture is complete
- [x] API / schema is complete
- [x] PG plan is complete
- [x] FE implementation is complete
- [x] BE implementation is complete
- [ ] QA verification is complete
- [x] Artifacts are consistent
- [x] Scope has not drifted
