# TASK-LIB-001 MVP Delivery Plan

## 1. Task metadata

- Task ID: TASK-LIB-001
- Workflow: WF-LIB-001
- Scenario: SCN-LIB-001
- Requirement: REQ-LIB-001
- Architecture: ARCH-LIB-001
- Status: qa_pending
- Owner: PG
- Scope: test-only local/test delivery; no production readiness claim
- Contract version: `docs/openapi.yaml` `info.version: 1.0.0`
- Created: 2026-09-04
- Updated: 2026-09-07

## 2. Delivery objective

Deliver the frozen library mini admin flow end-to-end:

1. list the current catalogue;
2. create a book with initial copies;
3. borrow one copy with a synthetic `readerId`;
4. return one active loan, requiring `readerId` when the ISBN is ambiguous;
5. keep book counts, derived status, loan history, and UI feedback consistent.

The implementation is limited to `apps/api/**` and `apps/web/**`. Coordination
artifacts are maintained under `docs/tasks/**`, `docs/traceability/**`, and
`docs/workflows/WF-LIB-001.md`.

## 3. Frozen scope and non-goals

### In scope

- `library-books-001`: `GET /api/books`
- `library-books-002`: `POST /api/books`
- `library-loans-001`: `POST /api/loans/borrow`
- `library-loans-002`: `POST /api/loans/return`
- `00000`, `A0000`, and `B0000` response-code mapping with `traceId`
- environment-scoped backend CORS policy: `dev`/`poc`/`test` bypass CORS and
  allow browser `OPTIONS` preflight; staging/production require an explicit
  allowlist
- relational persistence for `books` and `loans`, with an atomic borrow/return
  update
- Vue UI aligned to the local Figma export for `TopBar`, `TransactionCard`,
  `AddBookForm`, and `BookTable`
- stable `data-testid` locators for acceptance-critical controls and states
- synthetic test data only; default `Admin`, no login, no encryption

### Explicitly out of scope

- authentication, authorization, RBAC, SSO, MFA, or user lookup
- production security, encryption, HA, SLO/SLA, RTO/RPO, backup/DR, or on-call
- search behavior, pagination, editing, deleting, or a separate add-copies API
- fines, overdue calculation, notifications, third-party services, and MQ
- Figma mock rows as mandatory seed data

## 4. Contract freeze decision

PG adopts the bounded assumptions already used by the current SD artifacts for
this test MVP without modifying upstream requirement or architecture files:

- ISBN is the unique book identifier and is used by borrow/return requests.
- `readerId` is a synthetic value; it is not an authenticated identity.
- `readerId` is required for borrow and optional for return when one active loan
  can be selected; a return with multiple matching active loans and no reader
  selector returns `A0000`.
- `dueDate` is optional and persisted only; there is no fine or overdue policy.
- `isActive` defaults to `true`; status is derived as `INACTIVE`, `BORROWED`,
  or `AVAILABLE`.
- local/test are isolated, no-login environments entered as default `Admin`.
- Backend CORS is controlled by the `x-environment-cors-policy` extension in
  `docs/openapi.yaml`. In explicit `dev`, `poc`, or `test`, the backend must not
  reject cross-origin requests or `OPTIONS` preflight, must not enable
  credentials, and must not emit a business code for the transport handshake.
  Staging/production must use explicit origins and preflight policy.

If a business owner rejects one of these assumptions, this task must re-enter
SA/SD; neither FE nor BE may silently change the contract.

## 5. FE/BE ownership split

### BE — `apps/api/**`

