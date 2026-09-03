#!/usr/bin/env node

import { access, readdir, readFile } from 'node:fs/promises'
import { existsSync } from 'node:fs'
import { dirname, extname, relative, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDirectory = dirname(fileURLToPath(import.meta.url))
const skillDirectory = resolve(scriptDirectory, '..')
const defaultRepositoryDirectory = resolve(skillDirectory, '../../..')
const requiredFiles = [
  '.codex/skills/qa-e2e-verifier/SKILL.md',
  '.codex/skills/qa-e2e-verifier/references/playwright-e2e-architecture.md',
  '.codex/skills/qa-e2e-verifier/references/qa-e2e-checklist.md',
  '.codex/skills/qa-e2e-verifier/references/qa-report-template.md',
  'docs/templates/qa-report-template.md',
  'apps/web/library-mini-admin-web/playwright.config.ts',
]

const errors = []
const warnings = []
const argumentsList = process.argv.slice(2)
const strictMode = argumentsList.includes('--strict')
let repositoryDirectory = defaultRepositoryDirectory

for (let argumentIndex = 0; argumentIndex < argumentsList.length; argumentIndex += 1) {
  const argument = argumentsList[argumentIndex]

  if (argument === '--help') {
    console.log('Usage: node validate-qa-skill.mjs [--strict] [--repo-root <path>]')
    process.exit(0)
  }

  if (argument === '--strict') {
    continue
  }

  if (argument === '--repo-root') {
    const repositoryArgument = argumentsList[argumentIndex + 1]

    if (!repositoryArgument) {
      errors.push('--repo-root requires a path')
      break
    }

    repositoryDirectory = resolve(repositoryArgument)
    argumentIndex += 1
    continue
  }

  errors.push(`Unknown argument: ${argument}`)
}

async function fileExists(filePath) {
  try {
    await access(filePath)
    return true
  } catch {
    return false
  }
}

async function readTextFile(filePath) {
  try {
    return await readFile(filePath, 'utf8')
  } catch {
    return null
  }
}

async function collectFiles(directoryPath, predicate) {
  if (!(await fileExists(directoryPath))) {
    return []
  }

  const entries = await readdir(directoryPath, { withFileTypes: true })
  const files = []

  for (const entry of entries) {
    const entryPath = resolve(directoryPath, entry.name)

    if (entry.isDirectory()) {
      files.push(...(await collectFiles(entryPath, predicate)))
      continue
    }

    if (entry.isFile() && predicate(entryPath)) {
      files.push(entryPath)
    }
  }

  return files
}

function addMissingFileErrors() {
  return Promise.all(
    requiredFiles.map(async (relativePath) => {
      const filePath = resolve(repositoryDirectory, relativePath)

      if (!(await fileExists(filePath))) {
        errors.push(`Missing required file: ${relativePath}`)
      }
    }),
  )
}

function checkMarkdownLinks(relativePath, content) {
  const sourcePath = resolve(repositoryDirectory, relativePath)
  const linkPattern = /\[[^\]]+\]\(([^)]+)\)/g
  let linkMatch = linkPattern.exec(content)

  while (linkMatch) {
    const rawTarget = linkMatch[1].trim()
    const target = rawTarget.split(/\s+/u, 1)[0].replace(/^<|>$/gu, '').split('#', 1)[0]

    if (
      target &&
      !/^(?:[a-z][a-z\d+.-]*:|\/\/)/iu.test(target) &&
      !target.startsWith('/')
    ) {
      const targetPath = resolve(dirname(sourcePath), target)

      if (!fileExistsSync(targetPath)) {
        errors.push(`Broken local Markdown link: ${relativePath} -> ${target}`)
      }
    }

    linkMatch = linkPattern.exec(content)
  }
}

function fileExistsSync(filePath) {
  return existsSync(filePath)
}

function checkRequiredPolicy(content) {
  const policies = [
    ['REQ/SCN trigger scope', /REQ-\* deliveries[\s\S]*linked SCN-\* scenarios/u],
    ['parallel worker caveat', /workers=1[\s\S]*parallelism has not been verified/u],
    ['evidence redaction', /redact tokens, passwords, cookies, authorization headers/u],
    ['maintainer command', /npm run qa:skill:check/u],
  ]

  for (const [policyName, policyPattern] of policies) {
    if (!policyPattern.test(content)) {
      errors.push(`Missing required policy: ${policyName}`)
    }
  }
}

