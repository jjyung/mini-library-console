#!/usr/bin/env node

"use strict";

const fs = require("fs");
const path = require("path");

const projectRootPath = path.resolve(__dirname, "..");
const maxAgentConfigLines = 60;
const skillReferencePattern = /\$([a-z0-9][a-z0-9-]*)/g;
const embeddedSectionPattern = /^#{1,6}\s/m;
const agentConfigReferencePattern = /\.codex[\\/]agents(?:[\\/]|$)/;

const usageText = `Usage:
  node scripts/validate-skill-agent-binding.js [options]

Options:
  --repo-root <path>  Repository root. Defaults to the current repository.
  --strict            Treat warnings as validation errors.
  --help              Show this help.

Checks:
  - every registered agent config file exists
  - every agent config references at least one existing skill via $<skill-name>
  - agent configs stay thin adapters (no embedded workflow headings)
  - skill frontmatter declares name/description matching the skill directory
  - skills remain self-contained and never reference .codex/agents
  - every skill is bound to at least one agent
`;

function parseArguments(argumentsList, context) {
  const options = { strict: false, help: false, projectRoot: projectRootPath };

  for (let index = 0; index < argumentsList.length; index += 1) {
    const argument = argumentsList[index];

    if (argument === "--help") {
      options.help = true;
      continue;
    }

    if (argument === "--strict") {
      options.strict = true;
      continue;
    }

    if (argument === "--repo-root") {
      const repositoryArgument = argumentsList[index + 1];
      if (!repositoryArgument) {
        throw new Error("--repo-root requires a path");
      }
      options.projectRoot = path.resolve(repositoryArgument);
      index += 1;
      continue;
    }

    throw new Error(`Unknown argument: ${argument}`);
  }

  context.projectRootPath = options.projectRoot;
  return options;
}

function readText(filePath) {
  try {
    return fs.readFileSync(filePath, "utf8");
  } catch (error) {
    return undefined;
  }
}

function listFiles(directoryPath, extension) {
  if (!fs.existsSync(directoryPath)) {
    return [];
  }

  const files = [];
  for (const entry of fs.readdirSync(directoryPath, { withFileTypes: true })) {
    const entryPath = path.join(directoryPath, entry.name);
    if (entry.isDirectory()) {
      files.push(...listFiles(entryPath, extension));
    } else if (!extension || entry.name.endsWith(extension)) {
      files.push(entryPath);
    }
  }
  return files;
}

function relativeToProject(filePath, context) {
  return path.relative(context.projectRootPath, filePath) || ".";
}

function parseRegisteredAgents(configSource) {
  const registeredAgents = [];
  let currentAgent;

  for (const line of configSource.split(/\r?\n/)) {
    const sectionMatch = line.match(/^\s*\[agents\.([a-z0-9-]+)\]\s*$/);
    if (sectionMatch) {
      currentAgent = { name: sectionMatch[1] };
      registeredAgents.push(currentAgent);
      continue;
    }

    if (/^\s*\[/.test(line)) {
      currentAgent = undefined;
      continue;
    }

    if (currentAgent) {
      const configFileMatch = line.match(/^\s*config_file\s*=\s*"([^"]+)"/);
      if (configFileMatch) {
        currentAgent.configFile = configFileMatch[1];
      }
    }
  }

  return registeredAgents;
}

function extractSkillReferences(instructions) {
  const references = new Set();
  let match = skillReferencePattern.exec(instructions);

  while (match) {
    references.add(match[1]);
    match = skillReferencePattern.exec(instructions);
  }

  return references;
}

function checkRegisteredAgents(context, registeredAgents) {
  const configDirectory = path.join(context.projectRootPath, ".codex");
  const registeredPaths = new Set();

  for (const agent of registeredAgents) {
    if (!agent.configFile) {
      context.errors.push(`Agent ${agent.name} is registered without config_file`);
      continue;
    }

    const configFilePath = path.resolve(configDirectory, agent.configFile);
    registeredPaths.add(configFilePath);
    if (!fs.existsSync(configFilePath)) {
      context.errors.push(
        `Agent ${agent.name} points to a missing config file: ${agent.configFile}`,
      );
    }
  }

  return registeredPaths;
}

