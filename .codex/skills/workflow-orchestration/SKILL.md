---
name: workflow-orchestration
description: Coordinate scenario-driven delivery across SA, Archi, SD, PG, FE/BE, and QA by maintaining docs/workflows/WF-*.md state, enforcing stage gates, running parallel FE/BE execution, and routing QA rework loops. Use when user asks to start, resume, or advance a scenario workflow, or to decide the next role, required artifacts, and handoff state.
version: 1.0.0
owner: orchestrator
---

# Workflow Orchestration

Coordinate scenario-driven development so that any agent or session can safely
resume work from repository artifacts. Orchestration does not replace role
decisions; role procedures live in the role skills (`scenario-requirements-writer`,
`architecture-planner`, `sd-docs-producer`, `pg-delivery-coordination`,
`fe-development`, `be-development`, `qa-e2e-verifier`).

This skill is self-contained. Do not depend on role-agent configuration files
or require them as inputs.

## Entry gate

Start here before touching a stage. Read, in order:

1. `README.md`
2. `AGENTS.md`
3. The related scenario under `docs/scenarios/`
4. Existing `docs/workflows/WF-*.md` state files

Do not begin or advance development until the workflow state is understood. If
no state file exists and the request is scenario-driven, create one from
`docs/templates/workflow-state-template.md` using the
`WF-<DOMAIN>-<NNN>` ID format (for example `WF-LIB-001`).

## Stage model

```text
S0 Scenario Discovery
S1 SA
S2 Archi
S3 SD
S4 PG
S5A FE
S5B BE
S6 QA
S7 Done
```

Execution order:

```text
S0 → S1 → S2 → S3 → S4 → {S5A || S5B} → S6 → S7
```

- FE and BE may run in parallel after the PG gate.
- QA runs after FE and BE converge.
- QA may trigger rework loops; route by defect type, not by convenience.

## Workflow state is the source of truth

- State file: `docs/workflows/WF-<DOMAIN>-<NNN>.md`.
- New workflows must always be created from the repository template.
- Agents must never rely on conversation history alone; update the state file
  after every substantive action with the latest completed step, current
  status, next recommended action, blockers/questions, and the files the next
  session must read first.
- Validate the state file after updating it:

```bash
npm run workflow:validate -- docs/workflows/WF-<DOMAIN>-<NNN>.md
```

## Scenario discovery

1. Search existing `docs/workflows/WF-*.md` files.
2. If a workflow exists, resume it; do not create a duplicate.
3. If none exists, create one from the template before starting S1.

## Stage advancement rules

Advance only when the current stage artifacts are sufficient for the next role:

- SA must produce the requirement document.
- Archi must produce the architecture document.
- SD must produce the OpenAPI contract, global error codes, schema docs, and
  API flow docs.
- PG must produce the task plan before FE/BE start.

Run the current stage skill's validator and workflow gate before advancing. If
artifacts are incomplete: stop, ask the user, and mark the workflow
`needs_clarification`. Never invent critical business rules.

## Parallel FE / BE execution

FE and BE may run in parallel only when requirements, architecture, API/schema,
and the PG task plan are stable. Keep both aligned on API contract,
terminology, acceptance criteria, and scenario scope. Both must complete before
QA starts.

## QA auto loop

- Route implementation defects to FE/BE, contract gaps to SD, design conflicts
  to Archi, and requirement ambiguity to SA.
- Limit automatic attempts to a maximum of two retries per defect.
- If it still fails, mark the workflow `needs_clarification` and escalate
  instead of looping indefinitely.

## Missing information

Stop the workflow, ask the user, and set the state to `needs_clarification`.
When the user authorizes proceeding, record each safe assumption with its
rationale, impact if wrong, owner, and validation point.

## Exit gate / handoff

Every orchestration response and state update must include:

```md
## Workflow Status

Workflow:
Stage:
Status:

## Workflow Graph

S0 → S1 → S2 → S3 → S4 → {S5A || S5B} → S6 → S7

## Next Role

...

## Objective

...

## Required Artifacts

...

## Blocking Issues

...
```

Update the workflow state file before reporting completion. If the workflow
cannot advance, record the blocker and the exact user decision needed instead
of marking the stage done.

## Key principles

1. Respect the stage order; do not skip stages.
2. Keep state in artifacts, not conversation.
3. Prefer documentation before implementation.
4. Keep scope limited to the scenario.
5. Enable safe session handoffs.
