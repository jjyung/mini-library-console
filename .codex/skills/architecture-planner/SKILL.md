---
name: architecture-planner
description: Plan and document MVP system architecture from requirements, including technology selection, application architecture, environment-tier topology, C4 views, security, observability, resilience, NFR trade-offs, cost, risks, and explicit SD handoff boundaries.
---

# Architecture Planner

Produce a system-level architecture document that is decision-ready for SD and
PG. The architecture must explain how the system is controlled, operated,
traced, secured, and evolved across the selected environment profiles. Do not
treat observability as a single checklist item; make each operational control
an explicit architecture decision or an explicit deferred decision.

This skill is self-contained. Do not depend on role-agent configuration files
or require any file under `.codex/agents/` as an input.

## Workflow

### 0. Read context and establish scope

Read, when present:

- `README.md`
- `AGENTS.md`
- the related scenario under `docs/scenarios/`
- the corresponding workflow state under `docs/workflows/`
- the requirement artifact under `docs/requirements/`
- upstream artifacts explicitly referenced by the requirement

Identify the target domain and sequence, the business scope, in-scope and
out-of-scope capabilities, users and external systems, data boundaries, and
the environments requested. If a workflow state or requirement artifact is
missing, report that as a readiness issue rather than silently inventing it.

### 1. Requirements readiness gate: ask before deciding

Before finalizing architecture decisions, inspect whether the requirement
answers the following questions. Mark every answer as `Confirmed`, `Assumed`,
`TBD`, or `Not applicable`.

| Decision area | Minimum information required |
| --- | --- |
| Environment | Which environment profiles are in scope; purpose, isolation, data, deployment and release expectations of each |
| Users and access | Human/service actors, authentication source, roles, least-privilege expectations, privileged/admin access, and trust boundaries |
| Data protection | Data classification, secrets/PII, encryption requirements, residency, retention, deletion, and audit obligations |
| Availability | Business criticality, SLO/SLA, maintenance tolerance, failure impact, RTO, RPO, backup and disaster-recovery expectations |
| Load and efficiency | Normal/peak traffic, concurrency, growth, latency target, throughput, large payloads, and likely bottlenecks |
| Consistency and cache | Freshness tolerance, consistency requirement, cacheable data, invalidation owner, and acceptable stale/failure behavior |
| Dependencies | External services, dependency SLA, timeout/retry/rate-limit expectations, sandbox/mock policy, and dependency failure behavior |
| Operations | Metrics, logs, traces, audit events, retention, dashboard consumers, alert owners, escalation path, and runbook expectations |
| Delivery and cost | Hosting/region constraints, budget, release frequency, rollback strategy, migration/cutover, and support/on-call model |

If material information is missing, interview the user with Socratic,
decision-oriented questions before completing the architecture. Start with the
fewest questions that remove the highest-risk ambiguity. For each question,
state why the answer changes the architecture and offer concrete answer
examples where helpful. Prefer questions such as:

- Which environment profiles must this release support? Is `test` intended for
  functional/regression testing, while `SIT` is reserved for system
  integration? Is `POC` a temporary feasibility exercise or a maintained
  environment?
- What is the consequence of an outage, and how quickly must service recover
  (RTO/RPO and availability target)? Is redundancy required in every tier?
- Who may perform each sensitive action, and what evidence must remain after
  the action? Are SSO, MFA, separation of duties, or a central audit system
  mandatory?
- How much stale data is acceptable, if any? May the system serve stale data
  when the source is unavailable, and who owns invalidation?
- Who receives an alert, during what hours, and what action should be taken?

Do not silently choose critical security, availability, compliance, data-loss,
or operational assumptions. If the user authorizes assumptions, record each
one with its rationale, impact if wrong, confidence, owner, and validation
point. Block a final architecture when a missing answer would materially
change access control, environment isolation, availability/DR, data handling,
or operating responsibility. A clearly labelled preliminary option may be
provided while waiting for those answers.

### 2. Select environment profiles

Use the canonical profile definitions in [the architecture template](references/architecture-template.md), section `1.1 標準環境 profile 對應表`. Keep that table as the unique source of standard profile meaning; do not copy or redefine it in this skill or another support file.

Separate the environment name from its purpose. Select only the profiles
justified by the requirement, following the purpose, lifecycle, and promotion
rules in the canonical table. Do not make every project create every profile.

Create a project environment matrix with one row for every actual environment,
mapping its name to one profile from the canonical table. If a profile is out of
scope, say so and
explain the promotion path or future trigger. Do not copy PROD topology into
lower environments without a reason.

For each selected profile and actual environment, decide at system level:

- purpose and success criterion;
- identity, authorization, network isolation, secrets, and privileged access;
- data source, data classification, masking/synthetic-data policy, and
  retention;
