---
name: qa-e2e-verifier
description: Validate web delivery against requirement acceptance criteria with E2E and NFR gates. Use when user asks QA to verify whether a feature/scenario is complete (for example REQ-LIB-001), run Playwright after frontend/backend preflight checks, and produce or update docs/qa-report.md with pass/fail coverage and evidence.
---

# QA E2E Verifier

Run this workflow to perform requirement-to-E2E verification with clear evidence.

## Workflow

1. Load requirement and runtime context.
- Read target requirement doc (for example `docs/requirements/REQ-LIB-001.md`).
- Read current E2E specs under `apps/web/library-mini-admin-web/e2e/`.
- Read Playwright config `apps/web/library-mini-admin-web/playwright.config.ts`.

2. Build the QA coverage matrix first.
- Map FR/AC/NFR to verification status: `Pass`, `Partial`, `Fail`, or `Blocked`.
- Prefer concrete evidence (test file, command output, UI/API behavior), not assumptions.

3. Perform mandatory preflight before E2E.
- Ensure backend is running on `http://localhost:8080`.
- Ensure frontend is running on `http://localhost:5173`.
- Check ports with:
  - `lsof -iTCP:8080 -sTCP:LISTEN -n -P`
  - `lsof -iTCP:5173 -sTCP:LISTEN -n -P`
- If service is not ready, start with `npm run dev` or split start (`npm run dev:api`, `npm run dev:web`) and re-check.

4. Run E2E and evaluate stability.
- Run `npm run e2e` at repo root (or `npm --prefix apps/web/library-mini-admin-web run test:e2e`).
- For NFR stability, run at least 3 consecutive times when required by QA gate.
- If test fails due to environment/network limits, mark as `Blocked by environment` and separate from product defects.

5. Update QA deliverables.
- Update or create `docs/qa-report.md`.
- Include:
  - Scope and requirement ID.
  - Preflight result.
  - Command list and outcomes.
  - Coverage matrix (FR/AC/NFR).
  - Blocking issues and next actions.

## Rules

- Keep QA ownership boundary:
  - Allowed edits: `apps/web/library-mini-admin-web/e2e/**`, `docs/qa-report.md`.
  - Do not modify business source code unless user explicitly asks to switch role.
- Prefer `data-testid` selectors in E2E.
- Distinguish product issues from environment issues explicitly.

## Output Contract

When reporting completion, include:
- Target requirement ID.
- Preflight status for `8080` and `5173`.
- E2E command results.
- Coverage summary (`Pass`/`Partial`/`Fail`/`Blocked`).
- Exact file path of updated `docs/qa-report.md`.

## Reference

Use [references/qa-e2e-checklist.md](references/qa-e2e-checklist.md) as the execution checklist.
