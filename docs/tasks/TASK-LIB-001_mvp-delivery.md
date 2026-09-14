# TASK-LIB-001 MVP Delivery Plan

## 1. Metadata

- Task ID: TASK-LIB-001
- Workflow ID: WF-LIB-001
- Scenario ID: SCN-LIB-001
- Requirement ID: REQ-LIB-001
- Architecture: ARCH-LIB-001
- Status: ready_for_qa
- Owner: PG
- Updated At: 2026-09-14

## 2. Delivery Objective

Deliver the TEST-only Library Mini Admin MVP as a runnable Vue 3 + Spring Boot
application. The delivery covers book creation, paged catalogue search,
borrowing, returning, inventory/status invariants, business-code responses,
and the Figma-aligned admin UI.

The approved runtime assumptions are TEST-only, no login, private ingress,
H2 2.3.232 embedded file storage, Liquibase formatted SQL, and synchronous
three-layer `controller -> service -> dao` processing. No UAT/PROD, MQ, cache,
or third-party runtime integration is included.

## 3. Frozen Inputs

| Artifact | Version / decision | Use |
| --- | --- | --- |
| OpenAPI | `docs/openapi.yaml`, info version `0.1.0` | REST paths, DTOs, constraints, response envelopes |
| API IDs | `library-books-001` to `library-books-004` | Traceability and handoff |
| Backend generator | OpenAPI Generator Maven plugin `7.25.0` | Generated Spring API boundary |
| Frontend | Vue `3.5.28`, Vite `7.3.1`, TypeScript `5.9.3` | Existing application baseline |
| Persistence | H2 `2.3.232` + JDBC + Liquibase SQL | TEST persistence boundary |
| Pagination | 1-based `page`, default `1`; `pageSize`, default `20`, max `100` | `library-books-001` request/response behavior |
| Error contract | `00000`, `A0000`, `B0000`, `C0000` | Business result mapping; HTTP is not a replacement |

The SD baseline for unresolved Q-001 through Q-004 is implemented and kept
visible in this task: optional author, string readerId, no MVP fine
calculation, and return by unique loanId. Any product change must be routed
back to SA/SD and must not be silently implemented by FE or BE.

## 4. Scope and Traceability

| Requirement | Backend behavior | Frontend behavior | Evidence |
| --- | --- | --- | --- |
| FR-001 / AC-001..002 | Create book, validate fields, reject duplicate ISBN | Add-book form, validation, success/business error states | BE service/controller tests; FE unit/component tests |
| FR-002 / AC-003..005 | Atomic borrow, decrement available count, update status | Borrow tab, pending/disabled state, result mapping | BE transaction/API tests; FE tests |
| FR-003 / AC-006..008 | Atomic return by bookId + loanId, restore count/status | Return tab, result/error state | BE transaction/API tests; FE tests |
| FR-004 / AC-UI-001 | Paged search with active-loan summary | Figma-aligned table, search, empty state, pagination controls | `library-books-001`; locator and component evidence |
| FR-005 / AC-009 | Idempotency/conflict and business envelope | Preserve inputs, map business codes, retry/reload behavior | BE API tests; FE client/store tests |
| NFR-001..005 | Invariants, H2 consistency, trace/correlation, TEST config | Responsive/accessibility/loading/error states | BE integration checks; FE checks; QA E2E |

## 5. Ownership Split and Gates

### Gate-A — BE contract-critical implementation

BE owns `apps/api/**` and must:

- repair only the minimum Maven generation configuration needed to run the
  POM-defined generator;
- generate and retain the Spring API interface and DTOs under
  `src/main/generated` without manual edits;
- implement `controller`, `service`, and `dao` layers for all four API IDs;
- add request validation, business-code error envelopes, correlation IDs, and
  stable HTTP mappings from the SD flow;
- implement page-number pagination with deterministic ordering and active loan
  summary for `library-books-001`.

Exit evidence: generated sources compile, controller binding is covered,
invalid requests return business code `A0000`, and no controller contains
business decisions.

### Gate-B — BE persistence and integration readiness

BE must:

- add H2 2.3.232 and Liquibase dependencies/configuration;
- add immutable formatted-SQL changesets for `books` and `loans`;
- implement atomic borrow/return behavior and idempotency handling;
- add service, DAO/database, migration, and API integration tests;
- run `npm run api:generate`, `npm run api:verify-generated`,
  `npm run check:api`, `npm run db:validate`, and `npm run backend:check`.

Exit evidence: backend is runnable on port 8080, migration starts cleanly,
and API readiness notes include seed/reset behavior and test data isolation.

### Gate-C — FE application and verification

FE owns `apps/web/**` and must:

- keep Vue 3/Vite architecture and translate the requirement-scoped Figma
  export into Vue components;
- generate typed OpenAPI models into `src/core/api/generated` using pinned
  `openapi-typescript` `7.13.0` and call them only through a centralized
  client/service;
- implement search, page/pageSize controls, table, empty/loading/error/retry
  states, add-book form, borrow/return tabs, and API business-code mapping;
- preserve and freeze stable locators: `library-topbar-search`,
  `library-borrow-tab`, `library-return-tab`, `library-add-book-form`,
  `library-book-title-input`, `library-book-isbn-input`,
  `library-book-author-input`, `library-book-quantity-input`,
  `library-add-book-submit`, `library-borrow-submit`,
  `library-return-submit`, `library-book-table`, `library-book-row`,
  and row `data-book-id`; add pagination locators without renaming these;
- add a machine-readable FE traceability manifest and unit/component tests;
- run type-check, lint, build, API generation check, and the skill-owned
  requirement-test verifier.

Exit evidence: FE runs against the centralized API client, has no direct
component `fetch`/`axios`, and reports all required states and locators.

### Gate-D — PG integration and delivery handoff

PG owns the final gate and must:

- verify the OpenAPI, generated boundary, FE client, backend, and UI use the
  same DTO field names and business codes;
- run repository-level checks and record environment failures separately from
  product defects;
- hand the stable app, synthetic test data, locator map, and cross-screen
  journey to QA;
- update this task summary and `WF-LIB-001.md` with gate results.

## 6. QA Handoff Contract

QA journey: create a book with ISBN `978-0-13-235088-4`, borrow it with
synthetic reader `qa-reader-001`, verify available count/status, return the
specific loan, and verify the inventory returns to its original state.

Additional checks: catalogue search and `page=2&pageSize=1`, empty result,
invalid `page`/`pageSize`, duplicate ISBN, unavailable/inactive book, repeated
write request, network/system error mapping, responsive table, keyboard tab
navigation, and every frozen `data-testid` locator.

QA must own Playwright E2E changes and produce
`docs/qa-report/QA-LIB-001.md`; FE must provide API/mock expectations and must
not add or modify QA-owned E2E specs.

## 7. Progress and Blockers

| Item | Status | Notes |
| --- | --- | --- |
| Upstream requirements/architecture/SD read | done | S1/S2/S3 artifacts are available and SD validator passed |
| PG plan and FE/BE split | done | This document; design contract is immutable |
| Gate-A | done | Generated Spring boundary compiles; validation and business-code mapping covered |
| Gate-B | done | H2/Liquibase persistence, borrow/return integration tests and TEST CORS preflight checks pass |
| Gate-C | done | Vue UI, typed client, locator contract, unit/component tests and build pass |
| Gate-D | done | PG integration review complete; QA handoff is ready |
| FE visual rework | done | Re-aligned Vue global CSS to the requirement-scoped Figma light card layout and verified desktop/mobile rendering |

Resolved implementation blocker: `npm run api:generate` initially reached
OpenAPI Generator `7.25.0` but failed because the existing
`build-helper-maven-plugin` resource entry was not a Maven `Resource` object.
The POM now uses `resources/resource/directory`, generation succeeds, and the
generated Spring boundary is used by the implementation.

Known delivery-state limitation: the repository intentionally has not been
staged or committed in this session. Therefore `npm run api:verify-generated`
will report `src/main/generated` as untracked until the generated output is
committed; this is a source-control gate, not a generation or compilation
failure. The frontend OpenAPI type output is likewise generated under
`apps/web/library-mini-admin-web/src/core/api/generated`.

## 8. Handoff Notes

- PG has not changed requirements, architecture, OpenAPI, schema, or API flow
  documents in this execution.
- FE and BE implementation are complete within their assigned directories.
- FE completed a Figma visual rework in `apps/web/library-mini-admin-web/src/App.vue`:
  light surface tokens, card/form/table styling, desktop two-column layout,
  mobile stacking, and preserved pagination/error/loading behavior.
- Frontend requirement evidence is recorded in
  `docs/traceability/FE-REQ-LIB-001.json`; the skill verifier confirms all 16
  canonical FR/AC criteria have unit evidence and 97.81% line coverage.
- QA should execute the synthetic journey with ISBN `978-0-13-235088-4`,
  reader `qa-reader-001`, and the pagination/error/accessibility checks listed
  above. QA owns Playwright changes and the QA report.
- The next session should read this task, the delivery summary, and
  `WF-LIB-001.md`, then begin S6 QA verification.
- Any unresolved Q-001..Q-004 decision remains a product/SA/SD question;
  implementation uses the documented SD baseline only.
