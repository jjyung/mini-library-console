# TASK-LIB-001 MVP Delivery Summary

## Result

PG coordination completed the TEST-only Library Mini Admin MVP handoff. FE
and BE implementation boundaries remained within `apps/web/**` and
`apps/api/**`; upstream requirement, architecture, OpenAPI, schema and API
flow artifacts were not changed during implementation.

## Delivered

- Spring generated API boundary from OpenAPI Generator `7.25.0`, retained
  under `apps/api/library-mini-admin-api/src/main/generated/`.
- H2 `2.3.232` file-backed TEST runtime, JDBC DAO layer and Liquibase formatted
  SQL migrations for `books` and `loans`.
- Explicit TEST CORS allowlist for the local Vite dev/preview origins
  `http://localhost:5173` and `http://localhost:4173`, including the mutation
  headers used by the generated client.
- Jackson `JsonNullableModule` registration for generated optional request and
  response fields such as `author` and `dueDate`.
- Book creation, case-insensitive catalogue search, 1-based pagination,
  borrow, return, inventory/status invariants, idempotency replay and
  `00000`/`A0000`/`B0000`/`C0000` response mapping.
- Vue 3 Figma-aligned console with centralized `openapi-fetch` client,
  generated `openapi-typescript` models, search, page/page-size controls,
  empty/loading/error states, add-book form, borrow/return tabs and responsive
  table layout.
- Stable UI locators including `library-topbar-search`, transaction tabs and
  submits, add-book fields/form, `library-book-table`,
  `library-book-row`, and row `data-book-id`.
- FE traceability manifest:
  `docs/traceability/FE-REQ-LIB-001.json`.

## Verification

| Check | Result |
| --- | --- |
| `npm run check:api` | PASS — 16 Maven tests, including CORS preflight and optional-field borrow journey |
| `npm run db:validate -- --changelog apps/api/library-mini-admin-api/src/main/resources/db/changelog` | PASS — 2 changesets |
| `npm --prefix apps/web/library-mini-admin-web run test:unit` | PASS — 27 tests |
| FE coverage | PASS — 97.81% lines, 96.57% statements |
| `npm --prefix apps/web/library-mini-admin-web run type-check` | PASS |
| `npm --prefix apps/web/library-mini-admin-web run lint` | PASS |
| `npm --prefix apps/web/library-mini-admin-web run build` | PASS |
| FE requirement verifier | PASS — all 16 canonical FR/AC criteria have unit evidence |
| `npm run api:generate` | PASS |
| `npm run api:verify-generated` | Pending commit — generated path is untracked in this worktree |

## QA Handoff

QA should start the API and web app, create ISBN `978-0-13-235088-4`, borrow
with `qa-reader-001`, verify `availableCount` and status, return the exact
`loanId`, and verify the original inventory. QA should also cover search,
`page=2&pageSize=1`, empty results, validation/business errors, duplicate ISBN,
inactive/unavailable books, repeated writes, responsive layout, keyboard
navigation and the frozen locators.

The existing Playwright starter spec remains QA-owned and was not modified by
FE. QA should update or replace that starter expectation and produce
`docs/qa-report/QA-LIB-001.md`.

## Known MVP Limitations

- Idempotency replay is in-memory and single-instance only for this TEST MVP;
  durable distributed idempotency is out of scope.
- Q-001 through Q-004 follow the documented SD baseline but still require
  product confirmation before any UAT/PROD expansion.
- Generated API source and frontend schema output must be staged/committed,
  then `npm run api:verify-generated` rerun as the source-control gate.
