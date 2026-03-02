# LIB-API-001 API Delivery Plan

## Dispatch Status
- PG: 已完成 BE/FE 任務分派與整合驗證，進入 Gate-D 結案。
- BE: `TASK-01`、`TASK-02`、`TASK-05`、`TASK-06`、`TASK-07` 驗證完成（`mvn test` 綠燈）。
- FE: `TASK-08` 驗證完成（`type-check`、`lint`、`test:e2e` 全綠）。

## Scope
- Inputs:
  - `docs/openapi.yaml`
  - `docs/schema/books.md`
  - `docs/schema/loans.md`
  - `docs/api/library-books-001_create-book.md`
  - `docs/api/library-books-002_list-books.md`
  - `docs/api/library-books-003_search-books.md`
  - `docs/api/library-loans-001_borrow-book.md`
  - `docs/api/library-loans-002_return-book.md`
- Target APIs (5):
  - `library-books-001` Create Book
  - `library-books-002` List Books
  - `library-books-003` Search Books
  - `library-loans-001` Borrow Book
  - `library-loans-002` Return Book
- Constraints:
  - Backend follows strict TDD (test first, then implementation).
  - UI only minimal operable flow and all interactive elements must include `data-testid`.
  - Business error code mapping must follow `00000/A0000/B0000/C0000` family and concrete codes defined in OpenAPI.

## Executable Task Summary
1. Build DB migration baseline for `books` and `loans`, including indexes and check constraints from schema docs.
2. Implement domain model and repository contracts first, then write repository-level tests for persistence and transaction edge cases.
3. Implement domain services with pure business rules for all 5 APIs, driven by service unit tests first.
4. Implement controllers and request validation to fully match OpenAPI request/response DTO and error code behaviors.
5. Implement centralized exception-to-business-code mapping (`A0001~A0007`, `B0000`, `C0000`) with API-level tests.
6. Add integration tests covering end-to-end transaction behavior for borrow/return and query consistency for list/search.
7. Build minimal web UI for add/list/search/borrow/return flow with centralized API client and mandatory `data-testid` locators.
8. Run full verification (`./scripts/check.sh` if provided; otherwise project-equivalent api/web test and lint commands) and fix failures.

## BE/FE Assignment Matrix
| Task | Primary Owner | Secondary Owner | Dependency |
| --- | --- | --- | --- |
| TASK-01 DB Migration Baseline | BE | PG | None |
| TASK-02 Repository Layer for Books/Loans | BE | PG | TASK-01 |
| TASK-03 Domain Services (Books) | BE | PG | TASK-02 |
| TASK-04 Domain Services (Loans) | BE | PG | TASK-02 |
| TASK-05 Controller + DTO + Validation Alignment | BE | PG | TASK-03, TASK-04 |
| TASK-06 Error Code Mapping and Exception Strategy | BE | PG | TASK-05 |
| TASK-07 API Integration Tests | BE | PG | TASK-06 |
| TASK-08 Minimal Web UI + data-testid Coverage | FE | BE | TASK-05, TASK-06 |
| TASK-09 Final Verification and Gate | PG | BE, FE | TASK-07, TASK-08 |

## BE/FE Handoff Gates
1. Gate-A (BE -> FE): BE 完成 TASK-05 與 TASK-06 後，提交 API 可用清單（path、request sample、response sample、error code sample）。
2. Gate-B (BE -> FE): BE 完成 TASK-07 後，提供整合測試結果與已知限制，FE 再進行 UI 串接收斂。
3. Gate-C (FE -> PG): FE 完成 TASK-08 後，提交 `data-testid` 清單與操作旅程，交由 PG/QA 驗收。
4. Gate-D (PG): PG 彙整 BE/FE 驗證結果，執行 TASK-09 並輸出最終交付狀態。

## Task Backlog (TDD Ordered)

### TASK-01: DB Migration Baseline
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - `docs/schema/books.md`
  - `docs/schema/loans.md`
- Outputs:
  - Migration scripts for `books`, `loans`, indexes, constraints
  - Migration rollback strategy (if framework supports)
