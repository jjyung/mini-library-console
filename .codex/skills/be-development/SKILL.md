---
name: be-development
description: Implement assigned Java/Spring backend tasks from SD artifacts. Use Maven POM plugins for OpenAPI Generator, keep generated API code under source control without manual edits, apply proportionate layered architecture, and validate with TDD.
---

# BE Development

Implement only the assigned backend scope from the frozen SD artifacts. Keep
the API contract, generated boundary code, business logic, persistence,
messaging, migrations, tests, and handoff traceable to the task and acceptance
criteria.

## Repository backend standards

Apply these Java and backend rules to all BE changes:

### Naming

- Do not start or end identifiers with `_` or `$`.
- Do not use Chinese naming, Pinyin-English mixed naming, or discriminatory
  words; use `allowList` / `blockList` terminology.
- Classes use UpperCamelCase. Abstract classes use the `Abstract` or `Base`
  prefix, exception classes use the `Exception` suffix, and test classes use
  the `ClassNameTest` form.
- Methods and variables use lowerCamelCase. Do not use single-letter
  variables; choose meaningful names.
- Constants use `UPPER_CASE_WITH_UNDERSCORE`. Avoid magic values. Suffix
  `L`, `F`, or `D` on long, float, or double literals as applicable.
- Enum classes use the `Enum` suffix and enum members use upper case.
- Use `getXxx` for a single object, `listXxx` for multiple objects, and
  `countXxx` for counts. Implementation classes use the `Impl` suffix.
- Declare arrays as `int[] array`. Boolean POJO fields MUST NOT start with
  `is`.

### Object design

- All fields MUST be private, and classes MUST provide getters/setters.
- Do not call overridable methods from constructors.
- Prefer composition, focused responsibilities, interfaces, and polymorphism
  over deep inheritance or large conditional dispatch.
- Keep method parameter count at five or fewer.

### Exception handling

- Do not use empty catches or catch `Exception` / `Throwable` directly.
- Handle or declare every exception.
- Preserve the original exception when wrapping it and log failures with a
  stack trace.
- Use try-with-resources for resource handling.

For example:

```java
throw new MyBusinessException("message", cause);
```

## 1. Load the handoff and freeze scope

Before editing code, read:

- `README.md`
- `AGENTS.md`
- the related `docs/scenarios/SCN-*.md` when scenario-driven
- the corresponding `docs/workflows/WF-*.md`
- the assigned `docs/tasks/*.md`
- `docs/openapi.yaml` for API work
- the relevant `docs/error-codes.md`, `docs/schema/*.md`, and `docs/api/*.md`
- the current `pom.xml`, Maven profiles, and build scripts

Confirm that the task is assigned to BE, the PG handoff is stable, the SD
artifacts are implementation-ready, and the acceptance criteria are clear.
The normal code scope is `apps/api/**`; update `docs/tasks/**` only for
progress, blockers, or the requested delivery summary. Do not silently modify
requirements, architecture, OpenAPI, schema, or API-flow documents to make
source code fit.

For a scenario-driven task, a missing workflow state is a handoff blocker. Ask
for it to be initialized or document the explicit assumption before broad
changes. At the end of a substantial action, update the workflow state with the
completed step, current status, next action, blockers/questions, and files for
the next session.

## 2. API work: Maven POM and OpenAPI Generator are the source of truth

Treat a task as API work when it adds or changes a path, HTTP method, request or
response schema, status/error behavior, or an API interface implementation.
The following gate is mandatory before controller implementation:

1. Inspect `docs/openapi.yaml`. Confirm the relevant path, `operationId`,
   `x-api-id`, RequestDTO/ResponseDTO schemas, constraints, and business-code
   responses match the SD API flow.
2. Confirm the `pom.xml` declares the
   `org.openapitools:openapi-generator-maven-plugin`. Its version must be
   pinned in the POM or a POM property; never use a floating or ad hoc CLI
   version. The generator name, input spec, output directory, package names,
   and additional properties must be reviewable from the POM/configuration.
3. Run the POM-configured generator through the repository's deterministic
   entry point, normally `npm run api:generate`. That script must delegate to
   the API module Maven wrapper with `generate-sources` and activate only the
   POM-defined generation profile when one is used; it must not contain a
   second generator configuration or bypass the POM with a separate CLI.
