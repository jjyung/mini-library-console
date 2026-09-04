# TASK-LIB-001 MVP Delivery Summary

## 1. Delivery status

- Task: `TASK-LIB-001`
- Workflow: `WF-LIB-001`
- Scenario: `SCN-LIB-001`
- Updated: 2026-09-04
- PG status: QA handoff ready; formal S6 acceptance pending
- Gate-A: complete
- Gate-B: complete
- Gate-C: complete
- Gate-D: pending QA verification

The test-only library mini admin flow is implemented in the allowed
`apps/api/**` and `apps/web/**` scopes. The confirmed boundaries remain no
login, default `Admin`, no encryption, local/test only, and no production
readiness claim.

## 2. Implemented scope

### Backend

- Spring Boot synchronous controller-service-dao flow for list, create,
  borrow, and return.
- OpenAPI Generator `7.25.0` output under
  `apps/api/library-mini-admin-api/src/main/generated/`; generated files were
  not manually edited.
- H2 file database in PostgreSQL compatibility mode for local/test execution,
  with Liquibase formatted SQL for `books` and `loans`.
- Business-code envelopes with `traceId`: `00000` success, `A0000` client
  error, and `B0000` system error.
- Atomic borrow/return count and loan updates, duplicate ISBN handling,
  inactive/no-copy handling, and ambiguous active-loan return handling.
- Backend tests and integration tests under
  `apps/api/library-mini-admin-api/src/test/**`.

### Frontend

- Vue 3/Vite implementation of `TopBar`, transaction tabs, borrow/return
  forms, add-book form, catalogue table, loading/empty/error/success states,
  and immediate refresh behavior.
- Centralized typed API access in
  `apps/web/library-mini-admin-web/src/core/api/libraryApi.ts`.
- Stable acceptance locators documented in the task plan and implemented under
  `apps/web/library-mini-admin-web/src/**`.
- Requirement traceability in
  `docs/traceability/FE-REQ-LIB-001.json`, covering all 17 canonical
  FR/AC headings.

## 3. Frozen API surface

| API ID | Method | Path |
| --- | --- | --- |
| `library-books-001` | GET | `/api/books` |
| `library-books-002` | POST | `/api/books` |
| `library-loans-001` | POST | `/api/loans/borrow` |
| `library-loans-002` | POST | `/api/loans/return` |

The contract remains `docs/openapi.yaml` version `1.0.0`; no requirement,
architecture, or SD artifact was changed during PG implementation.

## 4. Validation evidence

- `npm run api:generate` — pass.
- `npm run backend:check` — pass, 12 tests.
- `npm run db:validate -- --changelog apps/api/library-mini-admin-api/src/main/resources/db/changelog` — pass, 2 changesets.
- `npm run check:web` — pass.
- `npm --prefix apps/web/library-mini-admin-web run test:unit` — pass, 26 tests; statements 95.37%, branches 92.3%, functions 96.42%, lines 95.89%.
- `npm --prefix apps/web/library-mini-admin-web run test:component` — pass, 17 tests.
- `npm --prefix apps/web/library-mini-admin-web run build` — pass.
- Requirement verifier — pass, 17/17 canonical criteria, both test commands executed, coverage artifact found.
- `npm run check` — pass.
- `npm run sd:validate -- --requirement docs/requirements/REQ-LIB-001.md --architecture docs/architecture/ARCH-LIB-001.md --strict` — pass, 4 APIs, 0 errors, 0 warnings.
- `npm run workflow:validate -- docs/workflows/WF-LIB-001.md` — pass.

### Manual API smoke

Using synthetic data, the following journey passed against the local API:

`GET empty -> POST create -> GET AVAILABLE -> POST borrow -> GET BORROWED with
0 available -> POST return -> GET AVAILABLE with 1 available`.

All responses returned business code `00000`.

## 5. Open blockers and exact remediation

1. The existing QA-owned
   `apps/web/library-mini-admin-web/e2e/vue.spec.ts` still asserts the starter
   Vue skeleton heading `You did it!`. `npm run e2e` therefore fails in all
   three configured browsers. QA must replace that assertion with the
   SCN-LIB-001 journey, using the stable locators from
   `TASK-LIB-001_mvp-delivery.md`, and then run `qa-e2e-verifier`.
2. `npm run api:verify-generated -- --generated-path apps/api/library-mini-admin-api/src/main/generated`
   reports untracked generated output. Commit the generated directory without
   edits and rerun the command. This is repository-state hygiene, not a code
   generation defect.

## 6. Next handoff

QA should read the scenario, requirement, architecture, SD artifacts, task
plan, this summary, and the FE traceability manifest first; update the QA-owned
E2E test and produce `docs/qa-report/QA-LIB-001.md`. After QA passes, rerun
strict generated-output verification and advance the workflow to S7 if no
rework is required.
