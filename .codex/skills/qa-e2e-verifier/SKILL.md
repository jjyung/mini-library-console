---
name: qa-e2e-verifier
description: Verify a web scenario against its requirement with risk-based Playwright acceptance tests, isolated test data, preflight/NFR gates, root-cause classification, and an evidence-backed QA report. Use for QA verification of REQ-* deliveries and their linked SCN-* scenarios; do not use it to implement product code.
---

# QA E2E Verifier

Validate the delivered user journey, not merely whether a test command exits successfully. The output is a traceable decision about each FR/AC/NFR item, backed by reproducible commands and artifacts.

## Operating boundary

- QA-owned edits are limited to `apps/web/library-mini-admin-web/e2e/**`, the corresponding `docs/qa-report/QA-<DOMAIN>-<NNN>.md`, and the existing corresponding workflow state when the scenario workflow requires a handoff update.
- Do not modify application code, API contracts, migrations, Figma source, or infrastructure to make QA pass. Report missing `data-testid` contracts or product defects to the owning role.
- Never turn a retry, a changed assertion, or a test-only API shortcut into evidence that the product behavior is correct.
- Keep environment failures separate from product, contract, test-design, data-isolation, and third-party failures.

## Required inputs and startup check

Read these before changing a spec or reporting a result:

1. `README.md` and `AGENTS.md`.
2. The target scenario under `docs/scenarios/` and its workflow state under `docs/workflows/` when present. If a required workflow artifact is absent, record that fact; do not invent its state.
3. The target requirement, for example `docs/requirements/REQ-LIB-001.md`. If it is absent or not frozen, stop at a blocked handoff and state what is missing.
4. Existing upstream artifacts when present: architecture, OpenAPI/schema/API docs, PG task plan/summary, and the requirement-scoped Figma folder.
5. `references/qa-e2e-checklist.md`, `references/qa-report-template.md`, current specs under `apps/web/library-mini-admin-web/e2e/`, and `apps/web/library-mini-admin-web/playwright.config.ts`.
6. Root and web `package.json` scripts so commands are taken from the repository rather than guessed.

Resolve the requirement, scenario, workflow, and report IDs before execution. Keep the report path exact: `docs/qa-report/QA-<DOMAIN>-<NNN>.md`.

## Gate 0: scope and coverage matrix

Build the matrix before writing or editing tests. For every FR, AC, and applicable NFR record:

| Field | Required content |
| --- | --- |
| ID/source | Requirement item and source document/section |
| Risk | Impact, frequency, acceptance importance, and data/state complexity |
| Journey | User-visible Given/When/Then scenario and boundaries |
| Setup | Required identity, mutable data, immutable seed, and setup entry point |
| Verification | UI result, API effect, or other observable evidence |
| Test/evidence | Spec path, command, artifact, or manual observation |
| Status | `Pass`, `Partial`, `Fail`, or `Blocked` |

Use the requirement's stated behavior as the oracle. Include success, validation/error, permission, empty/loading/timeout, state-transition, and concurrency cases only when required by the requirement, architecture, NFR, or risk assessment. Do not invent expected product behavior.

Prioritize high-risk journeys rather than maximizing test count. A test is not coverage unless its assertion proves the relevant acceptance item. Code inspection alone is not `Pass` evidence.

Status definitions:

- `Pass`: the required behavior is verified with a reproducible passing check and no unresolved caveat.
- `Partial`: only part of the item is verified, or the test passes only after retry/flakiness is observed.
- `Fail`: the product or contract contradicts the requirement.
- `Blocked`: execution or verification cannot proceed because of a missing artifact, unavailable service, permission, or external dependency.

## Gate 1: Playwright architecture review

Before adding coverage, read [references/playwright-e2e-architecture.md](references/playwright-e2e-architecture.md). Apply its boundaries:

```text
test spec -> fixture -> (POM | API/service) -> data
```

