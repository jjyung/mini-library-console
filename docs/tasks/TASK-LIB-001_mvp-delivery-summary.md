# TASK-LIB-001｜Library Mini Admin MVP Delivery Summary

## Result

PG completed the FE/BE implementation integration against the SD contract. Delivery is currently blocked by local toolchain prerequisites, so it is not yet ready for QA.

## Gate status

| Gate | Status | Evidence |
| --- | --- | --- |
| Gate-A BE contract-critical | implemented, unverified | Spring Boot controller, service, in-memory repositories, DTOs, business errors, and integration tests exist under `apps/api/**`. Java execution was unavailable. |
| Gate-B BE integration | blocked | `npm run check:api` stops before Maven because Java/JAVA_HOME is not configured. |
| Gate-C FE implementation | complete with E2E blocker | `npm run check:web` passed; frontend production build passed. `data-testid` contract is present. |
| Gate-D PG integration | blocked | Root check awaits Gate-B; Playwright E2E awaits browser installation. |

## Implemented scope

- `POST /books`: create book with ISBN canonicalization and duplicate protection.
- `GET /books`: list inventory with available/total counts and status.
- `POST /loans`: checkout an available active book.
- `POST /loans/returns`: return the earliest active loan for an ISBN, with optional readerId matching.
- Unified success/error envelopes with business codes and trace IDs.
- Global development CORS configuration allows the Vite frontend to call the API, including `OPTIONS` preflight requests.
- Vue UI aligned to the library admin Figma export: top bar, borrow/return cards, add-book form, inventory table, empty state, responsive layout, and business-code feedback.
- Centralized FE API client, store/composable, DTO-aligned models, and stable `data-testid` locators.
- Development API base URL configured through `.env.development` as `http://localhost:8080`.

## Validation record

Passed:

- `npm run check:web`
- `npm --prefix apps/web/library-mini-admin-web run build`
- OpenAPI structural checks: API IDs, path-oriented operationIds, and `$ref` targets.
- Vite dev runtime check: transformed `apiClient` resolved `VITE_API_BASE_URL` to `http://localhost:8080`.

Blocked:

- `npm run check:api`: no Java executable and `JAVA_HOME` is unset/invalid.
- `npm run e2e`: Playwright Chromium, Firefox, and WebKit executables are not installed.

Added since the previous handoff:

- `apps/api/library-mini-admin-api/src/main/java/com/example/library/config/WebConfig.java`
- CORS preflight integration coverage in `LibraryApiIntegrationTest`.

## Required next actions

1. Install/configure JDK 21 (`JAVA_HOME` and PATH), then run `npm run check:api`.
2. Install Playwright browsers with `npm --prefix apps/web/library-mini-admin-web exec playwright install`, then run `npm run e2e` against the running app.
3. Run root `npm run check` and hand off to QA using `docs/requirements/REQ-LIB-001.md` and the stable locators in `docs/tasks/TASK-LIB-001_mvp-delivery.md`.

## Open decisions retained

Q-001, Q-002, Q-003, and Q-005 remain product decisions. The MVP uses the documented assumptions: `readerId`, active-loan FIFO return, optional dueDate without fine calculation, and process-local storage.
