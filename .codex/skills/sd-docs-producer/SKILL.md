---
name: sd-docs-producer
description: Produce SD deliverables from requirements and architecture documents, including OpenAPI contract, schema docs, and per-API flow docs. Use when user asks SD to generate implementation-ready design artifacts under docs/openapi.yaml, docs/schema/, and docs/api/.
---

# SD Docs Producer

Generate system design artifacts for implementation handoff.

## Workflow

1. Read inputs.
- Read `docs/requirements/*.md`.
- Read `docs/architecture/*.md`.
- Read `.codex/agents/sd.toml` and `AGENTS.md` for naming and contract rules.

2. Lock output scope.
- Create or update `docs/openapi.yaml` as single source of API truth.
- Create or update `docs/schema/{table_name}.md` for data model details.
- Create or update `docs/api/{api_id}_{name}.md` for API flow only.

3. Define OpenAPI contract.
- Add API IDs via `x-api-id` and include API ID in summary.
- Keep `operationId` path-oriented and never use API ID as operationId.
- Use model naming rule:
  - `{HttpMethod}{ResourcePlural}RequestDTO`
  - `{HttpMethod}{ResourcePlural}ResponseDTO`
- Use unified business code in responses: `00000`, `Axxxx`, `B0000`, `C0000`.

4. Write schema docs.
- Include table purpose, DDL draft, columns, indexes, constraints.
- Include domain rules in Given/When/Then.
- Include transaction boundary notes.

5. Write API flow docs.
- Keep only main business flow and Given/When/Then.
- Include Mermaid flowchart.
- Link flow behavior to API ID and OpenAPI operation.

6. Validate before finish.
- Confirm all API IDs in requirements mapping are present.
- Confirm RequestDTO/ResponseDTO naming compliance.
- Confirm every API has success and error business code.
- Confirm docs/api files do not duplicate full API schema.

## Output Contract

When complete, report:
- Updated file list.
- API IDs covered.
- Any unresolved requirement ambiguity.

## Reference

Use [references/sd-deliverables-template.md](references/sd-deliverables-template.md) as checklist.
