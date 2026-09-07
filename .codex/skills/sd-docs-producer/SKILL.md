---
name: sd-docs-producer
description: Produce SD deliverables from requirements and architecture documents, including OpenAPI contract, global error-code definitions, schema docs, and per-API flow docs. Use when user asks SD to generate implementation-ready design artifacts under docs/openapi.yaml, docs/error-codes.md, docs/schema/, and docs/api/.
---

# SD Docs Producer

Generate system design artifacts for implementation handoff.

## Workflow

1. Read inputs.
- Read `docs/requirements/*.md`.
- Read `docs/architecture/*.md`.
- Read `AGENTS.md` for naming and contract rules.
- Resolve the target environment from the architecture deployment matrix or
  explicit task context before adding environment-specific integration rules.
- Apply built-in SD boundaries:
  - Only edit design deliverables under `docs/openapi.yaml`, `docs/error-codes.md`,
    `docs/schema/*.md`, and `docs/api/*.md`.
  - Do not modify requirement documents or architecture decision scope.
  - Keep implementation-level details in design docs only, not source-code changes.
  - Ensure business error-code mapping is consistent with requirements.

## Environment-specific CORS Shortcut

When the target environment is explicitly `dev`, `poc`, or `test`
(case-insensitive), apply the following development shortcut:

- Treat CORS as non-blocking for the SD handoff so downstream PG/FE/BE work can
  proceed without a CORS-specific implementation task.
- Record the decision in the relevant handoff or API flow notes as
  `CORS: ignored for <environment> to accelerate development`.
- Do not add strict origin allowlist, preflight, credential, or CORS acceptance
  criteria for that environment unless the requirements explicitly demand one.
- Keep this as an environment-scoped configuration decision; do not hardcode a
  permissive CORS policy into the API contract or production behavior.

For staging, production, or an unspecified environment, do not use this
shortcut. Define and document the required CORS origins, methods, headers,
credentials, and preflight behavior, and flag the missing environment as an
ambiguity when it cannot be resolved from the upstream artifacts.

2. Lock output scope.
- Create or update `docs/openapi.yaml` as single source of API truth.
- Create or update `docs/error-codes.md` as the global business error-code
  definition referenced by all APIs.
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
- Create one physical table document per `docs/schema/{table_name}.md`.
- Use [references/user-schema-template.md](references/user-schema-template.md)
  as the minimum structure: modification history, Schema 2.3 column dictionary,
  Schema 2.4 constraints, and DDL.
- Add further sections only when the upstream requirements or architecture
  artifacts explicitly require them.

5. Write API flow docs.
- Create one `docs/api/{api_id}_{name}.md` file per API.
- Use [references/api-flow-docs-template.md](references/api-flow-docs-template.md)
  as the baseline.
- Describe the main workflow with a Mermaid sequence diagram focused on the
  application service business logic.
- Document only the `Execute Business Logic` content. Request validation and
  generated DTO constraints belong in `docs/openapi.yaml` and must not be
  redefined in API flow docs.
- Document the detailed business logic and Given/When/Then behavior in that
  single section.
- Reference `docs/error-codes.md` for global code definitions. In the API flow
  document, only map this API's actual triggers, response behavior, and
  retryability.
- Link flow behavior to the API ID and OpenAPI operation without duplicating the
  full OpenAPI schema.

6. Validate before finish.
- Confirm all API IDs in requirements mapping are present.
- Confirm RequestDTO/ResponseDTO naming compliance.
- Confirm every API has success and error business code.
- Confirm docs/api files do not duplicate full API schema.
- Confirm the CORS shortcut is recorded only for an explicit `dev`, `poc`, or
  `test` target; otherwise confirm that the CORS policy is documented or the
  ambiguity is reported.
- Run `scripts/validate_sd_artifacts.js` from the repository root after the
  deliverables are written. Use `npm run sd:validate -- ...` when the root
  package command is available.
- Treat validator errors as a release-blocking S3 exit-gate. Treat warnings as
  review items, or use `--strict` when the delivery requires zero warnings.
- The validator checks deterministic structure, references, naming, required
  sections, and cross-document API ID/path mapping. It does not decide whether
  business rules, architecture trade-offs, transaction boundaries, retry
  policies, or requirement intent are semantically correct.

## Output Contract

When complete, report:
- Updated file list.
- API IDs covered.
- Any unresolved requirement ambiguity.
- Validator result, including error/warning counts and the command used.

## Reference

Use [references/sd-deliverables-template.md](references/sd-deliverables-template.md) as checklist.
Use [references/user-crud-openapi.yaml](references/user-crud-openapi.yaml) as the
canonical baseline when requirements include a user CRUD resource. Adapt fields,
authorization rules, and business behavior to the upstream artifacts while
preserving the API ID, DTO, response-envelope, and error-code conventions.
Use [references/user-schema-template.md](references/user-schema-template.md) as the
baseline for one-table-per-file schema documents. Preserve the change history,
Schema 2.3 column dictionary, Schema 2.4 constraints, and DDL structure unless
the upstream artifacts require a different structure.
Use [references/api-flow-docs-template.md](references/api-flow-docs-template.md)
as the baseline for one-API-per-file flow documents. Preserve the history,
sequence diagram, step logic, and dedicated error-code chapter unless the
upstream artifacts require a different structure.
Use [references/error-code-definition.md](references/error-code-definition.md) as
the baseline for `docs/error-codes.md`. Keep it as the global source of truth;
individual API documents should reference it instead of redefining global code
semantics.
