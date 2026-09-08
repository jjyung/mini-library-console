# Workflow State: WF-LIB-001

## 1. Metadata

- Workflow ID: WF-LIB-001
- Scenario ID: SCN-LIB-001
- Title: 小型圖書櫃管理 MVP
- Owner Role: orchestrator
- Current Stage: S2
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
- S2 Archi: in_progress

  - Summary: 等待架構角色接手並定義可支援借閱一致性的 MVP 邊界。
  - Output Files: 尚未產出
  - Open Questions: 請優先處理 Q-002、Q-004、Q-005，並說明 Q-001、Q-003、Q-006 的架構影響。
- S3 SD: not_started

  - Summary: 等待 ARCH-LIB-001。
  - Output Files: docs/openapi.yaml、docs/error-codes.md、docs/schema/、docs/api/
  - Open Questions: 待架構與需求決策。
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

- Blocking Issues: 目前沒有阻擋 Archi 開始的問題；REQ 已將未決產品選擇標示為 Open。
- Missing Decisions: Q-001 必填欄位、Q-002 reader identity、Q-003 逾期規則、Q-004 歸還定位、Q-005 持久化、Q-006 搜尋行為、Q-007 複本增補範圍。
- Waiting For: Archi 產出 ARCH-LIB-001；必要時由產品／SA 確認 REQ open questions。
- Safe Assumptions: 管理員已完成授權；MVP 先涵蓋單次單複本交易；所有 API 遵守 00000／A0000／B0000／C0000 業務碼契約；Figma export 作為 UI 視覺與互動語意基準，而非未確認業務規則的唯一來源。
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

- Last completed action: SA 完成 REQ-LIB-001，並以 `python3 .codex/skills/scenario-requirements-writer/scripts/validate_requirements.py docs/requirements/REQ-LIB-001.md` 驗證。
- Recommended next action: 請 Archi 讀取 REQ-LIB-001 與 Figma export，產出 ARCH-LIB-001；優先處理借閱一致性、借閱人識別及持久化邊界。
- Files to read first: README.md；AGENTS.md；docs/scenarios/SCN-LIB-001.md；docs/requirements/REQ-LIB-001.md；docs/figma/library-mini-admin-console/README.md；docs/figma/library-mini-admin-console/src/app/App.tsx；docs/figma/library-mini-admin-console/src/app/components/TransactionCard.tsx；docs/figma/library-mini-admin-console/src/app/components/BookTable.tsx。
- Questions to resolve: Q-001 至 Q-007；其中 Q-002 與 Q-004 會直接影響 API identity，Q-005 會影響架構與資料一致性。
- Notes for next agent/session: 需求文件中的 API ID 只是業務能力候選，不得直接當 operationId；任何 API 仍須遵守 AGENTS.md 的業務錯誤碼與 DTO 命名規則。Figma export 的 `src/styles/fonts.css` 缺失是匯出備註，不影響本階段需求分析。

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
