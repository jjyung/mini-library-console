# Workflow State: WF-LIB-001

## 1. Metadata

- Workflow ID: `WF-LIB-001`
- Scenario ID: `SCN-LIB-001`
- Title: 小型圖書櫃管理
- Owner Role: orchestrator
- Current Stage: `S4`
- Overall Status: `in_progress`
- Priority: medium
- Created At: 2026-08-31
- Updated At: 2026-09-01
- Related Branch/Worktree: 未指定
- Related Files:
  - `README.md`
  - `AGENTS.md`
  - `docs/scenarios/SCN-LIB-001.md`
  - `docs/figma/library-mini-admin-console/README.md`
  - `docs/figma/library-mini-admin-console/guidelines/Guidelines.md`
  - `docs/requirements/REQ-LIB-001.md`
  - `docs/architecture/ARCH-LIB-001.md`
  - `docs/openapi.yaml`
  - `docs/error-codes.md`
  - `docs/schema/books.md`
  - `docs/schema/loans.md`
  - `docs/api/library-books-001_create-book.md`
  - `docs/api/library-books-002_list-books.md`
  - `docs/api/library-loans-001_checkout-book.md`
  - `docs/api/library-loans-002_return-book.md`
  - `docs/tasks/TASK-LIB-001_mvp-delivery.md`
  - `docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`
  - `docs/qa-report/QA-LIB-001.md`（待建立）

## 2. Business Goal

- 讓共享書櫃管理者能建立書籍、查看館藏狀態、記錄借出與歸還。
- 透過可借數量與狀態更新，讓管理者知道書籍是否仍在館及目前是否可借。
- In-scope：建立書籍、館藏列表、借書、還書、業務結果回饋、Figma UI 對齊。
- Out-of-scope：登入／權限、預約、罰款制度、通知、搜尋、未確認的借閱歷史查詢。

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

- Current Goal: PG 整合 FE/BE MVP 產出並完成 Gate-A～D 驗證。
- Why this is the next step: task plan 與 implementation 已產出；目前只剩 Java/JAVA_HOME 與 Playwright browser 環境阻塞。
- Expected Output: `docs/tasks/TASK-LIB-001_mvp-delivery.md`、`docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`
- Exit Criteria: `check:api`、`check:web`、E2E 與 root check 完成，且可交 QA。

## 5. Stage Status

- S0 Scenario Discovery: done
  - Summary: 已確認 `SCN-LIB-001`、Figma export README／Guidelines 與相關 React export 可讀取。
  - Output Files: `docs/scenarios/SCN-LIB-001.md`
  - Open Questions: Figma Guidelines 為空白範本；無額外設計規則。
- S1 SA: done
  - Summary: 已產出 FR、NFR、Given/When/Then AC、業務錯誤碼與 UI→API traceability；已標註 scenario／UI 差異與待確認事項。
  - Output Files: `docs/requirements/REQ-LIB-001.md`
  - Open Questions: 借閱人姓名／讀者 ID、多副本借閱紀錄、逾期／罰款、搜尋、持久性、viewport／locator 尚待確認。
- S2 Archi: done
  - Summary: 已完成單體 Web MVP 的 C4-L1/L2、部署拓撲、本地運行假設、NFR 策略、成本／複雜度取捨與擴展觸發條件；未定稿 API、DTO 或 DB schema。
  - Output Files: `docs/architecture/ARCH-LIB-001.md`
  - Open Questions: Q-001～Q-005 仍須在 SD contract freeze 前確認，Q-006～Q-007 由 FE／QA 凍結。
- S3 SD: done
  - Summary: 已產出 OpenAPI single source、全域 business code、books／loans schema 與四份 API flow；以 readerId、可選 dueDate、無逾期／罰款計算作為待確認前的明確假設。
  - Output Files: `docs/openapi.yaml`, `docs/error-codes.md`, `docs/schema/books.md`, `docs/schema/loans.md`, `docs/api/*`
  - Open Questions: Q-001 借閱人語意與還書比對、Q-002 多副本歸還策略、Q-003 dueDate／罰款、Q-005 持久化仍須產品／PG 凍結；Q-004 搜尋維持 out-of-scope。
- S4 PG: blocked
  - Summary: 已完成 task breakdown、FE/BE ownership、stable locator contract、implementation integration 與 Gate status；FE static checks/build 通過，但 API check 與 E2E 被本地環境阻塞。
  - Output Files: `docs/tasks/TASK-LIB-001_mvp-delivery.md`, `docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`
  - Open Questions: Q-001、Q-002、Q-003、Q-005 仍依既定 MVP assumptions；ENV-001、ENV-002 待解除。
