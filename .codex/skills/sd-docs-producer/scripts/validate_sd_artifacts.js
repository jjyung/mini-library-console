#!/usr/bin/env node

"use strict";

const fs = require("fs");
const path = require("path");

const projectRootPath = path.resolve(__dirname, "../../../..");
const httpMethods = new Set(["get", "post", "put", "patch", "delete", "options", "head", "trace"]);
const apiIdPattern = /^[a-z0-9]+(?:-[a-z0-9]+)+-(?:00[1-9]|0[1-9][0-9]|[1-9][0-9]{2})$/;
const dtoNamePatterns = {
  request: /^[A-Z][A-Za-z0-9]+RequestDTO$/,
  response: /^[A-Z][A-Za-z0-9]+ResponseDTO$/
};
const placeholderPattern = /<[^>\n]+>/;
const emptyValues = new Set(["", "-", "—", "待填寫", "TODO", "TBD"]);
const requiredErrorCodes = ["00000", "A0000", "B0000", "C0000"];

const usageText = `Usage:
  node .codex/skills/sd-docs-producer/scripts/validate_sd_artifacts.js [options]

Options:
  --project-root <path>       Repository root. Defaults to the current repository.
  --requirement <path>        Requirement Markdown file. Repeatable.
  --architecture <path>       Architecture Markdown file. Repeatable.
  --openapi <path>             OpenAPI contract. Defaults to docs/openapi.yaml.
  --error-codes <path>         Global error codes. Defaults to docs/error-codes.md.
  --api-dir <path>             API flow directory. Defaults to docs/api.
  --schema-dir <path>          Schema directory. Defaults to docs/schema.
  --format <text|json>         Report format. Defaults to text.
  --strict                     Treat warnings as validation errors.
  --help                       Show this help.
`;

