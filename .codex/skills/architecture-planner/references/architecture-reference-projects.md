# Clean / Hexagonal Architecture Reference Projects

Use these public repositories as comparison material when the requirement does
not provide an architecture example. They are references, not normative
templates. Select only the principles that fit the current system's complexity,
NFRs, team capability, and operational budget.

## 1. DDD by Examples Factory

Repository: <https://github.com/ddd-by-examples/factory>

Useful observations:

- It deliberately separates simple CRUD work from complex business commands.
- Complex business processing is modeled with a domain model, application
  services as primary ports, repositories/events as secondary ports, and REST,
  persistence, and event propagation as adapters.
- It is a strong reference for a hybrid decision: keep ordinary CRUD simple,
  and isolate only valuable domain complexity behind ports and adapters.

Apply this lesson when deciding whether a feature needs clean/hexagonal
boundaries or can remain a three-layer use case.

## 2. Clean Architecture in Pure Java

Repository: <https://github.com/link-intersystems/clean-architecture-example>

Useful observations:

- Domain-oriented modules make the use cases visible at the top level.
- Package-by-component is used to keep each component together across layers.
- The example emphasizes dependency rules, use cases/interactors, request and
  response models, and domain events while keeping framework details away from
  the core.

Apply this lesson when domain boundaries and dependency direction matter more
than mirroring technical layers in every package.

## 3. Hexagonal Architecture and DDD

Repository: <https://github.com/JonathanM2ndoza/Hexagonal-Architecture-DDD>

Useful observations:

- The repository explicitly separates Application, Domain, and Infrastructure.
- A Java port is represented by an interface and an adapter is an implementation
  of that port.
- It demonstrates that database and transport technology can change without
  changing the application core when boundaries are respected.

Apply this lesson when REST, persistence, MQ, or external-service adapters need
independent substitution or testing.

## 4. Food Ordering System

Repository: <https://github.com/p-papag/food-ordering-system>

Useful observations:

- It combines Clean/Hexagonal Architecture with DDD, Kafka, SAGA, Outbox, and
  CQRS in a multi-service example.
- It is useful for identifying the extra complexity introduced by asynchronous
  messaging, eventual consistency, and distributed transactions.

Do not copy this whole stack for a small service. Use it only to ask whether
MQ requires explicit delivery, retry, idempotency, outbox, or consistency
decisions in the current architecture.

## Decision rule

The reference projects support these defaults:

- simple synchronous CRUD or one-boundary use case: three layers;
- meaningful domain invariants or multiple adapters: clean/hexagonal boundaries;
- MQ with asynchronous delivery or cross-boundary consistency: hexagonal
  ports/adapters plus explicitly designed messaging semantics;
- never add CQRS, SAGA, Outbox, event sourcing, or microservices solely because
  a reference project uses them.
