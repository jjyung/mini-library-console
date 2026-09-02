# Requirement Test Verification

Use a machine-readable traceability manifest in addition to the human-facing
acceptance matrix. The manifest is checked against the canonical requirement
document, proves that every `FR-*`/`AC-*` criterion has declared test evidence,
and verifies that referenced test files exist and name the criterion in a test
title. The skill-owned verifier also runs the supplied test command(s) and
confirms that the coverage artifact exists. The project's test runner must
enforce the 80% coverage threshold; the verifier does not impose a report
format. This does not replace reviewing assertions or coverage quality.

## Test-level policy

Every user requirement represented by an `FR-*` or `AC-*` criterion must have
at least one unit-test evidence entry. This unit test must prove the required
behavior or business outcome; it does not require every UI behavior to be
implemented as a unit test. Use component or integration tests for behavior
that belongs at those boundaries, and add multiple levels when a criterion
crosses boundaries:

- `unit`: validation, mapper, business rule, store/composable/hook behavior;
- `component`: rendering, form interaction, accessibility, loading/empty/error
  states;
- `integration`: centralized client, request/response mapping, contract and
  business-code handling;
- E2E is QA-owned and is not a test level declared in the FE manifest.

Every manifest criterion therefore requires `unit`; `component` is added for
rendering and interaction details, and `integration` is added for client or
contract boundaries. For a complete user journey, FE supplies QA with steps,
data, expected states, API/mock expectations, and locator contracts; keep that
handoff explicit without adding FE-owned E2E code.

## Manifest format

Store one manifest per delivered requirement, for example:
`docs/traceability/FE-REQ-LIB-001.json`.

The IDs in this example are placeholders. During development, derive the
criterion IDs from the active canonical requirement document and create the
manifest for that task; the skill does not pre-assign project-specific IDs.

```json
{
  "requirementId": "REQ-LIB-001",
  "criteria": [
    {
      "id": "AC-LIB-001-001",
      "requiredLevels": ["unit", "component"],
      "tests": [
        {
          "level": "unit",
          "file": "src/features/books/book-state.spec.ts"
        },
        {
          "level": "component",
          "file": "src/features/books/book-list.spec.ts"
        }
      ]
    }
  ]
}
```

Put the criterion ID in the relevant `describe`/`it`/`test` title so the
relationship is visible during review, for example:

```ts
describe('[AC-LIB-001-001] available book count', () => {
  // assertions
})
```

## Verification command

Run the skill-owned helper
[verify_requirement_tests.py](../scripts/verify_requirement_tests.py). Do not
copy it into the repository or maintain a second repository-local
implementation. Pass the canonical requirement document and the manifest,
then pass each real test command that the FE task owns:

```bash
python3 <FE_SKILL_ROOT>/scripts/verify_requirement_tests.py \
  --requirement docs/requirements/REQ-LIB-001.md \
  --matrix docs/traceability/FE-REQ-LIB-001.json \
  --project-root . \
  --test-command "npm run test:unit" \
  --test-command "npm run test:component" \
  --coverage-file coverage/coverage-artifact
```

Resolve `<FE_SKILL_ROOT>` from the loaded `fe-development` skill location; in
this workspace it is `.codex/skills/fe-development`. The command is owned by
the skill, while the requirement, manifest, test files, and coverage artifact
are inputs from the target repository. Commands are tokenized and executed
without a shell, so use package-manager scripts rather than shell operators.
Configure the repository's native test runner to fail below 80% coverage and
make the test command produce its normal coverage artifact. The verifier only
requires the declared artifact to exist and does not prescribe its file format.

The repository's `package.json` owns the actual test, type-check, lint, build,
and generation commands. It does not need to duplicate the verifier:

```json
{
  "scripts": {
    "test:unit": "vitest run --coverage",
    "test:component": "vitest run --config vitest.component.config.ts",
    "check": "npm run test:unit && npm run test:component && npm run type-check && npm run build"
  }
}
```

The requirement and manifest paths must be selected per task; do not leave a
literal example requirement ID in a production command. If a project has
multiple active requirements, invoke the skill-owned helper once per manifest
or use a thin repository command wrapper that only supplies paths and test
commands, without reimplementing verification logic.

## Completion rule

Do not report a work item as complete when only `test:unit` passes. Completion
requires:

1. the canonical FR/AC matrix has no uncovered row;
2. every canonical `FR-*`/`AC-*` criterion has unit evidence;
3. the skill-owned verifier's static checks and declared test commands pass;
4. the test runner enforces 80% coverage, the coverage artifact exists, and
   important assertions are reviewed;
5. the QA E2E handoff is complete, and any skipped optional FE level is justified in
   the task/workflow handoff.