4. Keep generated source and generated resources in sync, inspect the diff,
   and ensure the generated output is in the tracked source directory agreed by
   the project. A target-only output that is deleted by `mvn clean` does not
   satisfy the generated-code-in-Git policy.
5. Compile the generated code and confirm the generated API interface and
   boundary DTOs are the only source for that API's interface and public
   request/response models.

Generator and contract versions must be reproducible and version-controlled
together:

- pin the OpenAPI Generator Maven plugin version in `pom.xml`;
- version `docs/openapi.yaml` using its `info.version` and the repository's
  change-history convention;
- record generator-version changes and contract-version changes in the same
  task/handoff when they are part of one delivery;
- do not assume the numbers must be identical: generator version and API
  contract version are different artifacts, but neither may be implicit.

Generated interfaces, DTOs, annotations, and generated resources are outputs,
not hand-authored source. Generated code must be committed to Git and must not
be manually edited. Implement the generated interface in the controller or
inbound adapter and put business behavior in application/domain code. If
internal domain types are needed, use an explicit mapper at the boundary; do
not create a second public DTO that copies or renames the generated model.

If the POM plugin, pinned version, input spec, output policy, or Maven sync
command is missing, stop the API gate and report the exact discovery result.
Recommend the smallest reproducible POM configuration and wait for human
authorization before changing build configuration. Do not substitute a local
OpenAPI CLI invocation.

If the OpenAPI contract is incomplete or conflicts with the API flow, stop at
the contract boundary. Notify SD and the human owner with the affected API ID,
the conflict, impact, and a concrete proposed correction. Wait for SD to
modify the contract and for human approval before resuming; do not edit
`docs/openapi.yaml` or generated files as an implementation shortcut.

For non-API work, load the relevant schema, API flow, task, and workflow
artifacts, then skip only the generator gate.

## 3. Select the smallest architecture that fits

Use the architecture selected by Archi and recorded in the architecture
artifact. If that decision is missing or no longer fits the task, report a
recommendation and wait for human/Archi confirmation before expanding the
architecture. Record the final choice and rationale in the task summary.

### Three-layer architecture

Use the simple synchronous default when the use case is request/response, has
one primary persistence boundary, and has no meaningful external messaging or
integration orchestration:

- `controller`: HTTP boundary and generated interface implementation only;
  translate transport concerns and delegate.
- `service`: application use case, transaction boundary, business rules, and
  business-error translation.
- `dao`: persistence abstraction and implementation; keep database mechanics
  out of the service.

The controller must not contain business decisions. The DAO must not decide
HTTP behavior. Preserve the generated API response shape and keep mapping at
clear boundaries.

### Clean / hexagonal architecture

Use a clean or hexagonal / ports-and-adapters shape when the domain contains
substantial invariants, multiple inbound or outbound adapters, asynchronous
work, MQ integration, retry/dead-letter semantics, idempotency, ordering,
multiple persistence technologies, or meaningful technology substitution
needs. MQ alone does not justify a large redesign if it is only a trivial
side-effect.

Use explicit dependency direction:

1. inbound adapters: REST controllers, MQ consumers, or scheduled triggers;
2. inbound/application ports: use-case interfaces exposed to adapters;
3. application/domain core: business rules and orchestration, independent of
   Spring transport and vendor clients;
4. outbound ports: persistence, message publishing, and external-service
   interfaces;
5. outbound adapters: DAO/repository, MQ producer, and external-service
   implementations.

Keep framework and vendor details at the edges. Define transaction, delivery,
retry, dead-letter, idempotency, ordering, and failure semantics before coding
an MQ flow. If the project uses “five-angle” to mean a different structure,
ask for its definition instead of inventing a package layout.

## 4. Persistence and Liquibase migrations

When a task changes a database schema, use Liquibase with formatted SQL
changelogs. Do not create Liquibase XML changelogs. Keep migration files in the
repository's established resource directory, typically under
`src/main/resources/db/changelog/`, and follow the configured Maven/Spring
Liquibase entry point.

Each migration must have an immutable changeset identity, an explicit author,
deterministic ordering, and a rollback strategy appropriate to the change.
Never rewrite an applied changeset; add a new SQL changeset. Keep DDL in the
SQL migration and keep business behavior in Java. Add integration coverage for
constraints, indexes, defaults, and migration-sensitive behavior where the
repository supports it.