- The spec states the user intent, business action, and business assertion.
- Fixtures compose resources and own setup/teardown; choose test or worker scope from the resource lifecycle.
- POMs own UI locators and UI actions, not backend data creation or the acceptance oracle.
- API/service clients prepare/query/clean state; use the real UI path for the behavior being accepted.
- Data definitions own state, ownership, uniqueness, namespace, and cleanup strategy.

Required isolation decisions:

- `Page` and `BrowserContext` remain test-isolated. Treat `storageState` as identity/session state, not permission to share mutable business data.
- Share a worker account or expensive resource only when tests cannot corrupt one another; mutable entities normally get a unique per-test identity.
- Make test data traceable with a run identifier plus worker/test identity (or the repository's equivalent), and make cleanup queryable and repeatable.
- Use teardown for normal cleanup, but also provide namespace/run tagging and TTL or batch cleanup for interrupted runs.
- Authenticate a test-support API explicitly; do not assume a page session is automatically applied to a separate request context.

Locator policy:

- Preserve every existing `data-testid` and follow the repository's `data-testid` contract for stable state, rows, containers, toasts, and dynamic content.
- For controls whose role, label, accessible name, or visible text is part of the requirement, prefer `getByRole`, `getByLabel`, or another user-facing locator so the assertion remains sensitive to that contract.
- Scope locators to the relevant component/row/dialog. Avoid CSS classes, DOM chains, XPath, positional selectors, and arbitrary `nth()` unless no stable alternative exists; document the exception.

## Gate 2: preflight

Run and record both checks before E2E:

```bash
lsof -iTCP:8080 -sTCP:LISTEN -n -P
lsof -iTCP:5173 -sTCP:LISTEN -n -P
```

- Backend must be reachable at the contract's configured local endpoint, normally `http://localhost:8080`.
- Frontend must be reachable at the Playwright `baseURL`, normally `http://localhost:5173` locally or `http://localhost:4173` in CI.
- Inspect `playwright.config.ts`: its `webServer` may start/reuse the frontend, but it does not prove backend readiness.
- If a service is missing, use the repository's documented `npm run dev`, `npm run dev:api`, or `npm run dev:web` command, preserve the process/log context, and re-check. Do not kill an unknown listener or silently change ports.
- Use a documented health endpoint when one exists. Otherwise record the port result and let the first contract/UI probe provide the service evidence.
- Capture the relevant Node/npm/package or app version and commit identifier when the report requires reproducibility.

If a preflight gate is not ready after the documented start/re-check path, mark affected coverage `Blocked` with reason `Blocked by environment`; do not mislabel it as a product failure.

## Gate 3: implement or review E2E coverage

When coverage is missing or weak, edit only the E2E layer within the ownership boundary. Keep tests deterministic and readable:

- Name tests after the user journey and acceptance outcome.
- Prepare complex mutable state through an API/test service; keep the UI steps focused on the behavior under test.
- Use web-first assertions and observable events. Do not use `waitForTimeout`, fixed sleeps, fixed database IDs, or assumptions about default sorting.
- Assert the business result and important failure behavior, not only that a click completed or a request returned `200`.
- Keep setup and cleanup visible through fixture names and data objects; do not hide the acceptance meaning in a generic flow helper.
- Before introducing a shared helper, ask: is the repetition mechanical, does the extracted code preserve the scenario's meaning, and can its change affect unrelated tests? Keep small semantic repetition when abstraction would obscure the oracle.

If a locator or product behavior is missing, record the exact locator/contract and owning rework target instead of editing product code from QA.

## Gate 4: execute and diagnose

Run in this order, adapting only to repository scripts/configuration:

1. Targeted affected spec on Chromium for fast feedback.
2. Full acceptance suite on Chromium, using the repository's equivalent of
   `--project=chromium`.
3. Stop after a passing run. Do not perform mandatory repeated stability runs.
   Trigger a retry only after a failure and only when there is a concrete
   hypothesis such as a transient environment issue, data collision, or a
   recently corrected test-design problem. Use a distinct run ID for each
   process and record the hypothesis and result.
4. Allow at most three process attempts for the same scope, including the
   initial attempt (initial run plus at most two retries). Never blindly rerun
   a passing test or exceed this cap. If a Playwright config adds per-test
   retries, record them and keep the total retry policy within the same cap;
   use an explicit `--retries=0` when process-level counting must be exact.
5. A parallelism check when data/state is mutable: use the configured worker
   mode and, when diagnosing isolation, compare with a one-worker run. A
   one-worker pass does not clear a parallel data defect.

Chromium is the default browser because it is the primary acceptance target.
Firefox and WebKit are opt-in only when the requirement, changed browser-
dependent behavior, release gate, or a Chromium finding gives a concrete
cross-browser reason. Do not fan every run out to all configured projects.
Preserve HTML reports, traces, screenshots, videos, logs, and test-result
paths that materially support a finding. Before writing evidence to a report
or shared artifact location, redact tokens, passwords, cookies, authorization headers,
and personal data. Keep raw evidence outside the deliverable only when its
storage is secure and authorized. A test that passes only after a retry is
flaky and remains `Partial` until explained and stabilized.

If the effective Playwright configuration or invocation uses only one worker (`workers=1`), parallelism has not been verified. Use an explicit multi-worker diagnostic when the environment allows it; otherwise mark the parallelism NFR `Partial` or `Blocked` with the reason.

For each failure, classify the first meaningful cause before changing anything:

- `Product`: UI/API behavior violates the requirement.
- `Contract`: OpenAPI, DTO, error code, or stable locator contract is inconsistent.
- `Test design`: assertion, locator, timing, or abstraction is wrong.
- `Data isolation`: shared mutable state, collision, order dependency, or failed cleanup.
- `Environment`: service, browser, dependency, port, credentials, or CI limitation.
- `Third-party`: sandbox/provider unavailable or returned an uncontrolled result.

Use trace/report artifacts, request/response evidence, service logs, and a focused rerun to support the classification. Do not repeatedly rerun without a new hypothesis. Route rework according to `AGENTS.md`: implementation to FE/BE, contract to SD, design to Archi, and requirement ambiguity to SA.

## Gate 5: report and handoff

Use `references/qa-report-template.md` as the report baseline; keep the repository's `docs/templates/qa-report-template.md` aligned when it is present. The report must contain:

- requirement/scenario/workflow IDs, scope, exclusions, and test environment;
- preflight results for `8080` and `5173`/`4173`;
- exact commands, timestamps or run labels, browser selection, first-attempt/retry outcomes, and any risk-triggered stability result;
- the complete FR/AC/NFR matrix with evidence links/paths and status;
- test architecture and data-isolation decisions for material E2E coverage;
- evidence redaction result for secrets and personal data;
- open issues with failure classification, owner/rework target, and next action.

If the corresponding workflow state exists, update its QA stage/loop, latest completed step, current status, blockers, next action, and next-session reading order. Do not create a fictional workflow state just to make the report look complete.

## Output contract

Final communication must include:

- target requirement ID;
- preflight status for backend `8080` and frontend `5173`/`4173`;
- E2E commands and outcomes, including browser selection and retries only when triggered by failure;
- coverage summary by `Pass`/`Partial`/`Fail`/`Blocked`;
- exact path of the updated `docs/qa-report/QA-<DOMAIN>-<NNN>.md`;
- blockers, defect classification, and next owner/action.

## Maintainer validation

When this skill or its supporting templates/references change, run the read-only validator:

```bash
npm run qa:skill:check
```

It checks required files, local Markdown links, synchronized report templates, skill-agent decoupling, evidence-redaction policy, and the Playwright worker caveat. It does not replace requirement coverage or E2E execution.

## References

- [Playwright E2E architecture and data-isolation guide](references/playwright-e2e-architecture.md) — read when creating, reviewing, or diagnosing specs.
- [QA execution checklist](references/qa-e2e-checklist.md) — use during execution.
- [QA report template](references/qa-report-template.md) — use when creating/updating the report.
- Design basis: [E2E 測試：AI 時代的自動化測試與驗收（八）](https://tech.samsonlab.dev/blog/ai-era-testing-and-acceptance-playwright-e2e-architecture/).