- topology, replica count, scaling, failure domain, maintenance, and whether
  HA is required;
- external dependency mode: real, sandbox, stub, or mock;
- backup/restore and DR expectations;
- log/metric/trace/audit level and retention;
- dashboard, alert, on-call, and escalation expectations;
- deployment, promotion, rollback, and cost guardrails.

Keep the deployment matrix focused on deployment-specific differences. Put the
detailed security, data protection, recovery, logging, tracing, monitoring,
and alerting decisions in the corresponding NFR sections, and reference those
sections from the deployment matrix instead of repeating the same content.

At minimum, prevent production data and production credentials from leaking
into any non-PROD environment. Keep identities, secrets, telemetry access, and
destructive operations isolated by environment. Apply the purpose, data,
availability, dependency, observability, and promotion expectations from the
canonical profile table, then document any project-specific deviation. PROD
must satisfy the agreed SLO, resilience, security, backup, and operational
ownership. These are defaults, not substitutes for user confirmation.

### 3. Design the system-level architecture

Use the smallest architecture that satisfies the confirmed requirements. Cover
all of the following:

- C4-L1 system context, including actors, external systems, trust boundaries,
  and important data/control flows;
- C4-L2 containers, responsibilities, communication paths, and ownership;
- deployment topology, network zones, ingress/egress, failure domains,
  scaling boundaries, managed services, and local-run model;
- key decisions, alternatives considered, consequences, and upgrade path;
- capacity assumptions, cost drivers, and triggers for moving to a more robust
  topology.

#### Technology selection and version governance

For every material technology choice, record the selected technology and
version, version-pinning and upgrade policy, alternatives considered, reason,
NFR impact, operational cost, licensing, and ownership. For this repository's
Java backend baseline, explicitly address Java/Spring, Maven Wrapper and
`pom.xml`, the OpenAPI Generator Maven plugin, persistence, Liquibase formatted
SQL migrations, and MQ only when messaging is in scope.

The architecture must require a pinned OpenAPI Generator Maven plugin version
and a separately versioned OpenAPI contract. Generated API code is committed to
Git and is never manually edited. Database migration policy is Liquibase
formatted SQL only; Liquibase XML changelogs are not an option. These are
technology and governance decisions, not API schema or migration SQL design.

#### Application architecture selection

Explicitly choose the smallest suitable application architecture and record
the complexity signals behind the choice:

- simple synchronous use cases: three layers — `controller`, `service`, `dao`;
- substantial domain rules or multiple integration boundaries: Clean or
  Hexagonal Architecture / Ports and Adapters;
- MQ with asynchronous delivery, retries, dead letters, idempotency, ordering,
  or cross-boundary consistency: use explicit inbound adapters/ports,
  application/domain core, outbound ports, and outbound adapters.

MQ alone does not require a large redesign when it is only a trivial side
effect. Document dependency direction and integration boundaries at a level BE
can implement, but do not define internal package layouts, API fields, schema,
or transaction steps. When comparing styles, use
[the reference projects](references/architecture-reference-projects.md) as
evidence rather than copying an entire project's stack.

### 4. Apply the architecture control-plane baseline

The document must have an explicit decision, requirement reference, or
user-confirmed deferral for each control below. A generic statement such as
“enable monitoring” is insufficient.

#### Access control and security

Define the system-level approach for human and service identity,
authentication, authorization (RBAC/ABAC as appropriate), least privilege,
tenant/environment isolation, network boundaries, secrets and key ownership,
privileged/admin access, break-glass access, and deny-by-default behavior.
State where policy is enforced and how access changes are reviewed and
revoked. Do not prescribe API fields or implementation code.

#### Traceability and auditability

Define propagation of correlation/request and distributed trace identifiers
across system boundaries. Define which security and business actions are
audited, including actor/service identity, target, action, timestamp, outcome,
reason or source, and environment. Decide storage, immutability/tamper
resistance, access, retention, privacy, clock synchronization, and how an
event can be linked back to a requirement or incident.

#### Logging

Define structured centralized logs, minimum event categories, severity,
timestamp and correlation fields, collection path, retention, search/access
control, redaction of secrets/PII, sampling, clock standards, and behavior
when the logging pipeline is unavailable. Distinguish operational logs from
audit records; neither should expose credentials or unnecessary sensitive
payloads.

#### Availability, HA, resilience, and recovery

Map business criticality to availability target, RTO, and RPO. Decide the
required failure domains, redundancy, health/readiness checks, failover,
timeouts, retry/backoff, rate limiting, graceful degradation, dependency
isolation, backup/restore, disaster recovery, maintenance, and recovery test
frequency. State explicitly when a lower-tier profile intentionally has weaker
HA and what must be proven before PROD.

#### Performance, access efficiency, and caching

