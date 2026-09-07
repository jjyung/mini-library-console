# QA E2E Execution Checklist

Use this checklist with `SKILL.md`. Check items with evidence, not assumptions.

## 1. Handoff and scope

- [ ] Read `README.md` and `AGENTS.md`.
- [ ] Read the target scenario, workflow state when present, requirement, and available architecture/SD/PG/Figma artifacts.
- [ ] Confirm requirement/scenario/workflow/report IDs and report path.
- [ ] Convert every FR/AC/NFR into a coverage row with risk, journey, setup, oracle, and status.
- [ ] Mark missing or unfrozen upstream artifacts as a gate/blocker.

## 2. Architecture and testability

- [ ] Read `references/playwright-e2e-architecture.md`.
- [ ] Keep spec intent/assertions separate from fixture lifecycle, POM UI mechanics, API/service setup, and data ownership.
- [ ] Decide test versus worker scope for every shared resource.
- [ ] Decide unique ID/namespace, correlation/run tag, teardown, and interrupted-run cleanup.
- [ ] Preserve `data-testid`; use semantic locators when role/label/text is itself part of the requirement.
- [ ] No fixed mutable IDs, fixed ordering, arbitrary sleeps, or setup API used as the UI acceptance oracle.

## 3. Preflight (mandatory)

- [ ] Backend listener checked: `lsof -iTCP:8080 -sTCP:LISTEN -n -P`.
- [ ] Frontend listener checked: `lsof -iTCP:5173 -sTCP:LISTEN -n -P` (or CI port `4173`).
- [ ] `playwright.config.ts` `baseURL`, `webServer`, projects, retries, workers, and artifact settings reviewed.
- [ ] Services started only with documented repository commands when needed, then re-checked.
- [ ] Environment/version/commit context recorded when reproducibility matters.

## 4. Execution and diagnosis

- [ ] Targeted affected spec/project run on Chromium.
- [ ] Full acceptance suite run on Chromium (or documented equivalent with `--project=chromium`).
- [ ] Passing run stopped without an unnecessary stability rerun.
- [ ] Any retry was failure-triggered, hypothesis-driven, assigned a distinct run ID,
      and kept within three total process attempts for the same scope.
- [ ] Firefox/WebKit were run only when a documented cross-browser risk or release
      gate required them; otherwise Chromium-only scope was recorded.
- [ ] Parallelism/isolation exercised for mutable data; one-worker comparison used only as a diagnostic.
- [ ] If the effective worker count is `1`, parallelism is explicitly marked `Partial` or `Blocked` rather than treated as verified.
- [ ] Failure artifacts preserved: HTML report, trace, screenshot/video, logs, request/response evidence as available.
- [ ] Secrets, credentials, cookies, authorization headers, and personal data redacted before evidence is shared or committed.
- [ ] Each failure classified as product, contract, test design, data isolation, environment, or third-party.
- [ ] Retry-only passes reported as flaky/`Partial`, not unqualified `Pass`.

## 5. Report and handoff

- [ ] Read `references/qa-report-template.md`; keep `docs/templates/qa-report-template.md` aligned when present.
- [ ] Report preflight, browser scope, exact commands, run outcomes, retries when
      triggered, and all FR/AC/NFR rows.
- [ ] Link evidence paths and record first failure plus retry behavior.
- [ ] Record blocker/defect owner, rework target, and next action.
- [ ] Update the corresponding existing workflow state with QA status and next-session handoff notes.
