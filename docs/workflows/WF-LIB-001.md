# Workflow State: WF-LIB-001

## 1. Metadata

- Workflow ID: WF-LIB-001
- Scenario ID: SCN-LIB-001
- Title: 小型圖書櫃管理 MVP
- Owner Role: orchestrator
- Current Stage: S3
- Overall Status: in_progress
- Priority: medium
- Created At: 2026-09-08
- Updated At: 2026-09-08
- Related Branch/Worktree: current worktree
- Related Files:
  - docs/scenarios/SCN-LIB-001.md
  - docs/requirements/REQ-LIB-001.md
  - docs/figma/library-mini-admin-console/README.md
  - docs/figma/library-mini-admin-console/guidelines/Guidelines.md
  - docs/architecture/ARCH-LIB-001.md
  - docs/openapi.yaml
  - docs/tasks/TASK-LIB-001_mvp-delivery.md
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

- Current Goal: 由 Archi 依 REQ-LIB-001 定義 MVP 架構、NFR trade-off 與 SD handoff 邊界。
- Why this is the next step: SA 已完成 scenario／Figma 分析，需求文件已通過本地 validator，可供架構設計使用。
- Expected Output: docs/architecture/ARCH-LIB-001.md
- Exit Criteria: 架構文件涵蓋元件、資料流、部署、交易一致性、錯誤處理、觀測性、風險與 FE／BE／SD 邊界，並回應 REQ 的待確認事項。

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
- S3 SD: in_progress

  - Summary: 架構已 Ready for SD；SD 需將需求與架構邊界落成 OpenAPI、error codes、schema 與 API flow artifacts。
  - Output Files: docs/openapi.yaml、docs/error-codes.md、docs/schema/、docs/api/
  - Open Questions: Q-001 欄位必填性、Q-002 reader identity、Q-003 due date／fine、Q-004 唯一 loan identity、Q-006 search 行為。
- S4 PG: not_started

  - Summary: 等待需求、架構與 SD 契約穩定後規劃 FE／BE 切分。
  - Output Files: docs/tasks/TASK-LIB-001_mvp-delivery.md
  - Open Questions: 待 API contract freeze。
- S5A FE: not_started

  - Summary: 需依 PG plan 與 frozen OpenAPI 實作 Figma 對齊 UI。
  - Output Files: apps/web/*
  - Open Questions: 需凍結 data-testid locator 契約。
- S5B BE: not_started

  - Summary: 需依 PG plan 與 frozen OpenAPI 實作書目／借閱交易。
  - Output Files: apps/api/*
  - Open Questions: 需由 SD 定義 DTO、error envelope 與 transaction boundary。
- S6 QA: not_started

  - Summary: 待 FE／BE 可執行且 locator、API、測試資料隔離契約穩定後驗證。
  - Output Files: docs/qa-report/QA-LIB-001.md
  - Open Questions: 待實作與環境就緒。
- S7 Done: not_started

  - Summary: 尚未完成所有 stage 與 QA gate。

## 6. Dependency / Blocking Status

- Blocking Issues: 目前沒有阻擋 SD 開始的問題；Q-001 至 Q-004 若在 SD contract freeze 前仍未決，需回送 SA／產品，不得由 SD 靜默猜測。
- Missing Decisions: Q-001 必填欄位、Q-002 reader identity、Q-003 逾期規則、Q-004 歸還定位、Q-005 持久化、Q-006 搜尋行為、Q-007 複本增補範圍。
- Waiting For: SD 讀取 ARCH-LIB-001 並產出 implementation-ready artifacts；必要時回送 Q-001 至 Q-004 給 SA／產品決策。
- Safe Assumptions: TEST 是唯一 deployment profile；MVP 免登入且只允許 private TEST ingress；H2 2.3.232 作為 TEST embedded persistence；管理員流程先涵蓋單次單複本交易；所有 API 遵守 00000／A0000／B0000／C0000 業務碼契約；Figma export 作為 UI 視覺與互動語意基準，而非未確認業務規則的唯一來源。
- Risks: 以 ISBN 直接歸還可能無法定位多複本的特定借閱；前端 mock state 不足以支援共享資料；Figma 搜尋欄與 scenario 範圍尚未一致。

## 7. Parallel Work Plan

- FE can start when: S4 PG 完成切分，且 S3 SD 已凍結 OpenAPI、DTO、錯誤碼與 locator 契約；可先做不依賴 API 的純視覺骨架，但不得宣稱可交付。
- BE can start when: S4 PG 完成切分，且 S3 SD 已凍結 OpenAPI、schema、錯誤碼與交易邊界。
- Shared dependencies: REQ-LIB-001 的 AC、業務規則、狀態模型、API response envelope、錯誤碼與測試資料識別策略。
- Contract freeze point: S3 SD exit gate 通過並由 PG 在 TASK-LIB-001_mvp-delivery.md 記錄 freeze。
- Merge criteria: FE／BE 均通過各自 check；API contract 與 UI locator 不漂移；AC-001 至 AC-009 具備可驗證實作；交由 QA 前 workflow 與 handoff 文件更新完成。

## 8. Auto QA Loop

- QA Trigger Condition: FE／BE 實作完成、API contract freeze、前端 `data-testid` locator 穩定，且本地前後端可啟動。
- Latest QA Result: not_run
- Defects:

  - DEF-001:

    - Severity: 尚未發現
    - Owner: QA
    - Status: not_started
    - Fix Plan: QA 執行後依缺陷分類回送 FE、BE、SD、Archi 或 SA。
- Re-entry Rule:

  - implementation bug -> FE/BE
  - contract gap -> SD
  - design conflict -> Archi
  - requirement ambiguity -> SA

## 9. Session Handoff Notes

- Last completed action: Archi 產出 ARCH-LIB-001，完成使用者確認的 TEST／免登入／H2 決策、架構控制面與 SD handoff。
- Recommended next action: 請 SD 讀取 REQ-LIB-001 與 ARCH-LIB-001，產出 docs/openapi.yaml、docs/error-codes.md、docs/schema/ 與 docs/api/，並在 contract freeze 前處理 Q-001 至 Q-004。
- Files to read first: README.md；AGENTS.md；docs/scenarios/SCN-LIB-001.md；docs/requirements/REQ-LIB-001.md；docs/architecture/ARCH-LIB-001.md；docs/figma/library-mini-admin-console/README.md；docs/figma/library-mini-admin-console/src/app/App.tsx；docs/figma/library-mini-admin-console/src/app/components/TransactionCard.tsx；docs/figma/library-mini-admin-console/src/app/components/BookTable.tsx。
- Questions to resolve: Q-001 至 Q-004、Q-006、Q-007；其中 Q-002 與 Q-004 會直接影響 API identity，Q-003 影響是否擴大資料與驗收範圍。
- Notes for next agent/session: ARCH-LIB-001 已選定三層式同步架構、TEST 單實例、H2 embedded、無 MQ／無 cache；不得將免登入決策推廣到 UAT／PROD。API ID 仍由 SD 依 AGENTS.md 定稿，不得直接當 operationId。

## 10. Completion Checklist

- [x] Scenario exists and is valid
- [x] Requirements are complete
- [ ] Architecture is complete
- [ ] API / schema is complete
- [ ] PG plan is complete
- [ ] FE implementation is complete
- [ ] BE implementation is complete
- [ ] QA verification is complete
- [ ] Artifacts are consistent
- [x] Scope has not drifted
