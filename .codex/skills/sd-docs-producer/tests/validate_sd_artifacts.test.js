"use strict";

const assert = require("assert");
const fs = require("fs");
const os = require("os");
const path = require("path");
const test = require("node:test");

const {
  findMarkdownTable,
  validateOpenApiContract,
  validateProject
} = require("../scripts/validate_sd_artifacts.js");

function createContext(projectRootPath) {
  return { projectRootPath, issues: [] };
}

function writeFile(filePath, content) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, content, "utf8");
}

function validErrorCodes() {
  return `# Error Codes

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | SD | 2026-09-03 | Test fixture. |

## 2. Global Rules

HTTP status cannot replace a business code. Every error has a traceId.

## 3. Error Code Registry

| Code | Meaning |
| --- | --- |
| 00000 | Success |
| A0000 | Client |
| B0000 | System |
| C0000 | Third-party |

## 4. Shared Error Response Contract

ErrorResponseDTO contains code, message, and traceId.

## 5. API Reference Rules

API flow documents reference docs/openapi.yaml and docs/error-codes.md.
`;
}

function validSchema() {
  const codeFence = "```";
  return `# Users Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | SD | 2026-09-03 | Test fixture. |

## 2. Schema

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| user_id | UUID | No | none | PK | Identifier | userId |

### 2.4 限制條件

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| pk_users | Primary key | Unique identifier | A0000 |

## 3. DDL

${codeFence}sql
CREATE TABLE users (
    user_id UUID NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id)
);
${codeFence}
`;
}

function validApiFlow(apiId, method, pathTemplate) {
  const codeFence = "```";
  return `# ${apiId}

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | SD | 2026-09-03 | Test fixture. |

## 2. API Flow

### 2.1 Workflow Sequence Diagram

${codeFence}mermaid
sequenceDiagram
    Client->>API: ${method} ${pathTemplate}
${codeFence}

## 3. Execute Business Logic

### Detailed Logic

1. Execute the business rule.

### Given / When / Then

- Given valid application service input
- When the service executes
- Then the response contains the business result

## 4. 錯誤代碼 (Error Codes)

### 4.1 Business Code Definition

See docs/error-codes.md.

### 4.2 API-specific Error Mapping

| HTTP Status | Business Code | Trigger | Response Behavior | Retryable |
| --- | --- | --- | --- | --- |
| 200 | 00000 | Success | ResponseDTO | No |
| 400 | A0000 | Invalid business state | ErrorResponseDTO | No |
`;
}

function validOpenApi(apiId = "library-books-001") {
  return `openapi: 3.0.3
info:
  title: Test API
  version: 1.0.0
paths:
  /v1/books:
    get:
      summary: List books [${apiId}]
      operationId: getBooks
      x-api-id: ${apiId}
      x-request-dto: GetBooksRequestDTO
      parameters:
        - name: query
          in: query
          schema:
            $ref: '#/components/schemas/GetBooksRequestDTO'
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GetBooksResponseDTO'
        '400':
          description: Client error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponseDTO'
        '500':
          description: System error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponseDTO'
components:
  schemas:
    GetBooksRequestDTO:
      type: object
    GetBooksResponseDTO:
      type: object
      required: [code]
      properties:
        code:
          type: string
          enum: ["00000"]
    ErrorResponseDTO:
      type: object
      required: [code]
      properties:
        code:
          type: string
          enum: [A0000, B0000, C0000]
`;
}

test("validates the reference OpenAPI template without structural errors", () => {
  const repositoryRootPath = path.resolve(__dirname, "../../../..");
  const context = createContext(repositoryRootPath);
  const referencePath = path.join(
    repositoryRootPath,
    ".codex",
    "skills",
    "sd-docs-producer",
    "references",
    "user-crud-openapi.yaml"
  );
  const result = validateOpenApiContract(context, referencePath);
  assert.equal(result.operations.length, 5);
  assert.deepEqual(context.issues.filter((issue) => issue.severity === "ERROR"), []);
});