If the project has no Liquibase configuration or the requested change needs a
database/product decision not present in SD, stop and report the missing POM,
runtime, environment, or rollback decision. Ask for human authorization before
adding the dependency or changing migration policy. Do not fall back to XML.

## 5. Implement with tests and appropriate patterns

Use TDD for each behavior: write a failing test, implement the smallest
change, then refactor without changing behavior.

At minimum, cover the layer where behavior lives and the boundary that can
break:

- service/domain unit tests for happy paths, invariants, and business errors;
- controller/API integration tests for generated contract binding, validation,
  response envelope, and business-code mapping;
- DAO/database integration tests for constraints and transaction behavior;
- MQ consumer/producer or contract tests for serialization, retry,
  idempotency, duplicate delivery, and failure behavior when messaging is in
  scope;
- migration integration tests when a schema change can affect runtime behavior.

Use design patterns only when they remove a concrete source of variation or
isolate an integration boundary:

- Strategy for interchangeable policies;
- Factory for controlled creation of variants;
- Adapter for persistence, MQ, or third-party clients;
- Repository/DAO for persistence abstraction;
- Facade/application service for a stable use-case boundary;
- Outbox/idempotency patterns only when SD defines the delivery semantics.

Prefer composition, interfaces, and polymorphism. Keep classes focused, avoid
large conditional business dispatch, keep method parameters at five or fewer,
and avoid deep inheritance. Follow the backend standards in this skill and the
repo-wide business-error contract in `AGENTS.md`; HTTP status never replaces a
business code.

Leave comments around core business invariants and non-obvious decisions:

- state the rule and why it exists;
- reference the relevant API ID, acceptance criterion, or error code when
  useful;
- explain concurrency, ordering, retry, or transaction assumptions;
- never add comments inside generated code or narrate obvious statements.

## 6. Validate and inspect the result

Run the narrowest relevant checks first, then the repository backend check. In
this repository the preferred backend check is:

```bash
npm run check:api
```

For API work, also run `npm run api:verify-generated -- --generated-path
<tracked-generated-source> [--generated-path <tracked-generated-resource>]`.
That entry point runs the POM generator and checks the generated paths against
Git. Then verify:

- the pinned generator plugin ran successfully;
- generated interfaces, DTOs, and resources are current and compile;
- generated output is committed and has no manual edits;
- no duplicate hand-written API interface or public boundary DTO exists;
- implementation matches the OpenAPI operation and API flow;
- every result maps to the required business code;
- relevant tests pass.

For a complete backend check, run `npm run backend:check`. For a scenario
handoff, run `npm run workflow:validate -- docs/workflows/WF-<DOMAIN>-<NNN>.md`.
These scripts validate and execute deterministic work only; they do not choose
an architecture, edit upstream artifacts, advance workflow stages, or replace
the SD/human approval gate.

For database work, run `npm run db:validate -- --changelog
apps/api/<service>/src/main/resources/db/changelog` and then verify Liquibase
changeset identity, ordering, rollback, and migration-sensitive tests. For MQ work, verify the
declared acknowledgment, retry, dead-letter, ordering, duplicate-delivery, and
transaction behavior. Report environment failures separately from product
defects, including the exact command and actionable remediation.

## 7. Handoff, human-in-the-loop, and questions

Before finishing, report:

- implemented scope and files changed;
- API IDs, POM generator command/version, and contract version used;
- generated files committed and confirmation that none were manually edited;
- architecture choice and the complexity signals behind it;
- Liquibase SQL migrations and rollback notes, when applicable;
- tests and validation commands with results;
- assumptions, risks, unresolved blockers, and recommendations needing
  approval.

Ask focused questions instead of guessing when any of these are missing:

- assigned scope, acceptance criterion, or upstream artifact;
- API contract, DTO shape, error code, generator POM, or version decision;
- MQ delivery semantics or the intended meaning of pentagonal architecture;
- persistence/transaction/rollback decision that changes behavior;
- permission to notify SD, alter the POM, add a dependency, change migration
  policy, or expand scope.

When a contract or architecture change is needed, notify SD and the human owner
with the proposed change and impact, then pause until the human-in-the-loop
decision is explicit. Route requirement ambiguity to SA, architecture gaps to
Archi, contract gaps to SD, and implementation defects to BE/FE as appropriate.
