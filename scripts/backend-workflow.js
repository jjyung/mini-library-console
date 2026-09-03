#!/usr/bin/env node

"use strict";

const childProcess = require("child_process");
const fs = require("fs");
const path = require("path");

const projectRootPath = path.resolve(__dirname, "..");
const apiRootPath = path.join(
  projectRootPath,
  "apps",
  "api",
  "library-mini-admin-api"
);
const apiPomPath = path.join(apiRootPath, "pom.xml");
const openApiContractPath = path.join(projectRootPath, "docs", "openapi.yaml");
const openApiMavenProfileName = "api-generation";
const defaultChangelogPath = path.join(
  apiRootPath,
  "src",
  "main",
  "resources",
  "db",
  "changelog"
);

const usageText = `Usage:
  node scripts/backend-workflow.js api:generate
  node scripts/backend-workflow.js api:verify-generated --generated-path <path> [--generated-path <path>]
  node scripts/backend-workflow.js api:check --generated-path <path> [--generated-path <path>]
  node scripts/backend-workflow.js backend:check
  node scripts/backend-workflow.js db:validate [--changelog <path>]

The API commands require a pinned OpenAPI Generator Maven plugin, a tracked
docs/openapi.yaml contract, and an explicit non-target output directory.
`;

function fail(message) {
  console.error(`ERROR: ${message}`);
  process.exitCode = 1;
}

function readText(filePath, description) {
  try {
    return fs.readFileSync(filePath, "utf8");
  } catch (error) {
    if (error.code === "ENOENT") {
      throw new Error(`${description} does not exist: ${filePath}`);
    }
    throw new Error(`Cannot read ${description} ${filePath}: ${error.message}`);
  }
}

function parseArguments(argumentsList) {
  const options = { generatedPaths: [], changelogPath: defaultChangelogPath };

  for (let argumentIndex = 0; argumentIndex < argumentsList.length; argumentIndex += 1) {
    const argument = argumentsList[argumentIndex];
    if (argument === "--generated-path" || argument === "--changelog") {
      const value = argumentsList[argumentIndex + 1];
      if (!value || value.startsWith("--")) {
        throw new Error(`${argument} requires a path value.`);
      }
      if (argument === "--generated-path") {
        options.generatedPaths.push(value);
      } else {
        options.changelogPath = value;
      }
      argumentIndex += 1;
      continue;
    }

    throw new Error(`Unknown argument: ${argument}`);
  }

  return options;
}

function findPluginBlock(pomSource) {
  const pluginBlocks = pomSource.match(/<plugin>[\s\S]*?<\/plugin>/g) || [];
  return pluginBlocks.find((pluginBlock) =>
    /<groupId>\s*org\.openapitools\s*<\/groupId>/.test(pluginBlock)
    && /<artifactId>\s*openapi-generator-maven-plugin\s*<\/artifactId>/.test(pluginBlock)
  );
}

function findProfileBlock(pomSource, profileName) {
  const profileBlocks = pomSource.match(/<profile>[\s\S]*?<\/profile>/g) || [];
  return profileBlocks.find((profileBlock) => {
    const profileIdMatch = profileBlock.match(/<id>\s*([^<]+?)\s*<\/id>/);
    return profileIdMatch && profileIdMatch[1].trim() === profileName;
  });
}

function findMavenProperty(pomSource, propertyName) {
  const escapedPropertyName = propertyName.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const propertyPattern = new RegExp(
    `<${escapedPropertyName}>\\s*([^<]+?)\\s*</${escapedPropertyName}>`
  );
  const propertyMatch = pomSource.match(propertyPattern);
  return propertyMatch ? propertyMatch[1].trim() : undefined;
}

function resolveMavenValue(pomSource, value) {
  const propertyMatch = value.match(/^\$\{([^}]+)}$/);
  if (!propertyMatch) {
    return value;
  }
  return findMavenProperty(pomSource, propertyMatch[1]);
}

