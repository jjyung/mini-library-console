# AGENTS

Version: 1.0.4
Last Updated: 2026-09-14

This file defines repo-wide mandatory rules for AI Agents and developers.
Role-specific implementation rules live in the corresponding skill under
`.codex/skills/`. All work MUST comply with this file and the applicable role
skill.

---

## Global Rules

### UI Test Locator Integrity (MUST)

- All UI changes MUST preserve `data-testid` integrity.

- Locator gaps raised by verification MUST be handled by the owning
  implementation role before delivery.

### Skill-Agent Decoupling (MUST)

- Skill files under `.codex/skills/**` MUST NOT depend on `.codex/agents/*.toml`.
- Skill workflow/checklist content MUST be self-contained and MUST NOT require reading agent config files as inputs.
- Existing agents may remain for runtime/persona usage, but skills MUST work correctly when agent files are absent.
- New or updated skills MUST follow the same rule and MUST NOT add references to `.codex/agents/**`.

### Skill-Agent Binding (MUST)

- Agent config files under `.codex/agents/*.toml` MUST stay thin adapters:
  role, context routing, model, and MCP servers only.
- Every agent config file MUST be registered in `.codex/config.toml` under
  `[agents.<name>]` with its `config_file` path.
- Each agent MUST route to at least one skill using `$<skill-name>`; the skill
  owns the workflow, tool commands, output contract, and engineering rules.
- Agent config files MUST NOT embed workflow steps, deliverables, or
  engineering rules (no Markdown headings inside `developer_instructions`).
- Every skill referenced by an agent MUST exist under
  `.codex/skills/<skill-name>/SKILL.md`, and each skill MUST declare
  `name`, `description`, `version`, and `owner` frontmatter.
- Skill names MUST be lowercase kebab-case, capability-based, unique within the
  repository, and MUST NOT overlap semantically with another skill's purpose.
- Run `npm run skills:validate` after changing any agent or skill. It checks
  binding integrity, one-way decoupling, and thin-adapter shape.

### Role-specific Workflow Routing

- Role-specific implementation workflows are defined by the corresponding
  skills under `.codex/skills/`; skills own workflow procedures and
  engineering standards.
- Agent configuration files define role/context routing only; they MUST NOT be
  treated as the source of implementation procedures or engineering standards.

---

## Business Error Contract

All APIs MUST return business error code.

| Code | Type |
| --- | --- |
| 00000 | Success |
| A0000 | Client Error |
| B0000 | System Error |
| C0000 | Third-party Error |

HTTP status code MUST NOT replace business error code.

---


## OpenAPI Contract Rules

This section applies to all roles that author, consume, or verify the API
contract.

---

### API ID (MUST)

Each API MUST have a unique API ID.

Format:

```text
{service-name}-{resource-name-plural}-{3-digit-seq}

```

Rules:

- service-name: lowercase kebab-case

- resource-name: plural

- sequence: 001-999

- must be unique within same service-resource group

Example:

- library-books-001

- member-users-013

Usage:

- API ID MUST appear in API name

- API ID MUST NOT be used as operationId

---

### operationId (MUST)

- operationId MUST use path

- Must align with Stoplight maintenance style

- MUST NOT use API ID as operationId

---

### Model Naming (MUST)

All models MUST distinguish Request and Response.

Format:

```text
{HttpMethod}{ResourcePlural}RequestDTO
{HttpMethod}{ResourcePlural}ResponseDTO

```

HttpMethod:

- Get

- Post

- Put

- Patch

- Delete

Examples:

- GetBooksRequestDTO

- PostBooksResponseDTO

- PatchUsersRequestDTO

---

### Naming Conflict (SHOULD)

When conflict occurs, add qualifier:

- Admin

- Internal

- Public

- Summary

- Detail

- V2

Example:

- GetBooksDetailResponseDTO

- PostAdminBooksRequestDTO

---

## Git Rules

### Branch Naming

Format:

```text
category/issueId-description

```

Category:

- hotfix

- bugfix

- feature

- test

- wip

Rules:

- Must include issue ID

- Must not be number-only

- Keep short

- Be consistent

---

### Commit Message

Format:

```text
<type>(<scope>): <subject>

<body>

<footer>

```

Types:

- feat

- fix

- docs

- style

- refactor

- perf

- test

- chore

- revert

Rules:

- subject ≤ 50 characters

- each body line ≤ 72 characters

- explain why and impact

- include issue in footer

---

## Versioning (Semantic Versioning)

Format:

```text
MAJOR.MINOR.PATCH

```

Rules:

- MAJOR → incompatible API changes

- MINOR → backward compatible feature

- PATCH → backward compatible fix

- No leading zero

- Released version MUST NOT be modified

Pre-release:

- alpha

- beta

- rc

Example:

- 1.0.0

- 1.1.0

- 2.0.0

- 1.0.0-alpha

- 1.0.0-rc.1

---

## AI Agent Constraints

AI-generated work MUST:

1. Follow this file and the applicable role-specific skill.

2. Avoid magic values and duplicated logic.

3. Map API errors to the business error contract.

4. Follow the OpenAPI, Git, and versioning rules.

5. Keep skills decoupled from `.codex/agents/**`.

---

## Workflow orchestration rules

This repository follows a **stage-gated, artifact-driven workflow** aligned
with role boundaries.

### Role order

Unless explicitly overridden, scenario delivery should follow:

```text
SA -> Archi -> SD -> PG -> FE/BE -> QA
```

Do not collapse multiple decision layers into a single uncontrolled step.

### Workflow state is mandatory

For each scenario-driven implementation, maintain a workflow state file:

```text
docs/workflows/WF-<DOMAIN>-<NNN>.md
```

Create it from `docs/templates/workflow-state-template.md`. This file is the
handoff artifact across sessions and MUST record the current stage, stage
status, current objective, blockers and open questions, parallel FE/BE plan,
QA loop status, and next-session reading order.

### Required startup check for any agent

Before making changes, agents should inspect:

1. `README.md`
2. `AGENTS.md`
3. related scenario file under `docs/scenarios/`
4. corresponding workflow state under `docs/workflows/`
5. upstream artifacts required by the current stage

### Stage gates and handoff

- An agent may advance a workflow only when the current stage artifacts are
  sufficient to support the next role.
- FE and BE may run in parallel only after PG has produced a stable
  implementation plan and SD artifacts are sufficient for parallel delivery;
  both MUST stay aligned on the API contract, shared terminology, acceptance
  criteria, and scenario scope.
- QA rework MUST route by defect type: implementation -> FE/BE, contract -> SD,
  design -> Archi, requirement -> SA. Do not loop indefinitely; escalate after
  repeated failures.
- If required information is missing, stop and ask clarifying questions, or
  explicitly document assumptions before proceeding.
- At the end of any substantial action, update the workflow state with the
  latest completed step, current status, next recommended action,
  blockers/questions, and files the next session should read first.

The operational procedure for these rules is owned by the
`workflow-orchestration` skill; this section defines only the repo-wide
obligations.
