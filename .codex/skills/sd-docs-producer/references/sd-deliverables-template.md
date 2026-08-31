# SD Deliverables Checklist

## Inputs

- [ ] `docs/requirements/*.md`
- [ ] `docs/architecture/*.md`
- [ ] `AGENTS.md`
- [ ] Follow built-in SD boundaries defined in `sd-docs-producer/SKILL.md`

## OpenAPI (`docs/openapi.yaml`)

- [ ] API IDs with `x-api-id`
- [ ] API summary contains API ID
- [ ] `operationId` path-oriented, not API ID
- [ ] `*RequestDTO` and `*ResponseDTO` naming compliant
- [ ] Business code model included (`00000`, `Axxxx`, `B0000`, `C0000`)
- [ ] Error responses defined per requirements

## Schema Docs (`docs/schema/*.md`)

- [ ] One physical table per Markdown file named `docs/schema/{table_name}.md`
- [ ] Modification history includes who, when, why, and what changed
- [ ] Section 2 contains only Schema 2.3 and Schema 2.4
- [ ] Schema 2.3 column dictionary includes type, nullability, default, mapping, and description
- [ ] Schema 2.4 documents constraints and violation handling
- [ ] DDL draft includes applicable keys, constraints, and indexes

## Error Code

- [ ] Create or update the global definition at `docs/error-codes.md`
- [ ] Global document uses [references/error-code-definition.md](references/error-code-definition.md) as its baseline
- [ ] Modification history includes who, when, why, and what changed
- [ ] Defines `00000`, `A0000`, `B0000`, and `C0000`
- [ ] States that HTTP status does not replace business code
- [ ] Defines the shared error response envelope and `traceId` rule
- [ ] API flow documents reference the global definition and only add API-specific mapping

## API Flow Docs (`docs/api/*.md`)

- [ ] File name uses `{api_id}_{name}.md`
- [ ] Modification history includes who, when, why, and what changed
- [ ] Contains API ID and path
- [ ] Workflow includes a Mermaid sequence diagram
- [ ] Only `Execute Business Logic` is documented after OpenAPI-generated constraints
- [ ] `Execute Business Logic` has detailed logic and Given/When/Then rules
- [ ] Error-code chapter references the global definition and maps API-specific behavior
- [ ] No duplicated full OpenAPI schema
