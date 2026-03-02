---
name: pg-task-orchestrator
description: Orchestrate PG delivery from planning to implementation by splitting tasks across BE/FE, managing handoff gates, executing staged implementation, running checks, and producing delivery summaries. Use when user asks PG to continue execution after task planning or to coordinate BE/FE implementation workflow.
---

# PG Task Orchestrator

Run this workflow to execute PG coordination and delivery end-to-end.

## Workflow

1. Load delivery context.
- Read `docs/tasks/*.md` target plan.
- Read `docs/openapi.yaml`, `docs/schema/*.md`, and `docs/api/*.md`.
- Read agent boundaries in `.codex/agents/pg.toml`, `be.toml`, `fe.toml`, and `qa.toml` when present.

2. Confirm split ownership.
- Assign backend tasks to BE (`apps/api/**`).
- Assign frontend tasks to FE (`apps/web/**`).
- Keep PG as coordinator for sequencing, dependency management, and final integration gate.

3. Execute by gates.
- Gate-A: BE completes contract-critical backend tasks (controller, validation, error mapping).
- Gate-B: BE completes backend integration checks and exposes API readiness notes.
- Gate-C: FE completes UI/client/data-testid implementation and frontend checks.
- Gate-D: PG verifies full checks and updates task status to delivery-complete.

4. Enforce implementation rules.
- Follow TDD order for BE tasks: test first, then implementation.
- Use centralized API client on FE; avoid direct fetch/axios in UI components.
- Require business code based handling and stable `data-testid`.
- Do not modify requirement/architecture/design docs unless user explicitly approves.

5. Validate each stage.
- Backend: run Maven test/check commands and record outcomes.
- Frontend: run type-check/lint/e2e and record outcomes.
- If environment blockers appear (missing binary/network/port), report blocker and fallback path.

6. Update delivery artifacts.
- Update `docs/tasks/{task-id}_{task_name}.md` status and gate progress.
- Generate `docs/tasks/{task-id}_{task_name}-summary.md` with completed scope, validations, blockers, and next actions.

## Output Contract

When reporting progress or completion, include:
- Current gate status (A/B/C/D).
- What was implemented by BE and FE.
- Validation command results.
- Any blocker and exact remediation.

## Reference

Use [references/pg-delivery-checklist.md](references/pg-delivery-checklist.md) as the execution checklist.