function checkAgentDecoupling(relativePath, content) {
  if (/\.codex\/agents(?:\/|$)/u.test(content)) {
    errors.push(`Skill documentation must not depend on agent config: ${relativePath}`)
  }
}

function findLineNumber(content, matchIndex) {
  return content.slice(0, matchIndex).split('\n').length
}

function checkSensitiveEvidence(relativePath, content) {
  const sensitivePatterns = [
    ['private key', /-----BEGIN (?:RSA|EC|OPENSSH|DSA|PRIVATE) KEY-----/iu],
    [
      'bearer token',
      /(?:authorization|proxy-authorization)\s*:\s*bearer\s+(?!<|>|REDACTED\b|MASKED\b)[A-Za-z0-9._~+/=-]{20,}/iu,
    ],
    [
      'credential assignment',
      /(?:api[_-]?key|access[_-]?token|client[_-]?secret|password)\s*[:=]\s*["'`]?((?!<|>|REDACTED\b|MASKED\b|TODO\b|\$\{)[A-Za-z0-9._~+/=-]{12,})/iu,
    ],
  ]

  for (const [patternName, pattern] of sensitivePatterns) {
    const match = pattern.exec(content)

    if (match) {
      errors.push(
        `Possible unredacted ${patternName} in ${relativePath}:${findLineNumber(content, match.index)}`,
      )
    }
  }
}

async function main() {
  await addMissingFileErrors()

  const skillPath = resolve(repositoryDirectory, '.codex/skills/qa-e2e-verifier/SKILL.md')
  const skillContent = await readTextFile(skillPath)

  if (skillContent) {
    checkRequiredPolicy(skillContent)
  }

  const markdownFiles = await collectFiles(
    resolve(repositoryDirectory, '.codex/skills/qa-e2e-verifier'),
    (filePath) => extname(filePath) === '.md',
  )

  for (const markdownPath of markdownFiles) {
    const content = await readTextFile(markdownPath)

    if (!content) {
      errors.push(`Unable to read Markdown file: ${relative(repositoryDirectory, markdownPath)}`)
      continue
    }

    const relativePath = relative(repositoryDirectory, markdownPath)
    checkMarkdownLinks(relativePath, content)
    checkAgentDecoupling(relativePath, content)
  }

  const skillTemplatePath = resolve(
    repositoryDirectory,
    '.codex/skills/qa-e2e-verifier/references/qa-report-template.md',
  )
  const repositoryTemplatePath = resolve(repositoryDirectory, 'docs/templates/qa-report-template.md')
  const skillTemplate = await readTextFile(skillTemplatePath)
  const repositoryTemplate = await readTextFile(repositoryTemplatePath)

  if (skillTemplate && repositoryTemplate && skillTemplate !== repositoryTemplate) {
    errors.push('QA report templates are out of sync')
  }

  const playwrightConfigPath = resolve(
    repositoryDirectory,
    'apps/web/library-mini-admin-web/playwright.config.ts',
  )
  const playwrightConfig = await readTextFile(playwrightConfigPath)

  if (playwrightConfig) {
    if (!/testDir\s*:/u.test(playwrightConfig) || !/baseURL\s*:/u.test(playwrightConfig)) {
      errors.push('Playwright config must declare testDir and baseURL')
    }

    if (/workers\s*:\s*(?:1|process\.env\.CI\s*\?\s*1\b)/u.test(playwrightConfig)) {
      warnings.push(
        'Playwright config limits at least one mode to workers=1; use an explicit multi-worker run before marking parallelism Pass',
      )
    }
  }

  const reportFiles = await collectFiles(
    resolve(repositoryDirectory, 'docs/qa-report'),
    (filePath) => ['.md', '.log', '.txt'].includes(extname(filePath)),
  )

  for (const reportPath of reportFiles) {
    const content = await readTextFile(reportPath)

    if (content) {
      checkSensitiveEvidence(relative(repositoryDirectory, reportPath), content)
    }
  }

  const failed = errors.length > 0 || (strictMode && warnings.length > 0)
  const result = failed ? 'FAIL' : 'PASS'

  console.log(`QA skill validation: ${result}`)
  console.log(`Repository: ${repositoryDirectory}`)

  for (const warning of warnings) {
    console.warn(`WARN: ${warning}`)
  }

  for (const error of errors) {
    console.error(`ERROR: ${error}`)
  }

  if (strictMode && warnings.length > 0) {
    console.error('ERROR: --strict treats warnings as failures')
  }

  process.exitCode = failed ? 1 : 0
}

await main()
