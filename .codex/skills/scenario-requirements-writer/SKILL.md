---
name: scenario-requirements-writer
description: Analyze scenario documents and related Figma export materials, then generate a structured requirements analysis file with FR/NFR/AC, risk items, business error-code mapping, and traceability metadata. Use when user asks to analyze a scenario (e.g., SCN-*) and produce or update a requirement document (e.g., REQ-*).
---

# Scenario Requirements Writer

Execute this workflow to convert a scenario into a requirement analysis document.

## Workflow

1. Locate and read scenario and UI context files.
- Read `docs/scenarios/<SCN-ID>.md`.
- Read referenced design context (for this repo, prefer `docs/figma-make/<project>/README.md` and `guidelines/Guidelines.md`).
- If source files are missing, stop and report exact missing paths.

2. Establish document identity and traceability.
- Keep scenario ID as-is (for example `SCN-LIB-001`).
- Assign a separate requirement document ID (for example `REQ-LIB-001`).
- Add a `文件資訊` section near the top with:
  - `需求文件 ID：<REQ-ID>`
  - `來源情境：<SCN-ID>`
  - `UI 設計來源：<FIGMA-LINK-OR-PATH>`

3. Produce requirement analysis with fixed sections.
- `1) 需求摘要`
- `2) Functional Requirements (FR)`
- `3) Non-Functional Requirements (NFR)`
- `4) Acceptance Criteria (AC, Given/When/Then)`
- `錯誤碼（業務層）`
- `UI → API 對照表`
- `5) 風險與待確認事項`

4. Apply authoring rules.
- Use Traditional Chinese.
- Make FR/NFR/AC testable and specific.
- Include at least one FR that explicitly requires frontend UI alignment to the specified Figma source.
- Include at least one AC (Given/When/Then) that verifies frontend UI alignment to the specified Figma source.
- Keep API IDs compliant with `AGENTS.md` format: `{service-name}-{resource-name-plural}-{3-digit-seq}`.
- Use business error codes and do not rely on HTTP status alone.
- Avoid introducing assumptions without marking them as pending confirmation.

5. Write output file.
- Default path: `docs/requirements/<SCN-ID>.md` unless user requests another path.
- If file exists, update in place and preserve useful prior content.

6. Validate before finish.
- Ensure all mandatory sections exist.
- Ensure each AC is in Given/When/Then format.
- Ensure `需求文件 ID`, `來源情境`, and `UI 設計來源` are present.
- Ensure UI alignment FR and UI alignment AC both exist.

## Output Contract

When reporting completion, include:
- Output file path.
- Requirement document ID.
- Source scenario ID.
- Any open questions that block implementation.

## Reference

Use [references/requirements-template.md](references/requirements-template.md) as the base skeleton.