- S5A FE: not_started
  - Summary:
  - Output Files:
  - Open Questions:
- S5B BE: not_started
  - Summary:
  - Output Files:
  - Open Questions:
- S6 QA: not_started
  - Summary:
  - Output Files:
  - Open Questions:
- S7 Done: not_started
  - Summary:

## 6. Dependency / Blocking Status

- Blocking Issues: `npm run check:api` 缺少 Java/JAVA_HOME；`npm run e2e` 缺少 Playwright browsers。產品 Q-001～Q-003、Q-005 仍未正式凍結，但已依文件化 assumptions 實作。
- Missing Decisions: 借閱人欄位語意、借閱紀錄與多副本對應、到期日／罰款、搜尋是否另立需求、跨 session 保存。
- Waiting For: 開發環境安裝 JDK 21 與 Playwright browsers；產品／業務確認上述決策。
- Safe Assumptions: 以 Figma README 的 URL 與本地 export 作 UI source；不把 mock 初始資料當固定驗收資料；搜尋維持 out-of-scope。
- Risks: 需求情境與 UI 欄位不一致；export 沒有獨立借閱紀錄清單；export 未提供 `data-testid`。

## 7. Parallel Work Plan

- FE can start when: PG 完成任務切分，且 SD 已凍結 API contract、錯誤碼與 UI locator 要求。
- BE can start when: PG 完成任務切分，且 SD 已凍結 API contract、借閱狀態規則與錯誤碼。
- Shared dependencies: `REQ-LIB-001`、ARCH／SD artifacts、共用業務詞彙、AC、Figma UI source。
- Contract freeze point: PG 確認 Q-001～Q-005 的採用決策，SD artifacts 經 PG review 後凍結；任何 contract 變更回 SD。
- Merge criteria: FE／BE 皆符合 REQ AC、業務錯誤碼、Figma 對齊與 `data-testid` 穩定性，再交 QA。

## 8. Auto QA Loop

- QA Trigger Condition: Gate-B、Gate-C、Gate-D 通過，且 API、Web、E2E 可執行。
- Latest QA Result: blocked_before_qa（API Java runtime 與 Playwright browsers 尚未就緒）
- Defects:
  - DEF-001:
    - Severity: 未發現
    - Owner: SA
    - Status: not_created
    - Fix Plan: 若 QA 發現需求歧義，回 SA；若為 UI／實作／contract 問題，依 AGENTS.md 回 FE／BE／SD。
- Re-entry Rule:
  - implementation bug -> FE/BE
  - contract gap -> SD
  - design conflict -> Archi
  - requirement ambiguity -> SA

## 9. Session Handoff Notes

- Last completed action: PG 已整合 FE/BE implementation，完成 task plan／delivery summary；FE development API base URL 已設定為 `http://localhost:8080`，後端已加入全域 CORS 與 preflight test；`check:web`、frontend build 與 Vite runtime check 通過，API/E2E blockers 仍已記錄。
- Recommended next action: 設定 JDK 21/JAVA_HOME 後重跑 `npm run check:api`；安裝 Playwright browsers 後重跑 `npm run e2e`，再執行 root `npm run check`。
- Files to read first:
  - `README.md`
  - `AGENTS.md`
  - `docs/scenarios/SCN-LIB-001.md`
  - `docs/requirements/REQ-LIB-001.md`
  - `docs/architecture/ARCH-LIB-001.md`
  - `docs/openapi.yaml`
  - `docs/error-codes.md`
  - `docs/schema/books.md`
  - `docs/schema/loans.md`
  - `docs/figma/library-mini-admin-console/README.md`
  - `docs/tasks/TASK-LIB-001_mvp-delivery.md`
  - `docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`
- Questions to resolve: `REQ-LIB-001.md` Q-001～Q-007；至少先完成 Q-001～Q-005 才能凍結 SD contract。
- Notes for next agent/session: implementation 已存在於 `apps/api/**` 與 `apps/web/**`；development API URL 在 `apps/web/library-mini-admin-web/.env.development`；CORS config 在 `apps/api/**/config/WebConfig.java`。不要改動工作區原有 `.codex/agents/*` 變更。解除 ENV-001/002 後先驗證，再交 QA。

## 10. Completion Checklist

- [x] Scenario exists and is valid
- [x] Requirements are complete
- [x] Architecture is complete
- [x] API / schema is complete
- [x] PG plan is complete
- [x] FE implementation is complete
- [x] BE implementation is complete
- [ ] QA verification is complete
- [x] Artifacts are consistent for SA handoff
- [x] Scope has not drifted
