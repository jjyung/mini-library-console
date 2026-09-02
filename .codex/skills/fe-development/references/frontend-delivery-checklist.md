# Frontend Delivery Checklist

Use this checklist after implementation and before handing work to PG/QA.

## Readiness

- [ ] `AGENTS.md` and the target application were read.
- [ ] Requirement, story, scenario (if applicable), task, and workflow IDs are
      recorded.
- [ ] Architecture-approved framework, version, routing, state, styling, and
      test approach are known.
- [ ] Figma source is the latest requirement-scoped batch, or a design
      exemption is recorded.
- [ ] OpenAPI contract version and affected API IDs are known.
- [ ] No unresolved requirement/design/contract conflict is hidden in code.

## Traceability matrix

Create one row for every canonical `FR-*`/`AC-*` requirement criterion:

| FR/AC/story | Route/screen | User action | UI states | API ID/operation | `data-testid` | Test evidence |
| --- | --- | --- | --- | --- | --- | --- |
|  |  |  |  |  |  |  |

UI states should explicitly cover the states applicable to the behavior:
loading, success, empty, validation/client error, business error, forbidden,
network/timeout, and retry/recovery.

## Implementation

- [ ] Feature code follows the existing framework and project layout.
- [ ] Components have one responsibility; business logic is outside templates.
- [ ] State, actions, and getters/selectors have clear ownership.
- [ ] API calls use the centralized client/wrapper only.
- [ ] Generated code is isolated, reproducible, and not manually edited.
- [ ] Request/response models and field names match OpenAPI, or an explicit
      mapper documents the transformation.
- [ ] Business error codes map to stable UI behavior and localized/product
      messages.
- [ ] Runtime configuration supplies API base URLs and secrets are absent from
      source.

## Design and accessibility

- [ ] Layout, typography, spacing, colors, components, and assets match the
      selected Figma reference within the project's token system.
- [ ] Responsive behavior is defined for the supported viewports.
- [ ] Interactive controls have accessible names, focus behavior, keyboard
      support, and correct disabled/loading semantics.
- [ ] Existing `data-testid` values are preserved; new IDs are stable and
      tied to acceptance-critical behavior.

## Tests and checks

- [ ] A machine-readable requirement-to-test manifest exists for the delivered
      requirement.
- [ ] The manifest's criterion IDs match the canonical requirement document's
      complete `FR-*`/`AC-*` set exactly; no criterion is missing or invented.
- [ ] Every canonical requirement criterion has at least one unit-test evidence
      entry, with the criterion ID in the test title.
- [ ] Failing-first unit tests cover each requirement behavior's business
      outcome, rules, and mappers; UI micro-interactions may be component tests.
- [ ] Service/composable/hook/store tests cover success and business errors.
- [ ] Component tests cover interaction and non-happy states.
- [ ] Client/contract tests verify request shape and business-code mapping.
- [ ] The skill-owned verifier statically validates the canonical document and
      manifest, executes the real test command(s), and passes.
- [ ] The project's test runner enforces the 80% coverage threshold, the
      coverage artifact exists, and important assertions were reviewed.
- [ ] QA handoff includes the cross-screen journey, test data, expected states,
      API/mock expectations, and stable `data-testid` locators.
- [ ] FE did not add or modify QA-owned E2E specs.
- [ ] Type-check, lint, build, and API generation commands were run as
      applicable; skipped checks have reasons.
- [ ] Diff contains no generated-code hand edits, direct UI API calls, hard-
      coded URLs, accidental locator changes, or unrelated changes.

## Handoff

- [ ] FE review was completed; findings are resolved or documented with
      severity, evidence, owner, and next action.
- [ ] Task/workflow state includes completed step, current status, next action,
      blockers, QA status, and next-session files.
- [ ] Report names the requirement/story, Figma batch, API IDs, framework,
      commands, outcomes, assumptions, and limitations.
