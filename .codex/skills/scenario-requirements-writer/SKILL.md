---
name: scenario-requirements-writer
description: Analyze scenario documents and related Figma export materials, then generate a structured requirements analysis file with FR/NFR/AC, business rules, edge cases, risk items, and business error-code mapping. Use when user asks to analyze a scenario (e.g., SCN-*) and produce or update a requirement document (e.g., REQ-*).
version: 1.0.0
owner: sa
---

# Scenario Requirements Writer

Execute this workflow to convert a scenario into a requirement analysis document.

## Workflow

1. Locate and read scenario and UI context files.
- Read `docs/scenarios/<SCN-ID>.md`.
- Read referenced design context (for this repo, prefer `docs/figma/<project>/README.md` and `guidelines/Guidelines.md`).
- If source files are missing, stop and report exact missing paths.

2. Establish document identity.
- Keep scenario ID as-is (for example `SCN-LIB-001`).
- Assign a separate requirement document ID (for example `REQ-LIB-001`).
- Use the requirement document ID as the output filename stem: `<REQ-ID>.md`.
- Add a `文件資訊` section near the top with:
  - `需求文件 ID：<REQ-ID>`
  - `來源情境：<SCN-ID>`
  - `UI 設計來源：<FIGMA-LINK-OR-PATH>`

3. Produce requirement analysis with fixed sections.
- `1) 需求摘要`
- `2) Functional Requirements (FR)`
- `3) Non-Functional Requirements (NFR)`
- `4) Acceptance Criteria (AC, Given/When/Then)`
- `業務錯誤情境與錯誤碼需求`
- `UI → API 對照表`
- `5) 風險、假設與待確認事項`

4. Apply authoring rules.
- Use Traditional Chinese.
- Make FR/NFR/AC testable and specific.
- Capture scope, actors, preconditions, business rules, state transitions, and edge cases.
- Include at least one FR that explicitly requires frontend UI alignment to the specified Figma source.
- Include at least one AC (Given/When/Then) that verifies frontend UI alignment to the specified Figma source.
- Keep API IDs compliant with `AGENTS.md` format: `{service-name}-{resource-name-plural}-{3-digit-seq}`.
- Use business error codes and do not rely on HTTP status alone.
- Avoid introducing assumptions without marking them as pending confirmation.

5. Write output file.
- Default path: `docs/requirements/<REQ-ID>.md`.
- If the user requests another output directory, use that directory but keep the filename as `<REQ-ID>.md`. If a requested filename differs, use the canonical filename and report the resolved path.
- In the target directory, find an existing requirement document by its `需求文件 ID` metadata, not by scenario ID or filename. If exactly one matching document has a different filename, rename it to `<REQ-ID>.md` and preserve useful prior content.
- If the canonical path contains a different requirement document ID, or multiple documents in the target directory have the same requirement ID, stop and report the conflict instead of overwriting a document.
- If the canonical file exists with the matching ID, update it in place and preserve useful prior content.

6. Validate before finish.
- Ensure all mandatory sections exist.
- Ensure each AC is in Given/When/Then format.
- Ensure `需求文件 ID`, `來源情境`, and `UI 設計來源` are present.
- Ensure the output filename stem (excluding `.md`) exactly matches `需求文件 ID`.
- Ensure UI alignment FR and UI alignment AC both exist.
- Ensure business rules, state transitions, edge cases, and open questions are addressed.
- Run `python3 .codex/skills/scenario-requirements-writer/scripts/validate_requirements.py <REQ-FILE>`.
- Treat validator errors as blocking; warnings may be resolved according to the workflow gate.
- The validator checks one REQ document only. Cross-artifact traceability belongs to the workflow framework, not this skill.

7. Update workflow handoff for scenario-driven work.
- When `docs/workflows/WF-<DOMAIN>-<NNN>.md` exists, update the S1 SA status, summary, output path, open questions, the next recommended S2 Archi action, and the files the next session must read first.
- Do not create workflow state for a non-scenario request; report the missing handoff artifact instead.
- Run `npm run workflow:validate -- docs/workflows/WF-<DOMAIN>-<NNN>.md` after updating state.

## Output Contract

When reporting completion, include:
- Output file path.
- Requirement document ID.
- Source scenario ID.
- Any open questions that block implementation.
- Validation command and result.

## Reference

Use [references/requirements-template.md](references/requirements-template.md) as the base skeleton.
