# TASK-LIB-001 MVP Delivery Summary

## Result

PG coordination completed the TEST-only Library Mini Admin MVP delivery and
closed the S7 workflow gate. FE and BE implementation boundaries remained within `apps/web/**` and
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
- Figma visual rework in `apps/web/library-mini-admin-web/src/App.vue`: light
  surface tokens, card/form/table styling, Figma-aligned desktop placement,
  and mobile stacking while preserving the existing UI locator contract.
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
| Figma visual smoke (Chromium desktop + 390px viewport) | PASS |
| FE requirement verifier | PASS — all 16 canonical FR/AC criteria have unit evidence |
| `npm run api:generate` | PASS |
| `npm run api:verify-generated` | Known limitation — generator rewrites annotation timestamps; generated output is tracked and generated paths are clean after restoring the timestamp-only diff |
| Workflow state | PASS — S7 Done; workflow marked `done` |

## QA Result

QA completed the synthetic journey and the additional search, pagination,
validation/error, repeated-write, responsive, keyboard and locator checks.
Targeted AC-008 passed 1/1 and the full Chromium suite passed 6/6 with two
workers and no cross-test data pollution. The QA decision is Pass with
limitations; non-blocking pixel-diff, axe/contrast, independent loading-state
assertions and complete server-log correlation remain explicitly recorded in
`docs/qa-report/QA-LIB-001.md`.

## Known MVP Limitations

- Idempotency replay is in-memory and single-instance only for this TEST MVP;
  durable distributed idempotency is out of scope.
- Q-001 through Q-004 follow the documented SD baseline but still require
  product confirmation before any UAT/PROD expansion.
- Generated API source and frontend schema output are tracked in the current
  checkout; generated paths are clean, with only the expected workflow docs
  changed in this session. OpenAPI Generator 7.25.0 still rewrites annotation
  timestamps during regeneration, so `api:verify-generated` needs a
  generator-stability fix before it can be a deterministic source-control gate.