test("rejects a missing summary and a nested ResponseDTO reference", () => {
  const temporaryRootPath = fs.mkdtempSync(path.join(os.tmpdir(), "sd-validator-"));
  const contractPath = path.join(temporaryRootPath, "openapi.yaml");
  const invalidContract = validOpenApi()
    .replace("      summary: List books [library-books-001]\n", "")
    .replace(
      "                $ref: '#/components/schemas/GetBooksResponseDTO'",
      "                type: object\n                properties:\n                  data:\n                    $ref: '#/components/schemas/GetBooksResponseDTO'"
    );
  writeFile(contractPath, invalidContract);

  const context = createContext(temporaryRootPath);
  validateOpenApiContract(context, contractPath);
  const issueCodes = new Set(context.issues.map((issue) => issue.code));
  assert.ok(issueCodes.has("MISSING_OPERATION_SUMMARY"));
  assert.ok(issueCodes.has("SUCCESS_RESPONSE_DTO_MISSING"));
  fs.rmSync(temporaryRootPath, { recursive: true, force: true });
});

test("finds the required schema dictionary columns", () => {
  const table = findMarkdownTable(validSchema().split(/\r?\n/), [
    "Column",
    "Type",
    "Nullable",
    "Default",
    "Description",
    "API Mapping"
  ]);
  assert.ok(table);
  assert.equal(table.rows.length, 1);
});

test("fails cross-artifact validation when an API flow is missing", () => {
  const temporaryRootPath = fs.mkdtempSync(path.join(os.tmpdir(), "sd-validator-"));
  writeFile(path.join(temporaryRootPath, "docs", "requirements", "REQ-LIB-001.md"), "# Requirement\nAPI ID: library-books-001\n");
  writeFile(path.join(temporaryRootPath, "docs", "architecture", "ARCH-LIB-001.md"), "# Architecture\nMVP boundary.\n");
  writeFile(path.join(temporaryRootPath, "docs", "openapi.yaml"), validOpenApi());
  writeFile(path.join(temporaryRootPath, "docs", "error-codes.md"), validErrorCodes());
  writeFile(path.join(temporaryRootPath, "docs", "schema", "users.md"), validSchema());
  fs.mkdirSync(path.join(temporaryRootPath, "docs", "api"), { recursive: true });

  const report = validateProject({
    projectRoot: temporaryRootPath,
    requirements: [],
    architectures: [],
    openapi: "docs/openapi.yaml",
    errorCodes: "docs/error-codes.md",
    apiDir: "docs/api",
    schemaDir: "docs/schema",
    format: "text",
    strict: false
  });

  assert.equal(report.valid, false);
  assert.ok(report.issues.some((issue) => issue.code === "MISSING_API_FLOW"));
  fs.rmSync(temporaryRootPath, { recursive: true, force: true });
});

test("passes a complete minimal SD artifact set", () => {
  const temporaryRootPath = fs.mkdtempSync(path.join(os.tmpdir(), "sd-validator-"));
  writeFile(path.join(temporaryRootPath, "docs", "requirements", "REQ-LIB-001.md"), "# Requirement\nAPI ID: library-books-001\n");
  writeFile(path.join(temporaryRootPath, "docs", "architecture", "ARCH-LIB-001.md"), "# Architecture\nMVP boundary.\n");
  writeFile(path.join(temporaryRootPath, "docs", "openapi.yaml"), validOpenApi());
  writeFile(path.join(temporaryRootPath, "docs", "error-codes.md"), validErrorCodes());
  writeFile(path.join(temporaryRootPath, "docs", "schema", "users.md"), validSchema());
  writeFile(path.join(temporaryRootPath, "docs", "api", "library-books-001_list-books.md"), validApiFlow("library-books-001", "GET", "/v1/books"));

  const report = validateProject({
    projectRoot: temporaryRootPath,
    requirements: [],
    architectures: [],
    openapi: "docs/openapi.yaml",
    errorCodes: "docs/error-codes.md",
    apiDir: "docs/api",
    schemaDir: "docs/schema",
    format: "text",
    strict: false
  });

  assert.equal(report.valid, true, JSON.stringify(report.issues, null, 2));
  assert.equal(report.errorCount, 0);
  fs.rmSync(temporaryRootPath, { recursive: true, force: true });
});