| ID | Assignment | Depends on | Gate evidence |
| --- | --- | --- | --- |
| BE-001 | Run the POM-configured OpenAPI generator; keep generated API interfaces and DTOs tracked and unedited. | Frozen OpenAPI | Gate-A: generator and compile output |
| BE-002 | Establish test-safe relational persistence and Liquibase formatted SQL for `books` and `loans`, matching schema docs and rollback policy. | ARCH-LIB-001, books/loans schema | Gate-B: DB validation and migration-sensitive tests |
| BE-003 | Implement controller adapters for all four generated operations, request validation, default Admin/no-auth scope, and response envelopes. | BE-001, API flows | Gate-A: contract binding tests |
| BE-004 | Implement `controller -> service -> dao` behavior for list/create/borrow/return, including derived status, uniqueness, active-loan selection, and atomic count/loan updates. | BE-002, API flows | Gate-B: service/HTTP integration tests |
| BE-005 | Map all failures to business codes and `traceId`; HTTP status must not replace body `code`. | docs/error-codes.md | Gate-A/B: error mapping assertions |
| BE-006 | Provide BE handoff with API IDs, generator version, persistence/migration evidence, test data/reset notes, and known limitations. | BE-001..005 | Gate-B handoff note |
| BE-007 | Apply the environment-scoped CORS runtime policy; lower environments bypass origin validation and return a successful `OPTIONS` preflight without credentials, while production-like environments require explicit configuration. | `x-environment-cors-policy` in `docs/openapi.yaml` | Gate-A: runtime/preflight check; Gate-B: browser connectivity evidence |

### FE — `apps/web/**`

| ID | Assignment | Depends on | Gate evidence |
| --- | --- | --- | --- |
| FE-001 | Implement Figma-aligned page layout and responsive behavior using the existing Vue 3/Vite baseline. | REQ UI, Figma export | Gate-C: build and locator review |
| FE-002 | Add a centralized typed API client/service; all UI calls go through it and use the frozen paths/DTO fields. | OpenAPI 1.0.0, BE contract | Gate-C: client/contract tests |
| FE-003 | Implement list/create/borrow/return state transitions, loading, empty, validation, business error, system error, recovery, and immediate refresh. | FE-002 | Gate-C: unit/component tests |
| FE-004 | Preserve/add stable locators and accessible names for all acceptance-critical controls and states. | REQ AC/UI | Gate-C: locator inventory and tests |
| FE-005 | Create the requirement-to-test manifest and cover every canonical `FR-*`/`AC-*` heading with unit evidence; enforce 80% coverage. | REQ-LIB-001, FE test harness | Gate-C: skill verifier |
| FE-006 | Provide FE handoff with interaction matrix, Figma difference list, API/error mapping, locator inventory, and skipped-check rationale. | FE-001..005 | Gate-C handoff note |

### PG — coordination and integration

| ID | Assignment | Exit evidence |
| --- | --- | --- |
| PG-001 | Freeze scope, dependencies, shared terminology, and API/error contract. | This plan and workflow update |
| PG-002 | Sequence Gate-A/B before full integration; FE may use the frozen contract in parallel after Gate-A contract readiness. | Gate log |
| PG-003 | Run Gate-D full checks, inspect scope/data-testid/contract drift, and prepare QA handoff. | Delivery summary and workflow update |

## 6. API contract matrix

| API ID | Method/path | operationId | Request | Success | Client/system errors |
| --- | --- | --- | --- | --- | --- |
| `library-books-001` | GET `/api/books` | `getBooks` | `GetBooksRequestDTO` (empty) | HTTP 200, `GetBooksResponseDTO`, `00000` | HTTP 500, `ErrorResponseDTO`, `B0000` |
| `library-books-002` | POST `/api/books` | `postBooks` | `PostBooksRequestDTO` | HTTP 201, `PostBooksResponseDTO`, `00000` | HTTP 400 `A0000`; HTTP 500 `B0000` |
| `library-loans-001` | POST `/api/loans/borrow` | `postLoansBorrow` | `PostLoansBorrowRequestDTO` | HTTP 200, `PostLoansBorrowResponseDTO`, `00000` | HTTP 400 `A0000`; HTTP 500 `B0000` |
| `library-loans-002` | POST `/api/loans/return` | `postLoansReturn` | `PostLoansReturnRequestDTO` | HTTP 200, `PostLoansReturnResponseDTO`, `00000` | HTTP 400 `A0000`; HTTP 500 `B0000` |

Shared constraints: generated Request/Response DTO names are authoritative;
`BookDTO.status` is derived; `availableCount` must remain within
`0..totalCount`; active loans use `returnedAt == null`; every response includes
`code`, `message`, and `traceId`.

## 7. FE interaction and locator contract

The Figma export is the visual baseline. No existing Vue locator contract was
present at task start, so FE must add the following stable IDs and keep them
unchanged through QA:

