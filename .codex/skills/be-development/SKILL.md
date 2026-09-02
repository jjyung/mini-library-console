---
name: be-development
description: Implement assigned Java/Spring backend tasks from SD artifacts, syncing API interfaces and DTOs from OpenAPI Generator, choosing a proportionate layered architecture, and validating with tests. Use for BE implementation work, not for authoring requirements, architecture, or API contracts.
---

# BE Development

Implement the assigned backend scope from the frozen SD artifacts. Keep the API
contract, generated boundary code, business logic, persistence, messaging, and
tests traceable to the task and acceptance criteria.

## 1. Load and freeze the handoff context

Before editing code, read:

- `README.md`
- `AGENTS.md`
- the related `docs/scenarios/SCN-*.md` when this is scenario-driven
- the corresponding `docs/workflows/WF-*.md` when it exists
- the assigned `docs/tasks/*.md`
- `docs/openapi.yaml` for API work
- the relevant `docs/error-codes.md`, `docs/schema/*.md`, and `docs/api/*.md`

Confirm that the PG task is assigned, the SD artifacts are implementation-ready,
and the acceptance criteria are unambiguous. The implementation scope is
`apps/api/**`; update `docs/tasks/**` only for progress, blockers, or the
requested delivery summary. Do not silently modify requirements, architecture,
OpenAPI, schema, or API-flow documents to make the code fit.

For a scenario-driven task, a missing workflow state is a handoff blocker. Ask
for the workflow to be initialized or explicitly document the assumption before
making broad changes. At the end of a substantial action, update the workflow
state with the completed step, current status, next action, blockers/questions,
and files the next session should read first.

## 2. Decide whether the task changes an API

Treat a task as API work when it adds or changes a path, HTTP method, request or
response schema, status/error behavior, or an API interface implementation.

For API work, pass the generator gate before writing controller implementation:

1. Inspect `docs/openapi.yaml` and confirm the relevant path, `operationId`,
   `x-api-id`, RequestDTO/ResponseDTO schemas, business-code responses, and
   required constraints are present and consistent with the SD API flow.
2. Discover the repository's configured OpenAPI Generator source, version,
   config, output directory, and sync command. Check the build file, generator
   config, package/build scripts, and documented commands before inventing a
   command.
3. Run the repository-defined OpenAPI Generator sync/generate command. If the
   project exposes `openapi-generator sync`, use that entry point; otherwise
   use its configured equivalent (for example a Maven/Gradle/plugin or package
   script task). Keep the generated source and generated resources in sync,
   inspect the diff, and use the project's pinned version/configuration where
   available.
4. Confirm the generated API interface and boundary DTOs compile and are the
   only source for that API's interface and request/response models.

Generated interfaces, DTOs, annotations, and resources are outputs, not hand
authored source. Never edit them to fix a business rule or contract mismatch.
Implement the generated interface in the controller/adaptor and put business
behavior in application/domain code. If internal domain types are needed,
introduce an explicit mapper at the boundary; do not create a second public
DTO with a renamed copy of the generated model.

If no reproducible OpenAPI Generator configuration or sync command exists, stop
the API implementation gate and report:

- what source/configuration was searched;
- the missing command or version decision;
- a recommendation for a pinned, repeatable generator setup; and
- whether the user wants that build/configuration change authorized.

If the OpenAPI contract is incomplete or conflicts with the API flow, stop at
the contract boundary and ask SD/the user to resolve it. Do not patch
`docs/openapi.yaml` as part of ordinary BE implementation.

For non-API work, still load the relevant schema, API flow, task, and workflow
artifacts, then skip only the generator gate.

## 3. Choose the smallest architecture that fits

Record the decision and its reason in the task summary or handoff notes. Follow
existing package conventions when they do not conflict with the SD design.

### Three-layer architecture for simple synchronous work

Use this default when the use case is request/response, has one primary
persistence boundary, and has no meaningful external messaging or integration
orchestration:

- `controller`: HTTP boundary and generated interface implementation only;
  translate transport concerns and delegate.
- `service`: application use case, transaction boundary, business rules, and
  business-error translation.
- `dao`: persistence abstraction and implementation; keep database mechanics
  out of the service.

The controller must not contain business decisions. The DAO must not decide
HTTP behavior. Keep mapping at clear boundaries and preserve the API response
shape generated from OpenAPI.

### Five-part / pentagonal architecture when boundaries are real