- TDD Order:
  - Test first: migration test or startup schema verification test proves tables/constraints exist
  - Then implementation: add migration scripts/config
- DoD:
  - App startup can initialize schema in test environment
  - `books` and `loans` DDL structure matches schema docs
  - Check constraints and indexes are verifiable by automated test

### TASK-02: Repository Layer for Books/Loans
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - OpenAPI DTO fields
  - schema constraints and query needs
- Outputs:
  - `BookRepository` / `LoanRepository` interfaces + implementations
  - Query methods for create/list/search/borrow-return support
- TDD Order:
  - Test first: repository tests for insert/find/search/update and transaction-safe update conditions
  - Then implementation: repository code and SQL mapping
- DoD:
  - Duplicate ISBN conflict is detectable
  - Borrow decrement requires `available_quantity > 0`
  - Return increment is capped by `total_quantity`
  - Active loan lookup by `isbn` (+ optional `readerId`) is supported

### TASK-03: Domain Services (Books)
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - `library-books-001/002/003` flow docs
  - OpenAPI business codes
- Outputs:
  - `BookDomainService` methods for create/list/search
  - domain validation and status mapping logic
- TDD Order:
  - Test first: service unit tests for success/error branches
  - Then implementation: service logic
- DoD:
  - `library-books-001`: `A0002`, `A0003`, `B0000`, `00000` behavior aligned
  - `library-books-002`: optional `shelfStatus` filter and `A0003` validation aligned
  - `library-books-003`: keyword required/min length handling aligned
  - `circulationStatus` computed per schema state mapping rules

### TASK-04: Domain Services (Loans)
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - `library-loans-001/002` flow docs
  - loan constants (`DEFAULT_LOAN_PERIOD_DAYS=14`, `FINE_PER_DAY_TWD=5`)
- Outputs:
  - `LoanDomainService` methods for borrow/return
  - transactional orchestration using repositories
- TDD Order:
  - Test first: service unit tests for all branches
  - Then implementation: borrow/return logic with transaction boundary
- DoD:
  - Borrow path returns `A0001/A0004/A0005/A0003/B0000/00000` correctly
  - Default due date is applied when missing and `defaultDueDateApplied=true`
  - Return path returns `A0001/A0006/A0007/A0003/B0000/00000` correctly
  - Overdue days and fine amount calculation are deterministic and tested

### TASK-05: Controller + DTO + Validation Alignment
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - `docs/openapi.yaml` schemas and operation IDs
- Outputs:
  - Controllers for 5 endpoints
  - Request/Response DTO classes matching OpenAPI naming and fields
  - Validation annotations and request binding
- TDD Order:
  - Test first: controller tests asserting payload validation and response envelope
  - Then implementation: controller/DTO wiring
- DoD:
  - Paths, methods, and payload fields fully match OpenAPI
  - Response envelope always includes `code`, `message`, `data`
  - HTTP 200/400/500 usage is contract-consistent with business code payload

### TASK-06: Error Code Mapping and Exception Strategy
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - AGENTS business code rules
  - OpenAPI response examples
- Outputs:
  - Centralized exception hierarchy and global handler
  - Mapping table from domain errors to business codes/messages
- TDD Order:
  - Test first: exception mapping tests per known code
  - Then implementation: exception classes + handler + mapper
- DoD:
  - No branch falls back to raw stack message
  - Expected domain errors map to `A0001~A0007`
  - Unexpected internal errors map to `B0000`
  - Extension point reserved for `C0000` third-party errors

### TASK-07: API Integration Tests
- Type: Backend
- Primary Owner: BE
- Secondary Owner: PG（追蹤進度與阻塞）
- Inputs:
  - all endpoint flows and DB transaction rules
- Outputs:
  - Integration tests for 5 APIs with real persistence in test profile
- TDD Order:
  - Test first: failing integration scenarios defined from flow docs
  - Then implementation: fill missing application wiring until tests pass
- DoD:
  - Borrow then list/search reflects updated inventory and borrower info
  - Return then list/search reflects restored inventory
  - Overdue return produces `A0007` with populated `data` payload
  - Invalid requests and system-failure simulations return expected business code

