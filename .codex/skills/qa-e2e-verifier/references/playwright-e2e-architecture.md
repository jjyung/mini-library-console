# Playwright E2E Architecture

Use this guide when creating, reviewing, or diagnosing acceptance specs. It adapts the article's boundaries to this repository's `data-testid` and QA ownership rules.

Source design: [E2E 測試：AI 時代的自動化測試與驗收（八）如何設計資料隔離與可維護的 Playwright 架構？](https://tech.samsonlab.dev/blog/ai-era-testing-and-acceptance-playwright-e2e-architecture/).

## Responsibility graph

```text
test spec -> fixture -> (POM | API/service) -> data
```

This is a responsibility graph, not a mandatory call order. A fixture may compose a page object, a data client, and data factory before the spec runs.

| Layer | Answers | Owns | Must not own |
| --- | --- | --- | --- |
| Test spec | What user journey and acceptance result are being verified? | Scenario, business action, business assertion | CSS selectors, API implementation, hidden setup details |
| Fixture | What resources does this test need and how long do they live? | Composition, setup, teardown, scope | The meaning of the acceptance oracle |
| POM/component | How does the user operate this UI? | Locators, page/component actions, UI state accessors | Test data creation, backend cleanup, cross-service business decisions |
| API/service | How is prerequisite state created, queried, or removed? | Authenticated test client, setup/query/cleanup calls | Replacing the UI behavior under test or hiding its assertion |
| Data/factory | Which state and identifiers belong to this test? | Defaults, overrides, ownership, uniqueness, namespace | Shared mutable state without an explicit isolation decision |

The closer code is to the spec, the more it should read like user intent. The deeper it is, the more it should contain reusable mechanics.

## Fixture scope and resource ownership

Choose scope from mutability and creation cost:

| Resource | Default scope | Share only when |
| --- | --- | --- |
| `Page`, `BrowserContext`, test entity | Test | Never share mutable state between tests |
| Stable expensive account/resource | Worker | Every test in that worker can safely use it, or each test creates its own mutable child data |
| Immutable reference seed | Setup/worker | Tests cannot mutate it |
| Dev server/environment | `webServer`/worker-level process | It is externally managed and safe to reuse |

`storageState` reduces repeated login work but only represents browser identity/session. It does not make orders, books, users, settings, or other server-side business records safe to share. For tests that modify server state, prefer one account per parallel worker and unique mutable entities per test.

Fixtures should be lazy where possible and should tear down resources after `use()` returns. If a teardown can be skipped by a killed runner or failed provider call, isolation must still come from unique identifiers and cleanup must be recoverable by batch/TTL.

## Data isolation model

Classify each data dependency before implementing it:

| Data | Ownership | Typical handling |
| --- | --- | --- |
| Immutable reference data | Environment or suite | Seed once; never mutate from E2E |
| Stable prerequisite resource | Worker or fixture | Create/reuse only with a documented scope decision |
| Mutable business entity | Test | Create through API/test service with a unique identifier |
| External side effect | Test/sandbox | Use a sandbox or controllable substitute with correlation/run ID |

A useful identity is composed from the test run, worker/parallel identity, and test identity. Use the project's existing equivalent when available; never rely on a fixed ID or fixed list order for a mutable record.

Normal teardown is necessary but insufficient. Combine it with:

- a unique namespace or run tag;
- a correlation ID in logs and external requests;
- a repeatable batch cleanup query;
- TTL/scheduled cleanup for interrupted runs.

The goal is result independence: one test must not change another test's expected result, and each test must run alone or in a different order. It is not necessary to rebuild every immutable resource for every test.

## API setup versus UI acceptance

Use the same UI entry point a user would use for the behavior being accepted. Use an API or test service to reach a prerequisite state quickly and precisely, such as creating a book with an available copy before testing checkout.

Good separation:

```text
fixture: create an available book through API/test service
spec: open the book list, check out the book, assert availability/status
fixture: clean the unique book and checkout record
```

Do not let the setup service perform the action or assertion that the UI acceptance test claims to verify. A `200` from a setup endpoint proves setup only; it does not prove the rendered state, user interaction, or business outcome.

Name test clients after their purpose, such as `BookDataClient` or `TestDataService`, so they are not confused with an application domain service. Keep clients focused on setup/query/cleanup rather than becoming a universal workflow object.

## Locator contract

Use this decision order, adjusted for the repository's existing contract:

1. `getByRole` with an accessible name when role/name is part of the user-facing requirement.
2. `getByLabel` for form controls.
3. `getByText`/other user-facing locators when visible text is the acceptance contract.
4. `getByTestId` for stable state containers, dynamic content, rows, toasts, and elements without a meaningful stable semantic locator.
5. CSS/XPath only for an unavoidable legacy case, with a comment explaining why.

This repository requires preserving and generally preferring its `data-testid` contract. That does not mean every interaction should ignore accessible semantics: a `getByRole('button', { name })` assertion catches a broken role or label, while a test id gives a stable hook for a dynamic status region. Never remove or rename an existing test id as part of QA test maintenance.

Scope repeated elements to a row, card, dialog, or component before selecting a child control. Avoid selectors tied to styling classes, DOM depth, generated IDs, or incidental order.

## Abstraction check

Extract mechanical repetition: locators, API parsing, auth, setup/cleanup, polling, and diagnostics. Keep scenario-specific semantics in the spec: why a state is needed, which business rule differs, and what result proves acceptance.

Before adding a base class or flow helper, ask:

- Is the repetition mechanical or does it encode business meaning?
- Can a reader still understand the acceptance result without opening several files?
- Could a change affect tests that do not share the same business behavior?

Prefer small page/component objects and explicitly named flows over a universal `BasePage` or a method that silently performs login, data creation, multiple business actions, and assertions.
