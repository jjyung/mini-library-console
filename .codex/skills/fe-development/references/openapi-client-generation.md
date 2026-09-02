# OpenAPI Client Generation

This reference controls the generated frontend API boundary. The OpenAPI
contract and the repository's dependency lockfile are the sources of truth;
this document is not a substitute for either.

## Shared generation gate

1. Confirm `docs/openapi.yaml` exists and the target operation has its path,
   method, path/query/header parameters, `operationId`, API ID, request and
   response schemas, validation constraints, and business-code responses.
2. Confirm the contract version and the generator package version are both
   explicit and pinned. Resolve the package version at implementation time;
   do not use `latest`, a floating range, or an untracked global install.
3. Commit the generator configuration and lockfile. Use a package script so
   local, CI, and handoff generation use the same command.
4. Generate into a dedicated `generated/` directory. Review the diff and remove
   stale generated files only through the generator's configured behavior.
5. Do not manually edit generated files. Put auth, runtime base URL, error
   mapping, retries, and view-model conversions in non-generated code.
6. Compile/type-check generated code and run a request/response contract check.
7. Follow repository policy for whether generated output is committed. If the
   policy is not explicit, record the decision in the task before delivery;
   never let source control policy be inferred from a tool default.

## Angular: `ng-openapi-gen`

Use this path only for an Angular application. The upstream generator supports
OpenAPI 3.0/3.1 and Angular 16+, and its current 1.x defaults generate
standalone-friendly output, an `Api` service, union-style enum aliases, and
Promises. Verify the installed Angular version and exact package version before
choosing options.

Keep a project-local `ng-openapi-gen.json` similar to:

```json
{
  "$schema": "node_modules/ng-openapi-gen/ng-openapi-gen-schema.json",
  "input": "../../../docs/openapi.yaml",
  "output": "src/app/core/api/generated",
  "apiService": "Api",
  "module": false,
  "promises": false,
  "enumStyle": "alias",
  "enumArray": true,
  "indexFile": true,
  "ignoreUnusedModels": false,
  "removeStaleFiles": true
}
```

Adjust the relative `input` path to the app root and preserve the project's
chosen Promise/Observable approach. Do not copy this file blindly into a
different framework or repo layout.

Typical scripts are:

```json
{
  "scripts": {
    "generate:api": "ng-openapi-gen",
    "check:api-generated": "npm run generate:api && git diff --exit-code -- src/app/core/api/generated"
  }
}
```

The exact diff-check command must account for the repository's generated-code
commit policy. If generated output is intentionally untracked, check that the
generated tree is reproducible in CI instead.

Use the generated functions/service only inside a centralized app client. The
generator does not perform arbitrary date transformation; an OpenAPI
`date-time` string remains a string unless the app explicitly maps it. Keep
that conversion in a named mapper and test it.

Important Angular-specific checks:

- use the Angular CLI and the project's installed Angular version for new
  components/services/routes;
- configure the generated root URL through Angular environment/runtime
  configuration, not a literal production URL;
- choose Promise or Observable generation consistently with the app's state
  and cancellation strategy;
- run the project's API generation command, type-check, tests, and `ng build`;
- never change generated imports, DTOs, or request builders by hand.

## Vue or React

`ng-openapi-gen` is Angular-specific. For Vue or React, select an approved
toolchain that produces typed models and a centralized client, such as
`openapi-typescript` with `openapi-fetch`, or Orval when generated hooks/services
match the repository architecture. Pin versions, keep config in source, and
generate into the equivalent `src/core/api/generated` path.

For a Vue 3 + Vite application, prefer the smallest stable default:
`openapi-typescript` for the contract types and `openapi-fetch` behind an
app-owned client wrapper. Add Orval only when generated Vue Query hooks, MSW
mocks, or generated service files provide a concrete benefit.

Use package scripts as the only generation entry points. A project-local
`package.json` can follow this shape after the OpenAPI contract exists:

```json
{
  "scripts": {
    "api:generate": "openapi-typescript ../../../docs/openapi.yaml -o src/core/api/generated/schema.d.ts",
    "api:check": "npm run api:generate && git diff --exit-code -- src/core/api/generated",
    "check": "npm run api:check && npm run type-check && npm run test:unit && npm run build"
  }
}
```

The relative contract path is an example for this repository's app root;
recalculate it for another app. Install exact approved versions as local
dependencies, commit the lockfile, and run the scripts from the app root. Do
not put `@latest` in scripts. The `api:check` example assumes generated output
is tracked; if the repository intentionally ignores it, generate into a clean
CI temporary directory and type-check that result instead.

Recommended setup commands, with the versions chosen and recorded by the
project owner, are:

```bash
npm install --save-exact openapi-fetch@<approved-version>
npm install --save-dev --save-exact openapi-typescript@<approved-version>
```

Do not add these dependencies or scripts until `docs/openapi.yaml` and the
framework/contract handoff are ready.

Regardless of tool:

- components use an app-owned service/composable/hook, not generated calls;
- generated models remain contract-shaped;
- adapters handle UI-specific names or date/value conversion;
- error mapping uses business codes and preserves enough context for retry;
- generation runs in CI or is committed according to an explicit repo policy.

## Sources to verify at implementation time

- [`ng-openapi-gen` npm package](https://www.npmjs.com/package/ng-openapi-gen)
- [`ng-openapi-gen` README](https://github.com/cyclosproject/ng-openapi-gen)
- [`ng-openapi-gen` configuration schema](https://github.com/cyclosproject/ng-openapi-gen/blob/master/ng-openapi-gen-schema.json)
- [Angular skills repository](https://github.com/angular/skills)
- [Angular developer skill](https://github.com/angular/skills/blob/main/angular-developer/SKILL.md)