function validateOpenApiConfiguration() {
  const pomSource = readText(apiPomPath, "API POM");
  const profileBlock = findProfileBlock(pomSource, openApiMavenProfileName);
  if (!profileBlock) {
    throw new Error(
      `pom.xml must define the ${openApiMavenProfileName} Maven profile for API generation.`
    );
  }
  const pluginBlock = findPluginBlock(profileBlock);
  if (!pluginBlock) {
    throw new Error(
      "pom.xml must configure org.openapitools:openapi-generator-maven-plugin "
        + "before an API generation task can run."
    );
  }

  const versionMatch = pluginBlock.match(/<version>\s*([^<]+?)\s*<\/version>/);
  const pluginVersion = versionMatch
    ? resolveMavenValue(pomSource, versionMatch[1].trim())
    : undefined;
  if (!pluginVersion || pluginVersion === "latest" || /[<>=*]|\s/.test(pluginVersion)) {
    throw new Error(
      "OpenAPI Generator Maven plugin version must be explicitly pinned in pom.xml."
    );
  }

  if (!/<inputSpec>\s*[^<]+\s*<\/inputSpec>/.test(pluginBlock)) {
    throw new Error("OpenAPI Generator plugin must declare an inputSpec in pom.xml.");
  }

  const outputMatch = pluginBlock.match(/<output>\s*([^<]+?)\s*<\/output>/);
  if (!outputMatch) {
    throw new Error(
      "OpenAPI Generator plugin must declare an explicit tracked output directory."
    );
  }
  const outputDirectory = resolveMavenValue(pomSource, outputMatch[1].trim());
  if (!outputDirectory || /(^|[\\/])target([\\/]|$)/.test(outputDirectory)) {
    throw new Error(
      "OpenAPI Generator output must be a tracked source/resource directory, not target/."
    );
  }

  if (!fs.existsSync(openApiContractPath)) {
    throw new Error(`OpenAPI contract does not exist: ${openApiContractPath}`);
  }
  const contractSource = readText(openApiContractPath, "OpenAPI contract");
  const contractLines = contractSource.split(/\r?\n/);
  const infoLineIndex = contractLines.findIndex((line) => line.trim() === "info:");
  const infoEndLineIndex = infoLineIndex === -1
    ? -1
    : contractLines.findIndex(
      (line, lineIndex) => lineIndex > infoLineIndex
        && line.trim().length > 0
        && !/^[ \t]/.test(line)
    );
  const infoLines = infoLineIndex === -1
    ? []
    : contractLines.slice(
      infoLineIndex + 1,
      infoEndLineIndex === -1 ? contractLines.length : infoEndLineIndex
    );
  const contractVersionMatch = infoLines
    .join("\n")
    .match(/^\s+version:\s*([^\s#]+)/m);
  if (!contractVersionMatch) {
    throw new Error("docs/openapi.yaml must declare info.version.");
  }

  console.log(
    `OpenAPI configuration ready: generator ${pluginVersion}, contract ${openApiContractPath}`
  );
}

function runMaven(mavenArguments) {
  const isWindowsPlatform = process.platform === "win32";
  const command = isWindowsPlatform ? "cmd.exe" : "./mvnw";
  const mavenLocalRepositoryArgument = `-Dmaven.repo.local=${path.join(projectRootPath, ".m2-local")}`;
  const commandArguments = isWindowsPlatform
    ? ["/d", "/s", "/c", "mvnw.cmd", mavenLocalRepositoryArgument, ...mavenArguments]
    : [mavenLocalRepositoryArgument, ...mavenArguments];

  console.log(`Running Maven from ${apiRootPath}: ${command} ${commandArguments.join(" ")}`);
  const result = childProcess.spawnSync(command, commandArguments, {
    cwd: apiRootPath,
    stdio: "inherit"
  });

  if (result.error) {
    throw new Error(`Failed to start Maven: ${result.error.message}`);
  }
  if (result.status !== 0) {
    throw new Error(`Maven exited with status ${result.status ?? "unknown"}.`);
  }
}

function resolveRepositoryPath(repositoryPath) {
  const resolvedPath = path.resolve(projectRootPath, repositoryPath);
  const relativePath = path.relative(projectRootPath, resolvedPath);
  if (relativePath.startsWith(`..${path.sep}`) || relativePath === "..") {
    throw new Error(`Path must stay inside the repository: ${repositoryPath}`);
  }
  return resolvedPath;
}

function verifyGeneratedPaths(generatedPaths) {
  if (generatedPaths.length === 0) {
    throw new Error(
      "api:verify-generated requires at least one --generated-path. "
        + "Read the POM output configuration and pass every tracked generated source/resource path."
    );
  }

  const resolvedPaths = generatedPaths.map(resolveRepositoryPath);
  const repositoryRelativePaths = resolvedPaths.map((generatedPath) =>
    path.relative(projectRootPath, generatedPath)
  );
  const missingPaths = resolvedPaths.filter((generatedPath) => !fs.existsSync(generatedPath));
  if (missingPaths.length > 0) {
    throw new Error(`Generated path does not exist after generation: ${missingPaths.join(", ")}`);
  }

  const statusResult = childProcess.spawnSync(
    "git",
    ["status", "--short", "--untracked-files=all", "--", ...repositoryRelativePaths],
    { cwd: projectRootPath, encoding: "utf8" }
  );
  if (statusResult.error || statusResult.status !== 0) {
    throw new Error(`Unable to inspect generated paths with git: ${statusResult.stderr || statusResult.error}`);
  }
  if (statusResult.stdout.trim()) {
    throw new Error(
      "Generated paths are modified or untracked. Commit generator output and do not edit it manually:\n"
        + statusResult.stdout.trim()
    );
  }

  console.log(`Generated paths are reproducible and clean: ${repositoryRelativePaths.join(", ")}`);
}

function walkFiles(directoryPath) {
  const entries = fs.readdirSync(directoryPath, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const entryPath = path.join(directoryPath, entry.name);
    return entry.isDirectory() ? walkFiles(entryPath) : [entryPath];
  });
}

function validateLiquibaseChangelog(changelogPath) {
  const resolvedChangelogPath = resolveRepositoryPath(changelogPath);
  if (!fs.existsSync(resolvedChangelogPath)) {
    throw new Error(`Liquibase changelog path does not exist: ${resolvedChangelogPath}`);
  }

  const changelogFiles = fs.statSync(resolvedChangelogPath).isDirectory()
    ? walkFiles(resolvedChangelogPath)
    : [resolvedChangelogPath];
  const xmlFiles = changelogFiles.filter((filePath) => filePath.toLowerCase().endsWith(".xml"));
  if (xmlFiles.length > 0) {
    throw new Error(`Liquibase XML changelogs are prohibited: ${xmlFiles.join(", ")}`);
  }

  const sqlFiles = changelogFiles.filter((filePath) => filePath.toLowerCase().endsWith(".sql"));
  if (sqlFiles.length === 0) {
    throw new Error(`No Liquibase formatted SQL changelog found under ${resolvedChangelogPath}`);
  }

  const changesetKeys = new Map();
  for (const sqlFile of sqlFiles) {
    const source = readText(sqlFile, "Liquibase changelog");
    const firstMeaningfulLine = source
      .split(/\r?\n/)
      .map((line) => line.trim())
      .find((line) => line.length > 0);
    if (!/^--liquibase formatted sql$/i.test(firstMeaningfulLine || "")) {
      throw new Error(
        `Liquibase SQL must start with '--liquibase formatted sql': ${sqlFile}`
      );
    }

    const changesetMatches = [
      ...source.matchAll(/^\s*--changeset\s+(\S+):(\S+)(?:\s+.*)?$/gim)
    ];
    if (changesetMatches.length === 0) {
      throw new Error(`Liquibase SQL has no changeset declaration: ${sqlFile}`);
    }
    for (const changesetMatch of changesetMatches) {
      const changesetKey = `${changesetMatch[1]}:${changesetMatch[2]}`;
      if (changesetKeys.has(changesetKey)) {
        throw new Error(
          `Duplicate Liquibase changeset ${changesetKey}: ${sqlFile} and ${changesetKeys.get(changesetKey)}`
        );
      }
      changesetKeys.set(changesetKey, sqlFile);
    }
  }

  console.log(
    `Liquibase formatted SQL is valid: ${sqlFiles.length} file(s), ${changesetKeys.size} changeset(s)`
  );
}

function execute(command, options) {
  switch (command) {
    case "api:generate":
      validateOpenApiConfiguration();
      runMaven([`-P${openApiMavenProfileName}`, "generate-sources"]);
      return;
    case "api:verify-generated":
      validateOpenApiConfiguration();
      runMaven([`-P${openApiMavenProfileName}`, "generate-sources"]);
      verifyGeneratedPaths(options.generatedPaths);
      return;
    case "api:check":
      validateOpenApiConfiguration();
      runMaven([`-P${openApiMavenProfileName}`, "generate-sources"]);
      verifyGeneratedPaths(options.generatedPaths);
      runMaven(["test"]);
      return;
    case "backend:check":
      runMaven(["test"]);
      return;
    case "db:validate":
      validateLiquibaseChangelog(options.changelogPath);
      return;
    case "--help":
    case "-h":
    case undefined:
      console.log(usageText);
      return;
    default:
      throw new Error(`Unknown command: ${command}\n\n${usageText}`);
  }
}

try {
  const command = process.argv[2];
  const options = parseArguments(process.argv.slice(3));
  execute(command, options);
} catch (error) {
  fail(error.message);
}