function isRecord(value) {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function normalized(value) {
  return String(value || "").replace(/\s+/g, "").trim();
}

function isEmptyValue(value) {
  return emptyValues.has(String(value || "").trim());
}

function addIssue(context, severity, code, message, filePath, lineNumber) {
  const relativeFilePath = filePath
    ? path.relative(context.projectRootPath, filePath) || "."
    : undefined;
  context.issues.push({
    severity,
    code,
    message,
    ...(relativeFilePath ? { file: relativeFilePath } : {}),
    ...(lineNumber ? { line: lineNumber } : {})
  });
}

function addError(context, code, message, filePath, lineNumber) {
  addIssue(context, "ERROR", code, message, filePath, lineNumber);
}

function addWarning(context, code, message, filePath, lineNumber) {
  addIssue(context, "WARNING", code, message, filePath, lineNumber);
}

function isInsideProject(projectRoot, candidatePath) {
  const relativePath = path.relative(projectRoot, candidatePath);
  return relativePath === "" || (!relativePath.startsWith(`..${path.sep}`) && relativePath !== ".." && !path.isAbsolute(relativePath));
}

function resolveRepositoryPath(context, rawPath) {
  const resolvedPath = path.resolve(context.projectRootPath, rawPath);
  if (!isInsideProject(context.projectRootPath, resolvedPath)) {
    addError(context, "PATH_OUTSIDE_PROJECT", `Path must stay inside the repository: ${rawPath}`);
    return undefined;
  }
  return resolvedPath;
}

function readTextFile(context, filePath, description) {
  try {
    return fs.readFileSync(filePath, "utf8");
  } catch (error) {
    if (error.code === "ENOENT") {
      addError(context, "MISSING_FILE", `${description} does not exist.`, filePath);
    } else if (error instanceof TypeError) {
      addError(context, "INVALID_UTF8", `${description} is not valid UTF-8.`, filePath);
    } else {
      addError(context, "READ_FILE_FAILED", `Cannot read ${description}: ${error.message}`, filePath);
    }
    return undefined;
  }
}

function listMarkdownFiles(context, directoryPath, description, required) {
  if (!fs.existsSync(directoryPath)) {
    if (required) {
      addError(context, "MISSING_DIRECTORY", `${description} directory does not exist.`, directoryPath);
    } else {
      addWarning(context, "OPTIONAL_DIRECTORY_MISSING", `${description} directory does not exist; confirm no artifact is required.`, directoryPath);
    }
    return [];
  }

  let directoryEntries;
  try {
    directoryEntries = fs.readdirSync(directoryPath, { withFileTypes: true });
  } catch (error) {
    addError(context, "READ_DIRECTORY_FAILED", `Cannot read ${description} directory: ${error.message}`, directoryPath);
    return [];
  }

  const markdownFiles = directoryEntries
    .filter((entry) => entry.isFile() && entry.name.toLowerCase().endsWith(".md"))
    .map((entry) => path.join(directoryPath, entry.name))
    .sort();

  if (required && markdownFiles.length === 0) {
    addError(context, "EMPTY_DIRECTORY", `${description} directory has no Markdown files.`, directoryPath);
  }
  return markdownFiles;
}

function resolveInputDocuments(context, explicitPaths, defaultDirectory, description) {
  if (explicitPaths.length > 0) {
    return explicitPaths
      .map((rawPath) => resolveRepositoryPath(context, rawPath))
      .filter(Boolean);
  }

  const directoryPath = resolveRepositoryPath(context, defaultDirectory);
  return directoryPath
    ? listMarkdownFiles(context, directoryPath, description, true)
    : [];
}

function lineNumberOf(source, text) {
  const index = source.indexOf(text);
  return index < 0 ? undefined : source.slice(0, index).split(/\r?\n/).length;
}

function parseYamlDocument(context, filePath, source) {
  let yamlLibrary;
  try {
    yamlLibrary = require("yaml");
  } catch (error) {
    addError(
      context,
      "YAML_PARSER_UNAVAILABLE",
      "The pinned 'yaml' package is unavailable. Run npm install before SD validation.",
      filePath
    );
    return undefined;
  }

  try {
    const yamlDocument = yamlLibrary.parseDocument(source, {
      prettyErrors: true,
      uniqueKeys: true
    });
    if (yamlDocument.errors.length > 0) {
      yamlDocument.errors.forEach((error) => {
        addError(context, "INVALID_YAML", error.message, filePath);
      });
      return undefined;
    }
    return yamlDocument.toJS();
  } catch (error) {
    addError(context, "INVALID_YAML", `Cannot parse OpenAPI YAML: ${error.message}`, filePath);
    return undefined;
  }
}

function decodeJsonPointerToken(token) {
  return token.replace(/~1/g, "/").replace(/~0/g, "~");
}

function resolveLocalReference(document, reference) {
  if (typeof reference !== "string" || !reference.startsWith("#/")) {
    return undefined;
  }
  let currentValue = document;
  for (const token of reference.slice(2).split("/")) {
    const decodedToken = decodeJsonPointerToken(token);
    if (currentValue === null || currentValue === undefined || !(decodedToken in Object(currentValue))) {
      return undefined;
    }
    currentValue = currentValue[decodedToken];
  }
  return currentValue;
}

function referenceName(reference) {
  if (typeof reference !== "string") {
    return undefined;
  }
  const tokens = reference.split("/");
  return tokens[tokens.length - 1];
}

function collectReferences(value, references = []) {
  if (Array.isArray(value)) {
    value.forEach((item) => collectReferences(item, references));
    return references;
  }
  if (!isRecord(value)) {
    return references;
  }
  if (typeof value.$ref === "string") {
    references.push(value.$ref);
  }
  Object.entries(value).forEach(([key, childValue]) => {
    if (key !== "$ref") {
      collectReferences(childValue, references);
    }
  });
  return references;
}

function validateOpenApiReferences(context, document, filePath) {
  const references = collectReferences(document);
  const seenReferences = new Set();
  references.forEach((reference) => {
    if (seenReferences.has(reference)) {
      return;
    }
    seenReferences.add(reference);
    if (!reference.startsWith("#/")) {
      addWarning(context, "EXTERNAL_REF", `External $ref is not checked for local consistency: ${reference}`, filePath);
      return;
    }
    if (resolveLocalReference(document, reference) === undefined) {
      addError(context, "BROKEN_REF", `OpenAPI local $ref cannot be resolved: ${reference}`, filePath);
    }
  });
}

function resolveReferenceValue(document, value, visitedReferences = new Set()) {
  if (!isRecord(value) || typeof value.$ref !== "string") {
    return value;
  }
  if (!value.$ref.startsWith("#/") || visitedReferences.has(value.$ref)) {
    return value;
  }
  const resolvedValue = resolveLocalReference(document, value.$ref);
  if (resolvedValue === undefined) {
    return value;
  }
  const nextVisitedReferences = new Set(visitedReferences);
  nextVisitedReferences.add(value.$ref);
  return resolveReferenceValue(document, resolvedValue, nextVisitedReferences);
}

function findCodeValues(document, value, visitedReferences = new Set()) {
  if (!isRecord(value)) {
    return [];
  }
  if (typeof value.$ref === "string") {
    if (!value.$ref.startsWith("#/") || visitedReferences.has(value.$ref)) {
      return [];
    }
    const resolvedValue = resolveLocalReference(document, value.$ref);
    if (resolvedValue === undefined) {
      return [];
    }
    const nextVisitedReferences = new Set(visitedReferences);
    nextVisitedReferences.add(value.$ref);
    return findCodeValues(document, resolvedValue, nextVisitedReferences);
  }

  const codeSchema = value.properties && value.properties.code;
  if (codeSchema) {
    const resolvedCodeSchema = resolveReferenceValue(document, codeSchema);
    if (Array.isArray(resolvedCodeSchema.enum)) {
      return resolvedCodeSchema.enum.map(String);
    }
    if (resolvedCodeSchema.const !== undefined) {
      return [String(resolvedCodeSchema.const)];
    }
  }

  return ["allOf", "oneOf", "anyOf"].flatMap((keyword) => {
    const values = value[keyword];
    return Array.isArray(values)
      ? values.flatMap((item) => findCodeValues(document, item, visitedReferences))
      : [];
  });
}

function topLevelSchemaReference(value) {
  if (!isRecord(value) || typeof value.$ref !== "string" || !value.$ref.includes("/schemas/")) {
    return undefined;
  }
  return value.$ref;
}

function topLevelSchemaName(value) {
  return referenceName(topLevelSchemaReference(value));
}

function responseSchema(document, response) {
  const resolvedResponse = resolveReferenceValue(document, response);
  if (!isRecord(resolvedResponse) || !isRecord(resolvedResponse.content)) {
    return undefined;
  }
  const contentValue = resolvedResponse.content["application/json"]
    || Object.values(resolvedResponse.content)[0];
  return isRecord(contentValue) ? contentValue.schema : undefined;
}

function requestSchemaReferences(document, pathItem, operation) {
  const references = [];
  const requestDto = operation["x-request-dto"];
  if (typeof requestDto === "string") {
    references.push({ name: requestDto, source: "x-request-dto" });
  }

  const requestBody = resolveReferenceValue(document, operation.requestBody);
  if (isRecord(requestBody) && isRecord(requestBody.content)) {
    Object.values(requestBody.content).forEach((mediaType) => {
      if (isRecord(mediaType) && mediaType.schema) {
        const reference = topLevelSchemaReference(mediaType.schema);
        if (reference) {
          references.push({ name: referenceName(reference), source: "requestBody" });
        }
      }
    });
  }

  const parameters = [
    ...(Array.isArray(pathItem.parameters) ? pathItem.parameters : []),
    ...(Array.isArray(operation.parameters) ? operation.parameters : [])
  ];
  parameters.forEach((parameter) => {
    const resolvedParameter = resolveReferenceValue(document, parameter);
    if (!isRecord(resolvedParameter)) {
      return;
    }
    const parameterSchema = resolvedParameter.schema
      || (isRecord(resolvedParameter.content) && Object.values(resolvedParameter.content)[0]?.schema);
    const reference = topLevelSchemaReference(parameterSchema);
    if (reference) {
      references.push({ name: referenceName(reference), source: "parameter" });
    }
  });
  return references;
}

function validateDtoSchemaNames(context, document, filePath) {
  const schemas = document.components && document.components.schemas;
  if (!isRecord(schemas)) {
    addError(context, "MISSING_SCHEMAS", "OpenAPI must define components.schemas.", filePath);
    return;
  }
  Object.keys(schemas).forEach((schemaName) => {
    if (schemaName.endsWith("RequestDTO") && !dtoNamePatterns.request.test(schemaName)) {
      addError(context, "INVALID_REQUEST_DTO_NAME", `Request schema name is not compliant: ${schemaName}`, filePath);
    }
    if (schemaName.endsWith("ResponseDTO") && !dtoNamePatterns.response.test(schemaName)) {
      addError(context, "INVALID_RESPONSE_DTO_NAME", `Response schema name is not compliant: ${schemaName}`, filePath);
    }
  });
}

function validateOpenApiContract(context, filePath) {
  const source = readTextFile(context, filePath, "OpenAPI contract");
  if (source === undefined) {
    return { document: undefined, operations: [] };
  }
  if (placeholderPattern.test(source)) {
    addError(context, "OPENAPI_PLACEHOLDER", "OpenAPI contract contains an unresolved placeholder.", filePath, lineNumberOf(source, "<"));
  }

  const document = parseYamlDocument(context, filePath, source);
  if (!isRecord(document)) {
    addError(context, "INVALID_OPENAPI_DOCUMENT", "OpenAPI document root must be an object.", filePath);
    return { document, operations: [] };
  }
  validateOpenApiReferences(context, document, filePath);

  if (typeof document.openapi !== "string" || !/^3\./.test(document.openapi)) {
    addError(context, "INVALID_OPENAPI_VERSION", "OpenAPI contract must declare an OpenAPI 3.x version.", filePath);
  }
  if (!isRecord(document.info)) {
    addError(context, "MISSING_OPENAPI_INFO", "OpenAPI contract must define info.", filePath);
  } else if (isEmptyValue(document.info.version) || placeholderPattern.test(String(document.info.version || ""))) {
    addError(context, "INVALID_CONTRACT_VERSION", "OpenAPI info.version must be a concrete value.", filePath);
  } else if (!/^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?(?:\+[0-9A-Za-z.-]+)?$/.test(String(document.info.version))) {
    addWarning(context, "NON_SEMVER_CONTRACT_VERSION", "OpenAPI info.version is not semantic-version shaped; confirm the tracked contract version.", filePath);
  }

  validateDtoSchemaNames(context, document, filePath);
  const paths = document.paths;
  if (!isRecord(paths) || Object.keys(paths).length === 0) {
    addError(context, "MISSING_OPENAPI_PATHS", "OpenAPI contract must define at least one path.", filePath);
    return { document, operations: [] };
  }

  const operations = [];
  const apiIds = new Map();
  const operationIds = new Map();
  Object.entries(paths).forEach(([pathTemplate, pathItem]) => {
    if (!pathTemplate.startsWith("/")) {
      addError(context, "INVALID_PATH", `OpenAPI path must start with '/': ${pathTemplate}`, filePath);
    }
    if (!isRecord(pathItem)) {
      addError(context, "INVALID_PATH_ITEM", `OpenAPI path item must be an object: ${pathTemplate}`, filePath);
      return;
    }
    httpMethods.forEach((method) => {
      const operation = pathItem[method];
      if (operation === undefined) {
        return;
      }
      if (!isRecord(operation)) {
        addError(context, "INVALID_OPERATION", `OpenAPI operation must be an object: ${method.toUpperCase()} ${pathTemplate}`, filePath);
        return;
      }

      const apiId = operation["x-api-id"];
      const operationId = operation.operationId;
      const operationLabel = `${method.toUpperCase()} ${pathTemplate}`;
      if (typeof apiId !== "string" || !apiIdPattern.test(apiId)) {
        addError(context, "INVALID_API_ID", `API ID must match the repository format: ${apiId || "missing"} (${operationLabel})`, filePath);
      } else if (apiIds.has(apiId)) {
        addError(context, "DUPLICATE_API_ID", `API ID is duplicated with ${apiIds.get(apiId)}: ${apiId}`, filePath);
      } else {
        apiIds.set(apiId, operationLabel);
      }

      if (typeof operationId !== "string" || !operationId.trim()) {
        addError(context, "MISSING_OPERATION_ID", `operationId is required: ${operationLabel}`, filePath);
      } else if (operationIds.has(operationId)) {
        addError(context, "DUPLICATE_OPERATION_ID", `operationId is duplicated with ${operationIds.get(operationId)}: ${operationId}`, filePath);
      } else {
        operationIds.set(operationId, operationLabel);
      }
      if (typeof operation.summary !== "string" || !operation.summary.trim()) {
        addError(context, "MISSING_OPERATION_SUMMARY", `Operation summary is required: ${operationLabel}`, filePath);
      } else if (typeof apiId === "string" && !operation.summary.includes(apiId)) {
        addError(context, "API_ID_NOT_IN_SUMMARY", `Operation summary must include API ID ${apiId}: ${operationLabel}`, filePath);
      }
      if (typeof operationId === "string" && operationId === apiId) {
        addError(context, "OPERATION_ID_EQUALS_API_ID", `operationId must not equal API ID: ${apiId}`, filePath);
      }

      const requestReferences = requestSchemaReferences(document, pathItem, operation);
      if (requestReferences.length === 0) {
        addError(context, "MISSING_REQUEST_DTO", `Operation has no RequestDTO reference: ${operationLabel}`, filePath);
      }
      requestReferences.forEach(({ name, source }) => {
        if (!dtoNamePatterns.request.test(name || "")) {
          addError(context, "INVALID_REQUEST_DTO_NAME", `Request model must end with RequestDTO (${source}): ${name || "missing"}`, filePath);
        }
        if (typeof operation["x-request-dto"] === "string" && source !== "x-request-dto" && name !== operation["x-request-dto"]) {
          addError(context, "REQUEST_DTO_MISMATCH", `x-request-dto ${operation["x-request-dto"]} does not match ${name} in ${source}: ${operationLabel}`, filePath);
        }
        if (source === "x-request-dto" && (!document.components || !document.components.schemas || !document.components.schemas[name])) {
          addError(context, "REQUEST_DTO_NOT_DEFINED", `x-request-dto is not defined in components.schemas: ${name}`, filePath);
        }
      });

      const responses = operation.responses;
      if (!isRecord(responses)) {
        addError(context, "MISSING_RESPONSES", `Operation must define responses: ${operationLabel}`, filePath);
        return;
      }
      const responseEntries = Object.entries(responses);
      const successResponses = responseEntries.filter(([status]) => /^2\d\d$/.test(status));
      const errorResponses = responseEntries.filter(([status]) => /^[45]\d\d$/.test(status) || status === "default");
      if (successResponses.length === 0) {
        addError(context, "MISSING_SUCCESS_RESPONSE", `Operation must define a 2xx response: ${operationLabel}`, filePath);
      }
      if (errorResponses.length === 0) {
        addError(context, "MISSING_ERROR_RESPONSE", `Operation must define a 4xx/5xx or default error response: ${operationLabel}`, filePath);
      }

      successResponses.forEach(([status, response]) => {
        const schema = responseSchema(document, response);
        const schemaName = topLevelSchemaName(schema);
        if (!schemaName || !dtoNamePatterns.response.test(schemaName)) {
          addError(context, "SUCCESS_RESPONSE_DTO_MISSING", `2xx response must reference a ResponseDTO: ${status} ${operationLabel}`, filePath);
        }
        if (!findCodeValues(document, schema).includes("00000")) {
          addError(context, "SUCCESS_CODE_MISSING", `2xx response schema must expose business code 00000: ${status} ${operationLabel}`, filePath);
        }
      });

      errorResponses.forEach(([status, response]) => {
        const schema = responseSchema(document, response);
        if (!schema || findCodeValues(document, schema).every((code) => !/^A0000$|^B0000$|^C0000$/.test(code))) {
          addError(context, "ERROR_CODE_MISSING", `Error response must expose A0000, B0000, or C0000: ${status} ${operationLabel}`, filePath);
        }
      });

      operations.push({ apiId, method: method.toUpperCase(), path: pathTemplate, operation });
    });
  });

  return { document, operations };
}

function splitMarkdownTableRow(line) {
  const strippedLine = line.trim();
  if (!strippedLine.startsWith("|") || !strippedLine.includes("|", 1)) {
    return [];
  }
  return strippedLine.replace(/^\||\|$/g, "").split("|").map((cell) => cell.trim());
}

function isTableSeparator(cells) {
  return cells.length > 0 && cells.every((cell) => /^:?-{3,}:?$/.test(cell.replace(/\s/g, "")));
}

function findMarkdownTable(lines, requiredHeaders) {
  const normalizedHeaders = requiredHeaders.map(normalized);
  for (let lineIndex = 0; lineIndex < lines.length - 1; lineIndex += 1) {
    const headers = splitMarkdownTableRow(lines[lineIndex]);
    const separator = splitMarkdownTableRow(lines[lineIndex + 1]);
    if (headers.length === 0 || !isTableSeparator(separator)) {
      continue;
    }
    if (!normalizedHeaders.every((requiredHeader) => headers.some((header) => normalized(header).includes(requiredHeader)))) {
      continue;
    }
    const rows = [];
    for (let rowIndex = lineIndex + 2; rowIndex < lines.length; rowIndex += 1) {
      const row = splitMarkdownTableRow(lines[rowIndex]);
      if (row.length === 0) {
        break;
      }
      rows.push(row);
    }
    return { headers, rows, lineNumber: lineIndex + 1 };
  }
  return undefined;
}

function tableCell(table, row, header) {
  const normalizedHeader = normalized(header);
  const columnIndex = table.headers.findIndex((value) => normalized(value).includes(normalizedHeader));
  return columnIndex >= 0 ? String(row[columnIndex] || "").trim() : "";
}

function validateModificationHistory(context, source, filePath) {
  if (!source.includes("| Version | Who | When | Why / What |")) {
    addError(context, "MISSING_MODIFICATION_HISTORY", "Document must include the standard modification history table.", filePath);
  }
  if (placeholderPattern.test(source)) {
    addError(context, "DOCUMENT_PLACEHOLDER", "Document contains an unresolved placeholder.", filePath, lineNumberOf(source, "<"));
  }
}

function validateErrorCodeDocument(context, filePath) {
  const source = readTextFile(context, filePath, "Global error-code document");
  if (source === undefined) {
    return;
  }
  validateModificationHistory(context, source, filePath);
  [
    "## 1. 修改紀錄",
    "## 2. Global Rules",
    "## 3. Error Code Registry",
    "## 4. Shared Error Response Contract",
    "## 5. API Reference Rules"
  ].forEach((heading) => {
    if (!source.includes(heading)) {
      addError(context, "MISSING_ERROR_CODE_SECTION", `Global error-code document is missing section: ${heading}`, filePath);
    }
  });
  requiredErrorCodes.forEach((code) => {
    if (!new RegExp(`\\b${code}\\b`).test(source)) {
      addError(context, "MISSING_BUSINESS_CODE", `Global error-code document must define ${code}.`, filePath);
    }
  });
  ["HTTP status", "traceId", "ErrorResponseDTO", "docs/openapi.yaml"].forEach((term) => {
    if (!source.includes(term)) {
      addError(context, "MISSING_ERROR_CODE_RULE", `Global error-code document must mention ${term}.`, filePath);
    }
  });
}

function validateRequirementDocuments(context, requirementPaths, operations) {
  const requirementApiIds = new Set();
  requirementPaths.forEach((filePath) => {
    const source = readTextFile(context, filePath, "Requirement document");
    if (source === undefined) {
      return;
    }
    if (placeholderPattern.test(source)) {
      addError(context, "REQUIREMENT_PLACEHOLDER", "Requirement document contains an unresolved placeholder.", filePath, lineNumberOf(source, "<"));
    }
    const sourceWithoutCodeBlocks = source.replace(/```[\s\S]*?```/g, "");
    const apiIds = sourceWithoutCodeBlocks.match(/[a-z0-9]+(?:-[a-z0-9]+)+-(?:00[1-9]|0[1-9][0-9]|[1-9][0-9]{2})/g) || [];
    apiIds.forEach((apiId) => requirementApiIds.add(apiId));
  });

  if (operations.length > 0 && requirementApiIds.size === 0) {
    addError(context, "REQUIREMENT_API_MAPPING_MISSING", "Requirement documents must expose at least one API ID for SD traceability.");
  }
  requirementApiIds.forEach((apiId) => {
    if (!operations.some((operation) => operation.apiId === apiId)) {
      addError(context, "REQUIREMENT_API_ID_NOT_IN_OPENAPI", `Requirement API ID is not present in OpenAPI: ${apiId}`);
    }
  });
  operations.forEach((operation) => {
    if (operation.apiId && requirementApiIds.size > 0 && !requirementApiIds.has(operation.apiId)) {
      addWarning(context, "OPENAPI_API_ID_NOT_IN_REQUIREMENT", `OpenAPI API ID is not mentioned by the requirement documents: ${operation.apiId}`);
    }
  });
}

function validateArchitectureDocuments(context, architecturePaths) {
  architecturePaths.forEach((filePath) => {
    const source = readTextFile(context, filePath, "Architecture document");
    if (source === undefined) {
      return;
    }
    if (!source.trim()) {
      addError(context, "EMPTY_ARCHITECTURE", "Architecture document must not be empty.", filePath);
    }
    if (placeholderPattern.test(source)) {
      addError(context, "ARCHITECTURE_PLACEHOLDER", "Architecture document contains an unresolved placeholder.", filePath, lineNumberOf(source, "<"));
    }
  });
}

function validateApiFlowDocuments(context, apiDirectoryPath, operations) {
  const apiFiles = listMarkdownFiles(context, apiDirectoryPath, "API flow", operations.length > 0);
  const flowByApiId = new Map();
  apiFiles.forEach((filePath) => {
    const fileName = path.basename(filePath);
    const fileMatch = fileName.match(/^([a-z0-9]+(?:-[a-z0-9]+)+-(?:00[1-9]|0[1-9][0-9]|[1-9][0-9]{2}))_(.+)\.md$/);
    if (!fileMatch) {
      addError(context, "INVALID_API_FLOW_FILENAME", `API flow filename must use {api_id}_{name}.md: ${fileName}`, filePath);
      return;
    }
    const apiId = fileMatch[1];
    if (flowByApiId.has(apiId)) {
      addError(context, "DUPLICATE_API_FLOW", `More than one API flow document exists for ${apiId}.`, filePath);
      return;
    }
    flowByApiId.set(apiId, filePath);
  });

  const operationsByApiId = new Map(operations.filter((operation) => operation.apiId).map((operation) => [operation.apiId, operation]));
  operationsByApiId.forEach((operation, apiId) => {
    const filePath = flowByApiId.get(apiId);
    if (!filePath) {
      addError(context, "MISSING_API_FLOW", `OpenAPI API has no matching API flow document: ${apiId}`);
      return;
    }
    const source = readTextFile(context, filePath, "API flow document");
    if (source === undefined) {
      return;
    }
    validateModificationHistory(context, source, filePath);
    [
      "## 1. 修改紀錄",
      "## 2. API Flow",
      "### 2.1 Workflow Sequence Diagram",
      "## 3. Execute Business Logic",
      "### Detailed Logic",
      "### Given / When / Then",
      "## 4. 錯誤代碼 (Error Codes)",
      "### 4.2 API-specific Error Mapping"
    ].forEach((heading) => {
      if (!source.includes(heading)) {
        addError(context, "MISSING_API_FLOW_SECTION", `API flow document is missing section: ${heading}`, filePath);
      }
    });
    if (!source.includes("sequenceDiagram")) {
      addError(context, "MISSING_SEQUENCE_DIAGRAM", "API flow document must include a Mermaid sequenceDiagram.", filePath);
    }
    ["Given", "When", "Then"].forEach((keyword) => {
      if (!new RegExp(`^\\s*-\\s*${keyword}\\b.+\\S`, "m").test(source)) {
        addError(context, "MISSING_GIVEN_WHEN_THEN", `API flow document must include a substantive ${keyword} rule.`, filePath);
      }
    });
    if (!source.includes(apiId)) {
      addError(context, "API_FLOW_ID_MISMATCH", `API flow document must mention API ID ${apiId}.`, filePath);
    }
    if (!source.includes(operation.path)) {
      addError(context, "API_FLOW_PATH_MISMATCH", `API flow document must mention OpenAPI path ${operation.path}.`, filePath);
    }
    if (!new RegExp(`\\b${operation.method}\\b`).test(source)) {
      addError(context, "API_FLOW_METHOD_MISMATCH", `API flow document must mention HTTP method ${operation.method}.`, filePath);
    }
    if (!source.includes("docs/error-codes.md")) {
      addError(context, "MISSING_ERROR_CODE_REFERENCE", "API flow document must reference docs/error-codes.md.", filePath);
    }
    if (!/\b00000\b/.test(source) || !/\b(?:A0000|B0000|C0000)\b/.test(source)) {
      addError(context, "API_FLOW_BUSINESS_CODE_MISSING", "API flow document must map success and at least one business error code.", filePath);
    }
    if (/^\s*(?:openapi|components):\s*$/m.test(source) || /#\/components\/schemas\//.test(source)) {
      addWarning(context, "API_FLOW_SCHEMA_DUPLICATION", "API flow document appears to duplicate an OpenAPI schema; keep schema details in docs/openapi.yaml.", filePath);
    }
  });

  flowByApiId.forEach((filePath, apiId) => {
    if (!operationsByApiId.has(apiId)) {
      addError(context, "ORPHAN_API_FLOW", `API flow document has no matching OpenAPI API: ${apiId}`, filePath);
    }
  });
}

function validateSchemaDocuments(context, schemaDirectoryPath) {
  const schemaFiles = listMarkdownFiles(context, schemaDirectoryPath, "Schema", false);
  const tableNames = new Map();
  schemaFiles.forEach((filePath) => {
    const fileName = path.basename(filePath, ".md");
    if (!/^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$/.test(fileName)) {
      addError(context, "INVALID_SCHEMA_FILENAME", `Schema filename must be lower snake case: ${path.basename(filePath)}`, filePath);
    }
    const source = readTextFile(context, filePath, "Schema document");
    if (source === undefined) {
      return;
    }
    validateModificationHistory(context, source, filePath);
    ["## 1. 修改紀錄", "## 2. Schema", "### 2.3 欄位定義", "### 2.4 限制條件", "## 3. DDL"].forEach((heading) => {
      if (!source.includes(heading)) {
        addError(context, "MISSING_SCHEMA_SECTION", `Schema document is missing section: ${heading}`, filePath);
      }
    });

    const lines = source.split(/\r?\n/);
    const columnTable = findMarkdownTable(lines, ["Column", "Type", "Nullable", "Default", "Description", "API Mapping"]);
    if (!columnTable || columnTable.rows.every((row) => row.every(isEmptyValue))) {
      addError(context, "INVALID_COLUMN_DICTIONARY", "Schema 2.3 must contain a populated column dictionary table.", filePath);
    } else {
      ["Column", "Type", "Nullable", "Default", "Description", "API Mapping"].forEach((header) => {
        columnTable.rows.forEach((row, rowIndex) => {
          if (isEmptyValue(tableCell(columnTable, row, header))) {
            addError(context, "EMPTY_COLUMN_DICTIONARY_CELL", `Schema 2.3 row ${rowIndex + 1} has an empty ${header} cell.`, filePath, columnTable.lineNumber);
          }
        });
      });
    }
    const constraintTable = findMarkdownTable(lines, ["Constraint", "Type", "Definition / Intent", "Failure Handling"]);
    if (!constraintTable || constraintTable.rows.every((row) => row.every(isEmptyValue))) {
      addError(context, "INVALID_CONSTRAINT_DICTIONARY", "Schema 2.4 must contain a populated constraints table.", filePath);
    }

    const createTableMatches = [...source.matchAll(/\bCREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:[A-Za-z0-9_-]+\.)?["`]?([A-Za-z_][A-Za-z0-9_-]*)["`]?/gi)];
    if (createTableMatches.length !== 1) {
      addError(context, "DDL_TABLE_COUNT", `Schema document must contain exactly one CREATE TABLE statement; found ${createTableMatches.length}.`, filePath);
    } else {
      const tableName = createTableMatches[0][1];
      if (tableName !== fileName) {
        addError(context, "DDL_FILENAME_MISMATCH", `DDL table ${tableName} must match schema filename ${fileName}.`, filePath);
      }
      if (tableNames.has(tableName)) {
        addError(context, "DUPLICATE_TABLE_DOCUMENT", `Table ${tableName} is documented more than once: ${tableNames.get(tableName)}`, filePath);
      } else {
        tableNames.set(tableName, filePath);
      }
      if (!/\bPRIMARY\s+KEY\b|\bCONSTRAINT\b|\bUNIQUE\b/i.test(source)) {
        addWarning(context, "DDL_CONSTRAINTS_NOT_FOUND", "DDL does not show a primary key, constraint, or unique rule; confirm this is intentional.", filePath);
      }
    }
  });
}

function parseArguments(argumentsList) {
  const options = {
    projectRoot: projectRootPath,
    requirements: [],
    architectures: [],
    openapi: "docs/openapi.yaml",
    errorCodes: "docs/error-codes.md",
    apiDir: "docs/api",
    schemaDir: "docs/schema",
    format: "text",
    strict: false,
    help: false
  };
  const repeatableArguments = new Map([
    ["--requirement", "requirements"],
    ["--architecture", "architectures"]
  ]);
  const valueArguments = new Map([
    ["--project-root", "projectRoot"],
    ["--openapi", "openapi"],
    ["--error-codes", "errorCodes"],
    ["--api-dir", "apiDir"],
    ["--schema-dir", "schemaDir"],
    ["--format", "format"]
  ]);

  for (let argumentIndex = 0; argumentIndex < argumentsList.length; argumentIndex += 1) {
    const argument = argumentsList[argumentIndex];
    if (argument === "--help" || argument === "-h") {
      options.help = true;
      continue;
    }
    if (argument === "--strict") {
      options.strict = true;
      continue;
    }
    if (repeatableArguments.has(argument) || valueArguments.has(argument)) {
      const value = argumentsList[argumentIndex + 1];
      if (!value || value.startsWith("--")) {
        throw new Error(`${argument} requires a value.`);
      }
      const optionName = repeatableArguments.get(argument) || valueArguments.get(argument);
      if (repeatableArguments.has(argument)) {
        options[optionName].push(value);
      } else {
        options[optionName] = value;
      }
      argumentIndex += 1;
      continue;
    }
    throw new Error(`Unknown argument: ${argument}`);
  }

  if (!new Set(["text", "json"]).has(options.format)) {
    throw new Error(`Unsupported report format: ${options.format}`);
  }
  return options;
}

function validateProject(rawOptions) {
  const resolvedProjectRootPath = path.resolve(rawOptions.projectRoot);
  const context = {
    projectRootPath: resolvedProjectRootPath,
    issues: []
  };
  const openApiPath = resolveRepositoryPath(context, rawOptions.openapi);
  const errorCodePath = resolveRepositoryPath(context, rawOptions.errorCodes);
  const apiDirectoryPath = resolveRepositoryPath(context, rawOptions.apiDir);
  const schemaDirectoryPath = resolveRepositoryPath(context, rawOptions.schemaDir);
  const requirementPaths = resolveInputDocuments(context, rawOptions.requirements, "docs/requirements", "Requirement");
  const architecturePaths = resolveInputDocuments(context, rawOptions.architectures, "docs/architecture", "Architecture");
  const openApiResult = openApiPath
    ? validateOpenApiContract(context, openApiPath)
    : { document: undefined, operations: [] };

  validateRequirementDocuments(context, requirementPaths, openApiResult.operations);
  validateArchitectureDocuments(context, architecturePaths);
  if (errorCodePath) {
    validateErrorCodeDocument(context, errorCodePath);
  }
  if (apiDirectoryPath && errorCodePath) {
    validateApiFlowDocuments(context, apiDirectoryPath, openApiResult.operations);
  }
  if (schemaDirectoryPath) {
    validateSchemaDocuments(context, schemaDirectoryPath);
  }

  const errorCount = context.issues.filter((issue) => issue.severity === "ERROR").length;
  const warningCount = context.issues.filter((issue) => issue.severity === "WARNING").length;
  return {
    valid: errorCount === 0 && (!rawOptions.strict || warningCount === 0),
    errorCount,
    warningCount,
    operationCount: openApiResult.operations.length,
    apiIds: openApiResult.operations.map((operation) => operation.apiId).filter(Boolean),
    issues: context.issues
  };
}

function printReport(report, format, strict) {
  if (format === "json") {
    console.log(JSON.stringify({ ...report, strict }, null, 2));
    return;
  }
  report.issues.forEach((issue) => {
    const location = issue.file ? `${issue.file}${issue.line ? `:${issue.line}` : ""}` : "";
    console.log(`[${issue.severity}]${location ? ` ${location}` : ""} ${issue.code}: ${issue.message}`);
  });
  if (report.valid) {
    console.log(`[PASS] SD artifacts are valid: ${report.operationCount} API operation(s), ${report.warningCount} warning(s).`);
  } else {
    console.log(`[FAIL] SD artifacts are invalid: ${report.errorCount} error(s), ${report.warningCount} warning(s).`);
  }
}

function main(argumentsList) {
  try {
    const options = parseArguments(argumentsList);
    if (options.help) {
      console.log(usageText);
      return 0;
    }
    const report = validateProject(options);
    printReport(report, options.format, options.strict);
    return report.valid ? 0 : 1;
  } catch (error) {
    console.error(`ERROR: ${error.message}`);
    return 1;
  }
}

if (require.main === module) {
  process.exitCode = main(process.argv.slice(2));
}

module.exports = {
  apiIdPattern,
  findMarkdownTable,
  main,
  parseArguments,
  validateOpenApiContract,
  validateProject
};