| Area | Required `data-testid` values |
| --- | --- |
| App/status | `library-admin-page`, `catalogue-loading`, `catalogue-error`, `catalogue-empty`, `catalogue-success-toast`, `catalogue-error-toast` |
| TopBar | `topbar`, `topbar-search`, `admin-identity` |
| Transaction tabs | `transaction-card`, `borrow-tab`, `return-tab` |
| Borrow | `borrow-form`, `borrow-reader-id`, `borrow-isbn`, `borrow-due-date`, `borrow-submit`, `borrow-status` |
| Return | `return-form`, `return-isbn`, `return-reader-id`, `return-submit`, `return-status` |
| Create | `add-book-form`, `add-book-title`, `add-book-isbn`, `add-book-author`, `add-book-category`, `add-book-quantity`, `add-book-active`, `add-book-submit`, `add-book-status` |
| Catalogue | `book-table`, `book-row-{isbn}`, `book-status-{isbn}`, `book-available-{isbn}`, `quick-borrow-{isbn}`, `quick-return-{isbn}` |

ISBN values in locator suffixes must be deterministic test data in QA. FE must
use accessible labels/roles in addition to these IDs. Search remains visual
only and must not add a query parameter or new API.

## 8. Gate plan and handoff rules

### Gate-A — BE contract-critical readiness

- OpenAPI generator runs from the POM profile `api-generation`, plugin
  `7.25.0`, with generated sources under the tracked `src/main/generated`.
- Generated interfaces/DTOs compile and controller adapters implement the four
  operations without hand-written duplicate public DTOs.
- Request validation and `A0000`/`B0000`/`traceId` response mapping are covered.
- Lower-environment CORS mode is selected from runtime configuration; explicit
  `dev`/`poc`/`test` `OPTIONS` preflight does not return 403.
- No requirement or architecture document changes; SD policy changes must be
  reflected in the current frozen handoff before implementation.

### Gate-B — BE integration readiness

- Liquibase formatted SQL is present and validates; migrations match schema docs
  and include immutable changesets with rollback notes.
- Service/DAO tests prove list/create/borrow/return, status derivation,
  duplicate ISBN, no-copy/inactive book, no/ambiguous active loan, atomic
  rollback, and count bounds.
- A browser-origin smoke check proves the configured lower environment can read
  the API from the frontend origin; CORS transport failures are not mapped as
  business errors for the preflight handshake.
- `npm run backend:check` and generated-code verification pass.
- BE supplies reset/seed instructions using synthetic data only.

### Gate-C — FE readiness

- Centralized typed API boundary uses the frozen contract and maps business code,
  not HTTP status alone.
- Figma layout, responsive layout, all required UI states, stable locators, and
  no-fine/no-search behavior are present.
- Requirement manifest covers all canonical FR/AC headings and the skill-owned
  verifier passes with an 80% coverage artifact.
- FE lint, type-check, build, and relevant component/integration tests pass.

### Gate-D — PG integration and QA handoff

- Gates A-C are green and changes are limited to the allowed app paths plus
  coordination artifacts.
- API/UI smoke flow is executable with synthetic data: create -> list -> borrow
  -> last-copy status -> return -> restored availability.
- Business error behavior, no partial updates, no-login/default-Admin scope,
  no encryption, and data-testid integrity are rechecked.
- QA handoff contains test data, journey steps, expected API codes/states,
  locators, responsive checks, and Figma difference list.
- QA handoff identifies the active environment and confirms the corresponding
  CORS mode; bypass mode is never treated as a staging/production readiness
  claim.

PG integration has completed the API smoke journey and prepared the QA handoff.
BE has now implemented the runtime CORS policy; the test profile preflight test
and an `APP_ENV=dev` browser-origin OPTIONS probe both pass. Formal S6
acceptance remains pending until QA reruns the full matrix against the active
lower environment. Any remaining QA-owned Playwright maintenance must stay
within QA scope; PG must not edit `e2e/**`.

## 9. Validation command set