### TASK-08: Minimal Web UI + data-testid Coverage
- Type: Frontend
- Primary Owner: FE
- Secondary Owner: BE（API 串接支援）/ PG（協調）
- Inputs:
  - 5 API contracts
  - API flow docs + QA locator rule
- Outputs:
  - Minimal pages/components for:
    - create/list/search books
    - borrow book
    - return book
  - Centralized API client + DTO-aligned models
  - Complete `data-testid` on interactive UI elements
- TDD Order:
  - Test first: component/e2e locator and flow tests (if current web test setup supports)
  - Then implementation: UI and API integration
- DoD:
  - No component uses direct `fetch/axios` outside centralized API client
  - Every button/input/select/form submit/result trigger has stable `data-testid`
  - Error messages are shown via business code mapping, not raw HTTP status

### TASK-09: Final Verification and Gate
- Type: Backend + Frontend
- Primary Owner: PG
- Secondary Owner: BE + FE
- Inputs:
  - previous task outputs
- Outputs:
  - Green verification report
- TDD Order:
  - Red/green cycle completion: all new tests pass before merge
- DoD:
  - `./scripts/check.sh` passes if present
  - If not present, equivalent project checks documented and passing:
    - API tests
    - Web type-check/lint/test
  - No contract drift against `docs/openapi.yaml`

## API-to-Task Traceability
- `library-books-001` -> TASK-02, TASK-03, TASK-05, TASK-06, TASK-07, TASK-08
- `library-books-002` -> TASK-02, TASK-03, TASK-05, TASK-06, TASK-07, TASK-08
- `library-books-003` -> TASK-02, TASK-03, TASK-05, TASK-06, TASK-07, TASK-08
- `library-loans-001` -> TASK-01, TASK-02, TASK-04, TASK-05, TASK-06, TASK-07, TASK-08
- `library-loans-002` -> TASK-01, TASK-02, TASK-04, TASK-05, TASK-06, TASK-07, TASK-08

## Clarification Questions (Need User Confirmation Before Execution)
1. Return API (`library-loans-002`) currently allows optional `readerId`. If omitted and multiple active loans exist for same ISBN, should backend:
   - a) reject with `A0003` (ambiguous request), or
   - b) auto-select earliest borrowed record, or
   - c) auto-select latest borrowed record?
2. `POST /library/loans` due date validation boundary is unspecified. Should due date earlier than borrow date be treated as `A0003`?
3. For `GET /library/books/search`, should keyword matching be case-insensitive and partial-match for all of `title/isbn/author`?
4. For `GET /library/books` and `/search`, should default sorting be `updatedAt desc`?
5. `currentBorrowerReaderId` and `currentDueDate` in book list/search response:
   - when multiple active loans exist for one ISBN, should we return the earliest due one, latest borrowed one, or null?
6. For return overdue warning (`A0007`), UI should display warning banner and still refresh inventory. Please confirm this is expected success-with-warning behavior.
7. Please confirm mandatory `data-testid` naming convention. Proposed prefix set:
   - `book-create-*`, `book-list-*`, `book-search-*`, `loan-borrow-*`, `loan-return-*`
8. Please confirm minimum required interactive elements for QA locator coverage:
   - create form: title/isbn/author/category/quantity/shelfStatus/submit
   - list filter: shelfStatus select/apply/reset
   - search: keyword input/search trigger/clear
   - borrow: readerId/isbn/dueDate(optional)/submit
   - return: isbn/readerId(optional)/returnedAt(optional)/submit
   - result panel: code/message/data toggle
9. Repository tech choice confirmation: use Spring JDBC/JdbcTemplate for MVP (without JPA), is this acceptable?
10. `./scripts/check.sh` is not present in current repository. Should we create it during implementation, or treat Maven + web scripts as the acceptance command set?

## Wait State
- Current status: Planning complete (Workflow Step 1~3).
- Next action: wait for user reply `確認執行` and answers to clarification questions before starting TDD implementation.
