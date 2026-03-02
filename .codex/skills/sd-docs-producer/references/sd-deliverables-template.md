# SD Deliverables Checklist

## Inputs

- [ ] `docs/requirements/*.md`
- [ ] `docs/architecture/*.md`
- [ ] `AGENTS.md`
- [ ] `.codex/agents/sd.toml`

## OpenAPI (`docs/openapi.yaml`)

- [ ] API IDs with `x-api-id`
- [ ] API summary contains API ID
- [ ] `operationId` path-oriented, not API ID
- [ ] `*RequestDTO` and `*ResponseDTO` naming compliant
- [ ] Business code model included (`00000`, `Axxxx`, `B0000`, `C0000`)
- [ ] Error responses defined per requirements

## Schema Docs (`docs/schema/*.md`)

- [ ] Table purpose
- [ ] DDL draft
- [ ] Columns and constraints
- [ ] Index strategy
- [ ] Given/When/Then rules
- [ ] Transaction boundary notes

## API Flow Docs (`docs/api/*.md`)

- [ ] File name uses `{api_id}_{name}.md`
- [ ] Contains API ID and path
- [ ] Mermaid main flow
- [ ] Given/When/Then rules
- [ ] No duplicated full OpenAPI schema
