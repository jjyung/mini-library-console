# QA E2E Checklist

## 1) Scope
- Confirm target requirement file (example: `docs/requirements/REQ-LIB-001.md`).
- Confirm expected AC/NFR items to verify.

## 2) Preflight (mandatory)
- Verify backend port: `lsof -iTCP:8080 -sTCP:LISTEN -n -P`.
- Verify frontend port: `lsof -iTCP:5173 -sTCP:LISTEN -n -P`.
- If not ready, start services and re-check.

## 3) Test execution
- Run: `npm run e2e`.
- If stability gate is required, run 3 consecutive times.

## 4) Evidence collection
- Save pass/fail and key error lines.
- Separate environment constraints from product defects.

## 5) Reporting
- Update `docs/qa-report.md` with:
  - Requirement ID and scope.
  - Preflight result.
  - Commands and outcomes.
  - FR/AC/NFR coverage matrix.
  - Open issues and next actions.
