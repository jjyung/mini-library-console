# QA-<DOMAIN>-<NNN>

## Metadata

- Scenario ID: SCN-<DOMAIN>-<NNN>
- Requirement ID: REQ-<DOMAIN>-<NNN>
- Workflow ID: WF-<DOMAIN>-<NNN> (or `absent` when no workflow has been started)
- Report Type: QA verification report
- Status: `not_started` | `in_progress` | `blocked` | `done`
- Execution date/time: <timezone-aware timestamp>
- Commit/build: <commit, branch, or build identifier>
- Environment: <local/CI, browser projects, relevant service versions>
- Evidence redaction: <secrets and personal data masked/none present>

## Scope and decision

- Target user journey: <what is being accepted>
- Included: <FR/AC/NFR IDs>
- Excluded: <explicit exclusions and why>
- Decision: <pass/partial/fail/blocked summary>

## Preflight

| Dependency | Endpoint/port | Status | Evidence | Notes |
| --- | --- | --- | --- | --- |
| Backend | `http://localhost:8080` / `8080` | ready/blocked | <listener, health, or probe> | <notes> |
| Frontend | `http://localhost:5173` or CI `4173` | ready/blocked | <listener, baseURL, or probe> | <notes> |

## Test architecture and data isolation

- Spec(s): <paths>
- Fixtures and scopes: <test/worker resources>
- POM/component objects: <paths and responsibility>
- API/service setup: <paths/endpoints and why UI setup is not used>
- Data identity/namespace: <run/worker/test strategy>
- Cleanup and interrupted-run recovery: <teardown, batch cleanup, TTL>
- Locator contract: <data-testid and semantic locator decisions>

## Evidence and execution

| Run | Command | Result | First failure/retry | Artifacts |
| --- | --- | --- | --- | --- |
| Targeted | <command> | pass/fail/blocked | <details> | <paths> |
| Full | <command> | pass/fail/blocked | <details> | <paths> |
| Stability 1 | <command> | pass/fail/blocked | <details> | <paths> |
| Stability 2 | <command> | pass/fail/blocked | <details> | <paths> |
| Stability 3 | <command> | pass/fail/blocked | <details> | <paths> |
| Parallelism diagnostic | <command, if needed> | pass/fail/blocked | <details> | <paths> |

## Coverage Matrix

| Item | Type | Risk | Status | Journey/oracle | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| <FR/AC/NFR ID> | FR/AC/NFR | high/medium/low | Pass/Partial/Fail/Blocked | <observable expected result> | <test/command/artifact> | <caveat> |

Status meaning: `Pass` is reproducibly verified; `Partial` is incomplete or flaky; `Fail` contradicts the requirement; `Blocked` cannot be verified because of a gate or dependency.

## Open Issues

| Issue | Classification | Evidence | Owner/rework target | Severity/status |
| --- | --- | --- | --- | --- |
| <issue> | Product/Contract/Test design/Data isolation/Environment/Third-party | <path/log/test> | <FE/BE/SD/Archi/SA/QA> | <severity and status> |

## Next Action

- <one concrete next QA/rework/handoff action>
- Workflow update: <latest completed QA step, current status, blocker, next session files>