Define latency/throughput objectives and the system-level bottleneck strategy.
Use caching only when justified by the requirement. For each cache decision,
state data eligibility, scope, TTL/freshness, invalidation or versioning,
consistency trade-off, stampede/poisoning protection, sensitive-data policy,
capacity/eviction, behavior on cache outage, and cache hit/miss metrics. If no
cache is needed, record why and what measurement would trigger one.

#### Monitoring and alerting

Define the telemetry model across metrics, logs, traces, audit events, and
synthetic/dependency checks. At minimum cover traffic, latency, errors,
saturation, availability/SLO, dependency health, queue/storage capacity,
cache behavior when used, security signals, and key business outcomes.
Define dashboards by audience, alert condition and severity, owner, routing,
on-call hours, escalation, deduplication/noise control, runbook link, and
expected response. Prefer SLO/burn-rate or impact-based alerts over arbitrary
CPU-only alerts.

### 5. Produce the architecture document

Use [references/architecture-template.md](references/architecture-template.md)
as the base skeleton. The document must include:

- target path `docs/architecture/ARCH-<DOMAIN>-<SEQ>.md`;
- confirmed scope, environment coverage, assumptions, and open questions;
- C4-L1 and C4-L2 diagrams;
- deployment topology and local-run assumptions;
- selected technologies, versions, version-pinning policy, alternatives, and
  rationale;
- selected application architecture, complexity signals, dependency direction,
  and MQ boundary semantics when applicable;
- the standard environment-profile mapping and a project matrix that maps
  actual environment names to the selected profiles;
- separate, concrete sections for access control, traceability/audit,
  logging, HA/resilience/recovery, performance/cache, monitoring, and
  alerting;
- NFR-to-architecture mapping with requirement references and validation
  evidence/owner;
- cost/complexity trade-offs, risks, non-goals, extension triggers, and SD
  handoff items.

### 6. Enforce Archi/SD boundaries

Keep deliverables under `docs/` only. Do not output:

- API contract details, DTOs, response envelopes, operation paths, or
  field-level error mapping;
- database schema, DDL, indexes, or field-level data modelling;
- transaction step-by-step implementation, lock strategy, SQL, or code;
- module-internal package/layer design.

If one of these is required, state the system-level constraint and list it in
`移交 SD 項目（Archi 不定稿）`; do not finalize the low-level design.

### 7. Validate before finish

Run this quality gate and record `Pass`, `Fail`, `TBD`, or `N/A` with evidence:

- requirement source, user confirmations, assumptions, and open questions are
  traceable;
- every in-scope environment has an explicit profile, purpose, promotion gate,
  and isolation policy;
- C4-L1, C4-L2, deployment topology, trust boundaries, and local-run model
  are present;
- access control covers authentication, authorization, least privilege,
  secrets, privileged access, and revocation;
- request/trace correlation and security/business auditability are defined;
- operational logging is structured, centralized, redacted, retained, and
  access controlled;
- availability target, HA/failure domains, degradation, RTO/RPO,
  backup/restore, and recovery testing are addressed;
- performance objectives and cache semantics or an explicit no-cache decision
  are addressed;
- monitoring has metrics/dashboards and alerting has owner, routing,
  severity, escalation, and runbook expectations;
- technology selection includes version governance, OpenAPI Generator Maven
  plugin policy, and Liquibase formatted SQL/no-XML policy;
- application architecture selection and dependency direction are explicit;
- cost, complexity, risks, non-goals, and expansion triggers are explicit;
- SD handoff is actionable and no SD-owned low-level detail is finalized;
- at least one Mermaid diagram exists and renders as valid Mermaid.

If a gate is `Fail` or a critical item is `TBD`, do not report the architecture
as complete. Report the blocker and the exact user decision needed.

### 8. Update workflow handoff

For scenario-driven delivery, update the existing
`docs/workflows/WF-<DOMAIN>-<NNN>.md` after the architecture action. Record the
latest S2 Archi status, summary, output path, open questions, blockers or safe
assumptions, the next recommended S3 SD action, and the files the next session
must read first. Do not advance the workflow to SD when the readiness gate or
quality gate is blocked. If the workflow state is missing, use the repository's
workflow-state template only when creating the state is part of the authorized
workflow; otherwise report the missing handoff artifact as a blocker.

## Output contract

When complete, report:

- output file path;
- scope, selected profiles, and actual environment names covered;
- selected technologies, versions, and version-pinning policy;
- selected application architecture and dependency direction;
- user-confirmed decisions and remaining assumptions;
- validation-gate result and any unresolved blockers;
- SD handoff items.

## References

Use [references/architecture-template.md](references/architecture-template.md)
as the canonical output skeleton. Use
[references/architecture-reference-projects.md](references/architecture-reference-projects.md)
only when comparing Clean and Hexagonal Architecture options.