function checkAgentConfig(agentPath, context) {
  const relativeAgentPath = relativeToProject(agentPath, context);
  const source = readText(agentPath);

  if (source === undefined) {
    context.errors.push(`Cannot read agent config: ${relativeAgentPath}`);
    return new Set();
  }

  const instructionsMatch = source.match(/developer_instructions\s*=\s*"""([\s\S]*?)"""/);
  if (!instructionsMatch) {
    context.errors.push(`${relativeAgentPath} has no developer_instructions block`);
    return new Set();
  }

  const instructions = instructionsMatch[1];
  const skillReferences = extractSkillReferences(instructions);

  if (skillReferences.size === 0) {
    context.errors.push(
      `${relativeAgentPath} must route to at least one skill via $<skill-name>`,
    );
  }

  for (const skillName of skillReferences) {
    const skillPath = path.join(context.skillsDirectoryPath, skillName, "SKILL.md");
    if (!fs.existsSync(skillPath)) {
      context.errors.push(
        `${relativeAgentPath} references unknown skill $${skillName}`,
      );
    }
  }

  if (embeddedSectionPattern.test(instructions)) {
    context.errors.push(
      `${relativeAgentPath} embeds a Markdown section; move workflow content into a skill`,
    );
  }

  const lineCount = source.split(/\r?\n/).length;
  if (lineCount > maxAgentConfigLines) {
    context.warnings.push(
      `${relativeAgentPath} has ${lineCount} lines; keep agent configs as thin adapters (<= ${maxAgentConfigLines})`,
    );
  }

  return skillReferences;
}

function parseFrontmatter(source) {
  const frontmatterMatch = source.match(/^---\r?\n([\s\S]*?)\r?\n---/);
  if (!frontmatterMatch) {
    return undefined;
  }

  const frontmatter = {};
  for (const line of frontmatterMatch[1].split(/\r?\n/)) {
    const fieldMatch = line.match(/^([A-Za-z0-9_-]+):\s*(.*)$/);
    if (fieldMatch) {
      frontmatter[fieldMatch[1]] = fieldMatch[2].trim();
    }
  }
  return frontmatter;
}

function checkSkill(skillDirectoryPath, context) {
  const skillName = path.basename(skillDirectoryPath);
  const relativeSkillDirectory = relativeToProject(skillDirectoryPath, context);
  const skillPath = path.join(skillDirectoryPath, "SKILL.md");
  const source = readText(skillPath);

  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(skillName)) {
    context.errors.push(
      `${relativeSkillDirectory} must use a lowercase kebab-case skill directory name`,
    );
  }

  if (source === undefined) {
    context.errors.push(`Missing skill file: ${relativeToProject(skillPath, context)}`);
    return false;
  }

  const frontmatter = parseFrontmatter(source);
  if (!frontmatter) {
    context.errors.push(`${relativeSkillDirectory}/SKILL.md has no YAML frontmatter`);
    return false;
  }

  if (!frontmatter.name || !frontmatter.description) {
    context.errors.push(
      `${relativeSkillDirectory}/SKILL.md frontmatter must declare name and description`,
    );
  }

  if (frontmatter.name && frontmatter.name !== skillName) {
    context.errors.push(
      `${relativeSkillDirectory}/SKILL.md frontmatter name must match its directory`,
    );
  }

  if (!frontmatter.version) {
    context.warnings.push(`${relativeSkillDirectory}/SKILL.md is missing frontmatter version`);
  }

  if (!frontmatter.owner) {
    context.warnings.push(`${relativeSkillDirectory}/SKILL.md is missing frontmatter owner`);
  }

  const interfacePath = path.join(skillDirectoryPath, "agents", "openai.yaml");
  if (!fs.existsSync(interfacePath)) {
    context.warnings.push(`${relativeSkillDirectory} has no agents/openai.yaml interface file`);
  }

  return true;
}

