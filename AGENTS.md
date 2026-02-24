# AGENTS

Version: 1.0.0
Last Updated: 2026-02-24

This file defines mandatory rules for AI Agents and developers.
All generated code MUST comply with this document.

---

## 1. Java Backend Rules

### 1.1 Naming

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

### 1.2 OOP

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

### 1.3 Exception Handling

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

### 1.4 Error Code

All APIs MUST return business error code.

| Code | Type |
| --- | --- |
| 00000 | Success |
| A0000 | Client Error |
| B0000 | System Error |
| C0000 | Third-party Error |

HTTP status code MUST NOT replace business error code.

---

## 2. Frontend Rules

---

### 2.1 Naming

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

### 2.2 Component Structure

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

### 2.3 API Calling Rules

#### MUST

- All API calls MUST use centralized API client

- No direct fetch/axios inside component

- API path MUST match OpenAPI definition

- Model MUST align with `{HttpMethod}{Resource}RequestDTO/ResponseDTO`

Example:

- GetBooksResponseDTO must match backend response schema

---

### 2.4 State Management

#### MUST

- No global mutable variable

- Use store (Pinia / Redux / equivalent)

- Store must separate:

  - state

  - action

  - getter

---

### 2.5 Error Handling

#### MUST

- All API errors MUST map to backend business error code

- No direct error message display without mapping

- UI must not depend on HTTP status only

---

### 2.6 DTO Alignment

#### MUST

- Frontend model MUST match OpenAPI schema

- No manual field renaming without adapter layer

- If transformation needed, use mapper function

---

### 2.7 Forbidden

- No hard-coded API URL

- No magic number

- No duplicated API logic

- No direct business rule inside UI template

---

## 3. OpenAPI Contract Rules

This section applies to Backend, Frontend, SA, QA.

---

### 3.1 API ID (MUST)

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

### 3.2 operationId (MUST)

- operationId MUST use path

- Must align with Stoplight maintenance style

- MUST NOT use API ID as operationId

---

### 3.3 Model Naming (MUST)

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

### 3.4 Naming Conflict (SHOULD)

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

## 4. Git Rules

### 4.1 Branch Naming

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

### 4.2 Commit Message

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

## 5. Versioning (Semantic Versioning)

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

## 6. AI Agent Constraints

AI-generated code MUST:

1. Follow all rules in this file

2. Avoid magic values

3. Handle exceptions properly

4. Respect OOP principles

5. Map all errors to business error code

6. Follow OpenAPI contract rules

7. Follow Git rules
