# Workflow State: WF-<DOMAIN>-<NNN>

## 1. Metadata

- Workflow ID: WF-<DOMAIN>-<NNN>
- Scenario ID: SCN-<DOMAIN>-<NNN>
- Title: <short title>
- Owner Role: orchestrator
- Current Stage: S0|S1|S2|S3|S4|S5A|S5B|S6|S7
- Overall Status: not_started|in_progress|blocked|needs_clarification|needs_rework|done
- Priority: low|medium|high
- Created At: YYYY-MM-DD
- Updated At: YYYY-MM-DD
- Related Branch/Worktree: <optional>
- Related Files:
  - docs/scenarios/SCN-...
  - docs/requirements/REQ-...
  - docs/architecture/ARCH-...
  - docs/openapi.yaml
  - docs/tasks/TASK-...
  - docs/qa-report.md

## 2. Business Goal

- Why this workflow exists
- Expected business/user outcome
- In-scope
- Out-of-scope

## 3. Workflow Graph

```text
S0 Scenario Discovery
  ↓
S1 SA
  ↓
S2 Archi
  ↓
S3 SD
  ↓
S4 PG
  ├─→ S5A FE
  └─→ S5B BE
       ↓
      S6 QA
       ↓
      S7 Done
```

## 4. Current Objective

- Current Goal:
- Why this is the next step:
- Expected Output:
- Exit Criteria:

## 5. Stage Status

- S0 Scenario Discovery: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S1 SA: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S2 Archi: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S3 SD: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S4 PG: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S5A FE: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S5B BE: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S6 QA: done|...

  - Summary:
  - Output Files:
  - Open Questions:
- S7 Done: not_started|done

  - Summary:

## 6. Dependency / Blocking Status

- Blocking Issues:
- Missing Decisions:
- Waiting For:
- Safe Assumptions:
- Risks:

## 7. Parallel Work Plan

- FE can start when:
- BE can start when:
- Shared dependencies:
- Contract freeze point:
- Merge criteria:

## 8. Auto QA Loop

- QA Trigger Condition:
- Latest QA Result: pass|fail|not_run
- Defects:

  - DEF-001:

    - Severity:
    - Owner: FE|BE|SD|Archi|SA
    - Status:
    - Fix Plan:
- Re-entry Rule:

  - implementation bug -> FE/BE
  - contract gap -> SD
  - design conflict -> Archi
  - requirement ambiguity -> SA

## 9. Session Handoff Notes

- Last completed action:
- Recommended next action:
- Files to read first:
- Questions to resolve:
- Notes for next agent/session:

## 10. Completion Checklist

- [ ] Scenario exists and is valid
- [ ] Requirements are complete
- [ ] Architecture is complete
- [ ] API / schema is complete
- [ ] PG plan is complete
- [ ] FE implementation is complete
- [ ] BE implementation is complete
- [ ] QA verification is complete
- [ ] Artifacts are consistent
- [ ] Scope has not drifted
