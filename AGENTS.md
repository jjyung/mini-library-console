# AGENTS

Version: 1.0.1
Last Updated: 2026-03-04

This file defines mandatory rules for AI Agents and developers.
All generated code MUST comply with this document.

---

## Global Rules

### UI Test Locator Integrity (MUST)

- All UI changes MUST preserve `data-testid` integrity.

- If QA raises locator requirements, PG MUST prioritize handling them.

### Skill-Agent Decoupling (MUST)

- Skill files under `.codex/skills/**` MUST NOT depend on `.codex/agents/*.toml`.
- Skill workflow/checklist content MUST be self-contained and MUST NOT require reading agent config files as inputs.
- Existing agents may remain for runtime/persona usage, but skills MUST work correctly when agent files are absent.
- New or updated skills MUST follow the same rule and MUST NOT add references to `.codex/agents/**`.

---

## Java Backend Rules

### Naming

#### MUST

- No `_` or `$` at start or end of identifiers

- No Chinese naming

- No Pinyin-English mixed naming

- No discriminatory words (use allowList / blockList)

#### Class

- UpperCamelCase

- Abstract class: prefix `Abstract` or `Base`

- Exception class: suffix `Exception`

- Test class: `ClassNameTest`

#### Method / Variable

- lowerCamelCase

- No single-letter variable names

- Use meaningful names

#### Constant

- UPPER_CASE_WITH_UNDERSCORE

- No magic values

- long must use `L`

- float/double must use `F` / `D`

#### Enum

- Class name suffix `Enum`

- Members UPPER_CASE

#### Service / DAO

- getXxx → single object

- listXxx → multiple objects

- countXxx → count

- Implementation class suffix `Impl`

#### Other

- Array declaration: `int[] array`

- Boolean field in POJO MUST NOT start with `is`

---

### OOP

#### MUST

- All fields `private`

- Provide getter/setter

- No calling overridable methods in constructor

- Avoid deep inheritance

- Prefer composition

- Single Responsibility Principle

- Method parameter count ≤ 5

- Use interface + polymorphism

- Avoid large if-else for business logic

---

### Exception Handling

#### MUST

- No empty catch

- No catching `Exception` or `Throwable` directly

- All exceptions must be handled or declared

- Preserve original exception when wrapping

- Log with stack trace

- Use try-with-resources for resource handling

Example:

```java
throw new MyBusinessException("message", e);
```

---

### Error Code

All APIs MUST return business error code.

| Code | Type |
| --- | --- |
| 00000 | Success |
| A0000 | Client Error |
| B0000 | System Error |
| C0000 | Third-party Error |

HTTP status code MUST NOT replace business error code.

---

## Frontend Rules

---

### Naming

#### MUST

- Use camelCase for variables and functions

- Use PascalCase for components

- No Chinese naming

- No Pinyin-English mixed naming

Example:

- BookListPage

- fetchBooks

- userProfileStore

---

### Component Structure

#### MUST

- One component per file

- File name MUST match component name

- Component MUST have single responsibility

- Avoid business logic inside UI component

Business logic MUST be placed in:

- service layer

- composable / hook

- store

---

### API Calling Rules

#### MUST

- All API calls MUST use centralized API client

- No direct fetch/axios inside component

- API path MUST match OpenAPI definition

- Model MUST align with `{HttpMethod}{Resource}RequestDTO/ResponseDTO`

Example:

- GetBooksResponseDTO must match backend response schema

---

### State Management

#### MUST

- No global mutable variable

- Use store (Pinia / Redux / equivalent)

- Store must separate:

  - state

  - action

  - getter

---

### Error Handling

#### MUST

- All API errors MUST map to backend business error code

- No direct error message display without mapping

- UI must not depend on HTTP status only

---

### DTO Alignment

#### MUST

- Frontend model MUST match OpenAPI schema

- No manual field renaming without adapter layer

- If transformation needed, use mapper function

---

### Forbidden

- No hard-coded API URL

- No magic number

- No duplicated API logic

- No direct business rule inside UI template

---

### Figma UI Alignment

#### MUST

- Frontend implementation MUST align with design references under `docs/figma/*`.

- `docs/figma/*` MAY contain multiple requirement batches and segmented screens; FE MUST follow the target requirement's corresponding Figma folder(s).

- When visual/style conflicts exist across different `docs/figma/*` batches, FE MUST follow the latest requirement-scoped Figma source and document the chosen reference in delivery notes.

- UI alignment MUST NOT break existing `data-testid` locators.

---

## OpenAPI Contract Rules

This section applies to Backend, Frontend, SA, QA.

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

AI-generated code MUST:

1. Follow all rules in this file

2. Avoid magic values

3. Handle exceptions properly

4. Respect OOP principles

5. Map all errors to business error code

6. Follow OpenAPI contract rules

7. Follow Git rules

8. Keep skills decoupled from `.codex/agents/**`

---

## Workflow orchestration rules

This repository follows a **stage-gated, artifact-driven workflow** aligned with role boundaries.

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

Agents must treat this file as the handoff artifact across sessions.

Each workflow state file should include at least:

- workflow metadata
- current stage
- workflow graph
- current objective
- stage-by-stage status
- blockers and open questions
- parallel FE/BE plan
- QA loop status
- session handoff notes

### Required startup check for any agent

Before making changes, agents should inspect:

1. `README.md`
2. `AGENTS.md`
3. related scenario file under `docs/scenarios/`
4. corresponding workflow state under `docs/workflows/`
5. upstream artifacts required by the current stage

### Stage transition rule

An agent may move a workflow to the next stage only when the current stage has enough artifact quality to support the next role.

If required information is missing, the agent must:

- stop and ask clarifying questions, or
- explicitly document assumptions before proceeding

### Parallel FE/BE rule

FE and BE may run in parallel only after PG has produced a stable implementation plan and SD artifacts are sufficient for parallel delivery.

Agents must keep FE/BE aligned on:

- API contract
- shared terminology
- acceptance criteria
- scenario scope

### Auto QA loop rule

QA may trigger automatic rework loops, but the rework target must match the defect type:

- implementation issue -> FE/BE
- contract issue -> SD
- design issue -> Archi
- requirement issue -> SA

Do not keep looping indefinitely. Escalate after repeated failures.

### Handoff requirement

At the end of any substantial agent action, update the workflow state file with:

- latest completed step
- current status
- next recommended action
- blockers/questions
- files the next session should read first