Use a pentagonal shape when MQ or other integrations create independent
inbound/outbound boundaries, especially when the flow needs asynchronous
processing, retry/dead-letter behavior, idempotency, ordering, multiple
adapters, or transaction coordination. MQ alone does not force a rewrite if it
is merely a trivial side effect.

Use the following responsibilities as the minimum shape; adapt package names to
the codebase:

1. inbound adapters: REST controllers, MQ consumers, or scheduled triggers;
2. inbound/application ports: use-case interfaces exposed to adapters;
3. application/domain core: business rules and orchestration, independent of
   Spring transport and vendor clients;
4. outbound ports: persistence, message publishing, and external-service
   interfaces;
5. outbound adapters: DAO/repository, MQ producer, and external-service
   implementations.

Keep framework and vendor details at the edges. Define the transaction,
delivery, retry, idempotency, and failure semantics before implementing an MQ
flow. If “pentagonal” has a project-specific meaning not supplied by SD, ask
for that definition rather than assuming a package layout.

Do not introduce a second architecture merely to make a small feature appear
more abstract. If the choice is material and SD did not decide it, present the
recommended architecture, alternatives, affected files, and migration cost;
ask before expanding the scope.

## 4. Implement with design discipline

Use design patterns when they remove a concrete source of variation or isolate
an integration boundary, not as decoration. Typical fits are:

- Strategy for interchangeable policies or business rules;
- Factory for controlled creation of variants;
- Adapter for MQ, persistence, or third-party clients;
- Repository/DAO for persistence abstraction;
- Facade or application service for a stable use-case boundary;
- Outbox/idempotency patterns only when the SD architecture and delivery
  semantics require them.

Prefer composition, interfaces, and polymorphism. Keep classes focused, avoid
large conditional business dispatch, keep method parameters at five or fewer,
and avoid deep inheritance. Follow the Java naming, visibility, exception, and
business-error rules in `AGENTS.md`; never replace a business code with an HTTP
status alone.

Leave comments around core business invariants and non-obvious decisions:

- state the rule and why it exists;
- reference the relevant API ID, acceptance criterion, or error code when that
  makes the rule traceable;
- explain concurrency, ordering, retry, or transaction assumptions;
- do not add line-by-line narration or comments inside generated code.

## 5. TDD and test scope

Use TDD for each behavior: write a failing test, implement the smallest change,
then refactor without changing behavior.

At minimum, cover the layer where the behavior lives and the boundary that can
break:

- service/domain unit tests for happy paths, invariants, and business errors;
- controller/API integration tests for generated contract binding, validation,
  response envelope, and business-code mapping;
- DAO integration tests for persistence constraints and transaction behavior;
- MQ consumer/producer or contract tests for serialization, retry,
  idempotency, and failure behavior when messaging is in scope.

Use the repository's existing test framework, fixtures, and naming conventions.
Do not weaken a test or change acceptance criteria to make an implementation
pass. Keep tests deterministic and avoid requiring live third-party services
unless the task explicitly provides an integration environment.

## 6. Validate and inspect the result

Run the narrowest relevant checks first, then the repository backend check. In
this repository the preferred backend command is:

```bash
npm run check:api
```

Use the project's wrapper/cache convention and any configured generator or
contract-validation command. For API work, verify all of the following:

- generator sync completed successfully;
- generated interfaces/DTOs/resources are current and compile;
- no duplicate hand-written API interface or boundary DTO was introduced;
- implementation matches the OpenAPI operation and API flow;
- every result maps to the required business code;
- relevant tests pass.

For MQ work, additionally verify the declared acknowledgment, retry,
dead-letter, ordering, duplicate-delivery, and transaction behavior. Report
environment failures separately from product defects, including the exact
command and actionable remediation.

## 7. Handoff and questions

Before finishing, report:

- implemented scope and files changed;
- API IDs and generator command/version used, or why the generator gate was
  blocked;
- architecture choice and the complexity signals behind it;
- tests and validation commands with results;
- assumptions, risks, and unresolved blockers;
- recommendations that need user or SD/Archi approval.

Ask focused questions instead of guessing when any of these are missing:

- the assigned scope, acceptance criterion, or upstream artifact;
- API contract, DTO shape, error code, or generated-code configuration;
- MQ delivery semantics or the intended meaning of pentagonal architecture;
- a persistence/transaction decision that changes behavior;
- permission to add a dependency, alter build configuration, or expand scope.

Do not silently resolve a design conflict in source code. Route contract gaps to
SD, architecture gaps to Archi, requirement ambiguity to SA, and implementation
defects to BE/FE as appropriate; include a concrete recommendation and impact
in the handoff.
