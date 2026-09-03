#!/usr/bin/env node

"use strict";

const fs = require("fs");
const path = require("path");

const projectRootPath = path.resolve(__dirname, "..");
const allowedStages = new Set(["S0", "S1", "S2", "S3", "S4", "S5A", "S5B", "S6", "S7"]);
const allowedStatuses = new Set([
  "not_started",
  "in_progress",
  "blocked",
  "needs_clarification",
  "needs_rework",
  "done"
]);
const usageText = `Usage:
  node scripts/validate-workflow-state.js <workflow-file>
  node scripts/validate-workflow-state.js --help
`;

const requiredFields = [
  "Workflow ID",
  "Scenario ID",
  "Owner Role",
  "Current Stage",
  "Overall Status",
  "Current Goal",
  "Recommended next action",
  "Files to read first"
];
const requiredSections = [
  "## 1. Metadata",
  "## 3. Workflow Graph",
  "## 4. Current Objective",
  "## 5. Stage Status",
  "## 6. Dependency / Blocking Status",
  "## 7. Parallel Work Plan",
  "## 8. Auto QA Loop",
  "## 9. Session Handoff Notes"
];

function readWorkflow(workflowPath) {
  const resolvedPath = path.resolve(projectRootPath, workflowPath);
  const relativePath = path.relative(projectRootPath, resolvedPath);
  if (relativePath.startsWith(`..${path.sep}`) || relativePath === "..") {
    throw new Error(`Workflow file must stay inside the repository: ${workflowPath}`);
  }
  try {
    return { resolvedPath, source: fs.readFileSync(resolvedPath, "utf8") };
  } catch (error) {
    if (error.code === "ENOENT") {
      throw new Error(`Workflow file does not exist: ${resolvedPath}`);
    }
    throw new Error(`Cannot read workflow file ${resolvedPath}: ${error.message}`);
  }
}

function fieldValue(source, fieldName) {
  const escapedFieldName = fieldName.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const fieldPattern = new RegExp(
    `^[ \\t]*-[ \\t]*${escapedFieldName}:[ \\t]*([^\\r\\n]*)`,
    "mi"
  );
  const fieldMatch = source.match(fieldPattern);
  return fieldMatch ? fieldMatch[1].trim() : undefined;
}

function validateWorkflow(workflowPath) {
  const { resolvedPath, source } = readWorkflow(workflowPath);
  const errors = [];

  for (const sectionName of requiredSections) {
    if (!source.includes(sectionName)) {
      errors.push(`missing section: ${sectionName}`);
    }
  }

  for (const fieldName of requiredFields) {
    const value = fieldValue(source, fieldName);
    if (
      value === undefined
      || (!value && fieldName !== "Files to read first")
      || /<[^>]+>/.test(value || "")
    ) {
      errors.push(`missing or unresolved field: ${fieldName}`);
    }
  }

  const workflowId = fieldValue(source, "Workflow ID");
  if (workflowId && !/^WF-[A-Z0-9]+(?:-[A-Z0-9]+)*-\d{3}$/.test(workflowId)) {
    errors.push(`invalid Workflow ID: ${workflowId}`);
  }

  const scenarioId = fieldValue(source, "Scenario ID");
  if (scenarioId && !/^SCN-[A-Z0-9]+(?:-[A-Z0-9]+)*-\d{3}$/.test(scenarioId)) {
    errors.push(`invalid Scenario ID: ${scenarioId}`);
  }

  const currentStage = fieldValue(source, "Current Stage");
  if (currentStage && !allowedStages.has(currentStage)) {
    errors.push(`invalid Current Stage: ${currentStage}`);
  }

  const overallStatus = fieldValue(source, "Overall Status");
  if (overallStatus && !allowedStatuses.has(overallStatus)) {
    errors.push(`invalid Overall Status: ${overallStatus}`);
  }

  if (errors.length > 0) {
    throw new Error(`Workflow state is invalid: ${resolvedPath}\n- ${errors.join("\n- ")}`);
  }

  console.log(`Workflow state is valid: ${resolvedPath}`);
}

try {
  const workflowPath = process.argv[2];
  if (!workflowPath || workflowPath === "--help" || workflowPath === "-h") {
    console.log(usageText);
    if (!workflowPath || workflowPath === "--help" || workflowPath === "-h") {
      process.exitCode = workflowPath ? 0 : 1;
    }
  } else if (process.argv.length > 3) {
    throw new Error(`Unknown argument: ${process.argv[3]}\n\n${usageText}`);
  } else {
    validateWorkflow(workflowPath);
  }
} catch (error) {
  console.error(`ERROR: ${error.message}`);
  process.exitCode = 1;
}
