---
name: fe-development
description: Implement or review assigned frontend work from requirements, stories, Figma flows, and frozen OpenAPI artifacts. Apply the architecture-approved Vue, Angular, or React approach, centralize typed API access, preserve data-testid contracts, and validate with TDD. Use for frontend implementation or review, not backend, requirements, or system-architecture work.
---

# FE Development

Keep the assigned frontend scope traceable from requirement to design, API
contract, code, tests, review, and handoff. Use the target project's framework,
versions, package manager, component library, and test runner as the source of
implementation details. FE owns unit, component, and integration tests;
Playwright/E2E specs and QA reports are QA-owned.

## 1. Load the handoff and freeze scope

Read the repository guidance and the artifacts applicable to the assignment:

- `README.md` and `AGENTS.md`;
- scenario, workflow, and task artifacts when the work is scenario-driven;
- the canonical requirement and story;
- the architecture decision for framework, rendering, routing, state, and
  styling;
- the frozen OpenAPI contract, error-code policy, schema, and API flow;
- the requirement-scoped Figma material and existing application.

Record the requirement/story, Figma batch, API IDs, task scope, and assumptions.
Do not silently change upstream artifacts to make FE implementation easier.
Report missing or conflicting inputs when they can change the flow, API
boundary, security behavior, design, or framework decision.

Confirm these handoff gates before coding:

| Gate | Evidence |
| --- | --- |
| Scope | FE assignment, in/out scope, requirement/story IDs |
| Flow | entry, actions, success, loading, empty, validation, error, retry |
| Design | requirement-scoped Figma source or design exemption |
| Contract | path, method, operationId, API ID, DTOs, business errors |
| Architecture | framework/version, routing, state, styling, test strategy |
| QA | locator expectations and acceptance-to-test mapping |

## 2. Respect the approved architecture

Use the framework selected by Archi or already established by the target app.
Do not introduce a second framework because a Figma export uses another one.
Use the project's existing conventions; make only non-critical defaults that
are consistent with the selected architecture.

Keep features cohesive and components focused. Put business logic in the
project's service, composable, hook, store, or mapper boundaries rather than
templates. Keep API configuration, generated code, error mapping, and UI
adapters separated. Avoid duplicated API logic, global mutable state, magic
values, and unrelated migrations.

## 3. Map requirements, design, and API behavior

Create an interaction matrix before implementation:

`FR/AC/story -> screen/route -> action -> state -> API ID -> UI result -> test`

Use the latest requirement-scoped Figma source. Treat Figma exports as visual
and interaction references, then adapt them to the selected framework, tokens,
accessibility rules, responsive behavior, and existing components. Use the
applicable Figma design-to-code workflow for live Figma context or Figma writes.

Preserve existing `data-testid` values. Add stable IDs for new
acceptance-critical controls and states. Prefer accessible names and roles for
general interaction while keeping `data-testid` as the QA contract.

## 4. Use one generated API boundary

Treat the frozen OpenAPI contract as the source of truth for paths, methods,
parameters, schemas, DTO names, and business-error responses. All UI API calls
go through one centralized client or application service; components must not
call `fetch`, `axios`, or generated functions directly.

Keep generated output isolated, reproducible, version-pinned, and unedited.
Put runtime configuration, auth, retries, normalization, error mapping, and
view-model transformations outside generated output. Use an explicit mapper
when view data differs from contract data.

Use the selected framework's approved generator and read
[the API generation reference](references/openapi-client-generation.md) for
the applicable setup and verification gate. Map errors by business code, not
HTTP status alone, and implement the story's required pending, success, empty,
validation, business-error, permission, network, and recovery states.

## 5. Develop with TDD and verify requirements

Start from the interaction matrix and write failing tests for required
behavior, then implement and refactor. Select the smallest boundary that proves
the behavior:

- unit tests for requirement behavior, business outcomes, rules, and mappers;
- component tests for rendering, accessibility, and UI state transitions;
- integration tests for the centralized client and contract/error mapping;
- QA handoff for cross-screen journeys; do not create or modify FE-owned E2E.

Maintain the machine-readable requirement-to-test manifest described in
[the verification reference](references/requirement-test-verification.md).
At task time, derive criterion IDs from the canonical requirement document;
IDs in examples are placeholders. Every canonical `FR-*`/`AC-*` criterion must
have unit evidence, while UI micro-interactions may be covered by component
tests. Run the skill-owned verifier with the active requirement, manifest,
real test command(s), and coverage artifact. The project's test configuration
must enforce the 80% coverage threshold; the verifier must confirm the test
commands pass and the artifact exists. Do not copy the verifier into the repo.

Test behavior and acceptance criteria, not implementation details. Mock APIs at
the client or network boundary, retain at least one contract-backed check, and
do not weaken existing tests.

## 6. Implement and validate

Implement in vertical slices that complete user-visible behavior, including
normal and required non-happy states. Run the narrowest relevant checks, then
the repository's type-check, lint, build, generation, and FE check scripts as
applicable. Record skipped checks and reasons. QA runs the E2E gate after the
FE/BE handoff.

Before handoff, inspect the diff for contract/design mismatch, direct API calls,
hard-coded URLs, generated-code edits, broken locators, missing accessibility
states, raw business errors, unrelated changes, and incomplete requirement
test evidence.

## 7. FE review stage

Perform an explicit FE review before handoff. `review fe` may invoke this stage
independently; in review-only mode, report findings without changing source
files unless the user requests fixes.

Review the implementation against the canonical requirement, story, Figma flow,
OpenAPI contract, test evidence, coverage configuration, and workflow/task
handoff. Confirm all canonical `FR-*`/`AC-*` criteria are represented and that
the QA E2E handoff contains journey steps, data, expected states, API/mock
expectations, and stable locators.

Classify findings as blocker, requirement gap, contract gap, design gap, test
gap, maintainability issue, or documentation gap. Record affected criterion or
API ID, evidence, impact, owner, and next action. Resolve findings or document
accepted limitations before handoff.

## 8. Handoff and output contract

For scenario-driven work, update the workflow state and task artifact with the
completed FE step, current status, next action, blockers, QA loop status, and
next-session files. Do not create workflow state for a non-scenario task unless
the repository workflow requires it.

Report the requirement/story, Figma batch, API IDs, framework/version decision,
interaction-matrix coverage, preserved/added `data-testid` values, generation
details, test and review outcomes, QA E2E handoff status, limitations, skipped
checks, blockers, and next action.

Use [the delivery checklist](references/frontend-delivery-checklist.md) as the
final review gate.