function checkSkillDecoupling(context) {
  for (const markdownPath of context.skillMarkdownFiles) {
    const source = readText(markdownPath);
    if (source && agentConfigReferencePattern.test(source)) {
      context.errors.push(
        `Skill documentation must not depend on agent config: ${relativeToProject(markdownPath, context)}`,
      );
    }
  }
}

function validateProject(context) {
  const configPath = path.join(context.projectRootPath, ".codex", "config.toml");
  const agentsDirectoryPath = path.join(context.projectRootPath, ".codex", "agents");
  const skillsDirectoryPath = context.skillsDirectoryPath;
  const configSource = readText(configPath);
  let registeredPaths = new Set();
  if (configSource === undefined) {
    context.errors.push(`Missing config file: ${relativeToProject(configPath, context)}`);
  } else {
    registeredPaths = checkRegisteredAgents(context, parseRegisteredAgents(configSource));
  }

  const boundSkills = new Set();
  for (const agentPath of listFiles(agentsDirectoryPath, ".toml").sort()) {
    if (!registeredPaths.has(path.resolve(agentPath))) {
      context.errors.push(
        `${relativeToProject(agentPath, context)} is not registered in .codex/config.toml`,
      );
    }

    for (const skillName of checkAgentConfig(agentPath, context)) {
      boundSkills.add(skillName);
    }
  }

  const skillDirectories = fs.existsSync(skillsDirectoryPath)
    ? fs
        .readdirSync(skillsDirectoryPath, { withFileTypes: true })
        .filter((entry) => entry.isDirectory())
        .map((entry) => path.join(skillsDirectoryPath, entry.name))
        .sort()
    : [];

  context.skillMarkdownFiles = listFiles(skillsDirectoryPath, ".md");

  for (const skillDirectoryPath of skillDirectories) {
    if (checkSkill(skillDirectoryPath, context)) {
      const skillName = path.basename(skillDirectoryPath);
      if (!boundSkills.has(skillName)) {
        context.warnings.push(`Skill ${skillName} is not bound to any agent config`);
      }
    }
  }

  checkSkillDecoupling(context);
}

function printReport(context, options) {
  const failed = context.errors.length > 0 || (options.strict && context.warnings.length > 0);

  console.log(`Skill-agent binding validation: ${failed ? "FAIL" : "PASS"}`);
  console.log(`Repository: ${context.projectRootPath}`);

  for (const warning of context.warnings) {
    console.warn(`WARN: ${warning}`);
  }

  for (const error of context.errors) {
    console.error(`ERROR: ${error}`);
  }

  if (options.strict && context.warnings.length > 0) {
    console.error("ERROR: --strict treats warnings as failures");
  }

  console.log(
    `Summary: ${context.errors.length} error(s), ${context.warnings.length} warning(s)`,
  );

  return failed ? 1 : 0;
}

function main(argumentsList) {
  const context = {
    projectRootPath,
    skillsDirectoryPath: path.join(projectRootPath, ".codex", "skills"),
    errors: [],
    warnings: [],
    skillMarkdownFiles: [],
  };

  try {
    const options = parseArguments(argumentsList, context);
    if (options.help) {
      console.log(usageText);
      return 0;
    }

    context.projectRootPath = options.projectRoot;
    context.skillsDirectoryPath = path.join(options.projectRoot, ".codex", "skills");
    validateProject(context);
    return printReport(context, options);
  } catch (error) {
    console.error(`ERROR: ${error.message}`);
    return 1;
  }
}

if (require.main === module) {
  process.exitCode = main(process.argv.slice(2));
}

module.exports = {
  main,
  parseArguments,
  parseRegisteredAgents,
  parseFrontmatter,
  validateProject,
};