| Check | Command | Owner | Status |
| --- | --- | --- | --- |
| SD contract | `npm run sd:validate -- --requirement docs/requirements/REQ-LIB-001.md --architecture docs/architecture/ARCH-LIB-001.md --strict` | PG | baseline pass |
| Workflow | `npm run workflow:validate -- docs/workflows/WF-LIB-001.md` | PG | baseline pass |
| CORS runtime/preflight | Backend lower-environment `OPTIONS` preflight check from the configured frontend origin | BE/QA | pass: test profile integration test and `APP_ENV=dev` probe returned HTTP 200 without credentials |
| API generation | `npm run api:generate` | BE | pass |
| Generated cleanliness | `npm run api:verify-generated -- --generated-path apps/api/library-mini-admin-api/src/main/generated` | BE | pending: regenerated tracked output must be committed before strict verification can pass |
| Backend check | `npm run backend:check` | BE | pass: 15 tests |
| DB schema | `npm run db:validate -- --changelog apps/api/library-mini-admin-api/src/main/resources/db/changelog` | BE | pass: 2 changesets |
| FE lint/type-check | `npm run check:web` | FE | pass |
| FE build | `npm --prefix apps/web/library-mini-admin-web run build` | FE | pass |
| FE requirement verifier | `verify_requirement_tests.py` with FE manifest and coverage | FE | pass: 17/17 criteria, branch coverage 92.3% |
| Full repository check | `npm run check` | PG | pass |
| Smoke/E2E | API smoke plus `npm run e2e` | QA/PG handoff | API smoke pass; Playwright 3/3 browsers fail on stale QA skeleton assertion |
| Diff hygiene | scope/data-testid review and whitespace scan | PG | pass for product scope; generated output has expected regeneration diffs pending commit |

## 10. Gate log

| Gate | Result | Evidence / blocker |
| --- | --- | --- |
| Gate-A | complete | OpenAPI 7.25.0 generation, generated controller boundary, validation, and business-code tests passed; strict cleanliness awaits committing generated output. |
| Gate-B | complete | Liquibase validation, service/DAO/integration tests, count-bound checks, and API smoke flow passed. |
| Gate-C | complete | Vue implementation, centralized typed API client, required locators, unit/component tests, coverage, lint/type-check, build, and requirement verifier passed. |
| Gate-D | pending QA | API smoke and BE CORS preflight checks pass; formal E2E remains pending until QA reruns the journey with the active lower-environment configuration. |

## 11. Risks and escalation

- Missing Liquibase configuration or a local relational engine is a build/runtime
  decision. BE must use formatted SQL and record the selected test database;
  if a dependency or environment decision cannot be made from the frozen
  artifacts, escalate to PG/owner instead of changing SD documents.
- The current Spring Boot baseline is `3.5.12-SNAPSHOT`; this task may use it
  for local/test only and must not claim production readiness.
- The Figma export contains overdue/fine behavior; FE must omit it because
  `dueDate` is storage-only in the frozen MVP contract.
- The Figma export has four mock rows; neither FE nor BE may seed them as a
  mandatory product behavior.

### Current delivery blockers

- QA-owned `apps/web/library-mini-admin-web/e2e/vue.spec.ts` still expects the
  starter `h1` text `You did it!`; it fails in Chromium, Firefox, and WebKit.
  QA should replace it with the SCN-LIB-001 journey using the locators in
  Section 7, then run the QA verifier and produce `QA-LIB-001.md`.
- QA must rerun the full matrix with an explicit lower-environment setting
  (`APP_ENV=dev`, `APP_ENV=poc`, or the test profile). The BE implementation
  now applies `x-environment-cors-policy`; its test-profile preflight test and
  `APP_ENV=dev` browser-origin probe return HTTP 200 without credentials.
- `api:verify-generated` reports expected regeneration diffs in the generated
  Java directory. The generated OpenAPI output is intentionally unedited;
  commit it with the implementation and rerun the strict generated-cleanliness
  check.
- The frontend `api:check` command compares tracked diffs only, so generated
  regeneration requires the explicit status/strict verification above.

## 12. Completion definition

PG delivery is complete through Gates A-C and the Gate-D QA handoff. Overall
scenario delivery remains open until QA executes formal S6 acceptance and the
generated output is committed. QA remains the owner of formal S6 acceptance
and may reopen FE/BE by the workflow re-entry rules.
