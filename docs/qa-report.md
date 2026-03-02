# QA Report - REQ-LIB-001

- Date: 2026-03-02
- Scope: Verify web behavior against `docs/requirements/REQ-LIB-001.md`
- Verdict: **Not fully compliant**

## 1) Execution Evidence

### Frontend E2E stability (Playwright)
- Command: `npm run test:e2e` (apps/web/library-mini-admin-web)
- Run result:
  - Run #1: failed (3 failed, all timeout at `page.goto('/')`)
  - Run #2: failed (3 failed, all timeout at `page.goto('/')`)
  - Run #3: failed (3 failed, all timeout at `page.goto('/')`)
- Conclusion: does **not** meet NFR stability gate (3 consecutive green).

### Preflight + rerun evidence (services explicitly started)
- Preflight check:
  - Frontend: `vite` process listening on `5173`.
  - Backend: `spring-boot` process listening on `8080`.
- Then executed once: `npm run e2e` (repo root).
- Result: failed (3 failed), errors include:
  - `net::ERR_NETWORK_IO_SUSPENDED` at `page.goto('/')`
  - `Test timeout of 30000ms exceeded` at `page.goto('/')`
- Conclusion: Even with both services started, current E2E run is still unstable in this environment.

### Backend integration tests (reference only)
- Command: `npm run check:api`
- Result: passed (`Tests run: 8, Failures: 0, Errors: 0`)
- Note: This confirms backend API behavior, but does not prove frontend end-to-end user journey completeness.

## 2) Requirement Coverage Matrix (Web)

| Requirement | Status | QA Note |
| --- | --- | --- |
| FR-001 / AC-001 / AC-002 | Partial | UI has AddBookForm fields and submit. No FE E2E verifies success or duplicate ISBN flow end-to-end. |
| FR-002 / AC-003 / AC-004 / AC-005 | Partial | UI has borrow form + tab. No FE E2E verifies business-code scenarios through UI. |
| FR-003 / AC-006 / AC-008 / AC-009 | Partial | UI has return form + tab. No FE E2E verifies return success/failure scenarios through UI. |
| FR-004 / AC-007 | **Fail** | UI currently only renders code + text message, no overdueDays/fine amount display in web page. |
| FR-005 | Partial | BookTable columns and quick actions exist; quick action button disable logic is implemented. But no E2E verifies actual quick borrow/return flow outcomes. |
| FR-006 | Pass | TopBar contains search entry and trigger in UI. |
| FR-007 / AC-010 | Partial | Four required blocks exist (`TopBar`, `TransactionCard`, `AddBookForm`, `BookTable`). No formal diff list vs Figma file provided for mismatch approval workflow. |
| NFR-001 | Partial | Result panel exists, and store refreshes list after actions. No automated FE evidence for all success/error/warning paths. |
| NFR-002 | Partial | FE stores and shows business code in message panel; however E2E does not validate code mapping across scenarios. |
| NFR-003 | Partial | Table action disabling exists; required visual indicators (`*` etc.) not consistently enforced across forms. |
| NFR-004 | **Fail** | Current E2E only checks visibility of test IDs; does not cover add/borrow/return/overdue Given-When-Then journeys. |

## 3) Key Gaps

1. E2E stability is not acceptable yet.
- Evidence: repeated Playwright timeout at `page.goto('/')`.

2. Acceptance criteria are mostly not automated at web level.
- Current FE test only checks element existence, not business journeys.

3. Overdue return UI requirement is not met.
- REQ requires overdue days and fine shown in UI.
- Current message panel only displays code + text.

4. FE/BE local integration path is still fragile by configuration.
- FE client defaults to relative path if `VITE_API_BASE_URL` not set.
- Vite config has no `/library` proxy configured.
- This can break local web functional verification depending on startup mode.

## 4) File Evidence

- Requirement source: `docs/requirements/REQ-LIB-001.md`
- FE smoke E2E only: `apps/web/library-mini-admin-web/e2e/vue.spec.ts`
- Message panel (no overdue detail fields): `apps/web/library-mini-admin-web/src/components/MessagePanel.vue`
- FE business-code handling: `apps/web/library-mini-admin-web/src/store/libraryStore.ts`
- FE API client base-url behavior: `apps/web/library-mini-admin-web/src/api/client.ts`
- Vite config (no API proxy): `apps/web/library-mini-admin-web/vite.config.ts`
- Backend API integration tests: `apps/api/library-mini-admin-api/src/test/java/com/example/library/api/LibraryApiIntegrationTest.java`

## 5) QA Conclusion

Current web implementation cannot be marked as "fully matching REQ-LIB-001" yet.
It is structurally close on UI layout, but still missing robust and stable end-to-end evidence for AC-001~AC-009, and does not satisfy FR-004/AC-007 UI output requirement.
