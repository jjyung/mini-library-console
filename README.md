# Library Mini Admin Console

這個 repo 是一個示範 **Codex multi-agent collaboration** 與 **artifact-driven workflow** 的教學專案。業務場景是 library mini admin console，但 README 的重點不是 Library CRUD 本身，而是如何用 `orchestrator + role agents + workflow state` 完成一條可重現、可接手、可回溯的 scenario-driven delivery。

目前 repo 已附一條完整範例流程：

- Scenario: `SCN-LIB-001`
- Workflow: `WF-LIB-001`

如果你想看的是 Codex multi-agent 怎麼設定、角色怎麼分工、workflow 怎麼接手，以及怎麼 demo，這份 README 就是入口。

## Why This Repo

這個專案刻意把範圍收斂成一個小型 Web app，好讓焦點放在 workflow，而不是產品複雜度。

- 用最小可運行案例示範多角色協作
- 用 `docs/workflows/WF-LIB-001.md` 保存跨 session 的 workflow state
- 用文件 artifact 而不是聊天紀錄當 source of truth
- 用清楚的角色邊界降低單一 agent 包辦所有決策的風險

你可以用這個 repo 觀察兩種工作方式：

- `agent-by-agent`：逐角色推進，清楚看到每一站的輸入、輸出與 handoff
- `orchestrator`：由協調角色自動判斷 workflow 狀態並推進下一步

另外也包含第三種很實用的展示方式：

- `resume / handoff`：中斷後靠 workflow state 恢復上下文，不依賴前一段聊天

## Codex Multi-Agent 設定方式

這個 repo 的 multi-agent 設定已經完成，重點是理解它怎麼組成。

### 1. `AGENTS.md` 是 repo-level contract

`AGENTS.md` 定義這個 repo 的共同規範，包括：

- 工程規範與命名要求
- OpenAPI / DTO / error code 規則
- workflow stage 與 handoff 要求
- `docs/workflows/WF-*.md` 的維護責任

Codex 在開始工作前應先讀 `AGENTS.md`，再讀對應 scenario 與 workflow artifact。

### 2. `.codex/config.toml` 開啟 multi-agent

這個 repo 透過 `.codex/config.toml` 啟用多角色設定：

```toml
[features]
multi_agent = true

[agents.general]
config_file = "agents/orchestrator.toml"
```

已註冊的主要 agent 如下：

- `general`：對應 orchestrator
- `sa`
- `archi`
- `sd`
- `pg`
- `qa`

這代表你不需要從零設定 agent registry；README 主要是解釋結構與使用方式。

### 3. `.codex/agents/*.toml` 定義角色邊界

每個角色都有自己的 prompt / boundary 設定，例如：

- `.codex/agents/orchestrator.toml`
- `.codex/agents/sa.toml`
- `.codex/agents/archi.toml`
- `.codex/agents/sd.toml`
- `.codex/agents/pg.toml`
- `.codex/agents/qa.toml`

這些檔案定義的是角色責任，不是 workflow state 本身。workflow 狀態仍以 `docs/workflows/WF-*.md` 為準。

### 4. `.codex/skills/*` 是可重用 workflow skills

skills 放在 `.codex/skills/`，例如：

- `scenario-requirements-writer`
- `architecture-planner`
- `sd-docs-producer`
- `pg-task-orchestrator`
- `qa-e2e-verifier`

這些 skill 提供可重用的工作流程與檢查清單，並且依 repo 規範和 agent config 解耦。

## Agent Roles 與 Workflow 位置

本 repo 使用的 workflow 形狀如下：

```text
S0 Scenario Discovery
  -> S1 SA
  -> S2 Archi
  -> S3 SD
  -> S4 PG
  -> S5A FE || S5B BE
  -> S6 QA
  -> S7 Done
```

重點不是單一 agent 有多強，而是每個角色只在自己應負責的階段做決策。

| Role | Stage | Responsibility | Main Inputs | Main Outputs |
| --- | --- | --- | --- | --- |
| `orchestrator` | cross-stage | 協調整體 workflow、判斷下一步、維護 workflow state、管理 rework loop | `README.md`, `AGENTS.md`, scenario, workflow state, upstream artifacts | 更新後的 workflow state、下一步建議、正確角色 handoff |
| `SA` | `S1` | 從 scenario / Figma 整理需求，產出 FR / NFR / AC | `docs/scenarios/SCN-LIB-001.md`, `docs/figma/*` | `docs/requirements/REQ-LIB-001.md` |
| `Archi` | `S2` | 定義 MVP architecture、NFR tradeoff、成本與複雜度取捨 | `REQ-LIB-001` | `docs/architecture/ARCH-LIB-001.md` |
| `SD` | `S3` | 產出 implementation-ready 設計，包含 OpenAPI、schema、API flow | `REQ-LIB-001`, `ARCH-LIB-001` | `docs/openapi.yaml`, `docs/schema/*`, `docs/api/*` |
| `PG` | `S4` | 規劃任務、凍結 FE/BE 切分、協調實作與驗收門檻 | requirements, architecture, SD artifacts | `docs/tasks/TASK-LIB-001_mvp-delivery.md` |
| `FE / BE` | `S5A` / `S5B` | 在 PG 協調下平行實作前後端 | task plan, frozen API contract, acceptance criteria | `apps/web/*`, `apps/api/*` |
| `QA` | `S6` | 執行 E2E / NFR 驗證，必要時觸發回圈 | running app, REQ, workflow state, stable test ids | `docs/qa-report.md`, defect feedback |

補充兩點：

- `PG` 是協調與交付角色，不等於單人包辦所有 FE/BE 細節
- 本 repo 的 source of truth 不是聊天紀錄，而是 [docs/workflows/WF-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/workflows/WF-LIB-001.md)

## Artifact-Driven Workflow State

這個 repo 的核心不是「多開幾個 agent」，而是把 workflow 狀態保存成 artifact，讓不同 agent 或不同 session 都能安全接手。

workflow state 檔案位置：

```text
docs/workflows/WF-<DOMAIN>-<NNN>.md
```

本 repo 的範例是：

- [WF-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/workflows/WF-LIB-001.md)

它至少會記錄：

- workflow metadata
- current stage
- current objective
- stage-by-stage status
- blockers and open questions
- FE / BE parallel plan
- QA loop status
- session handoff notes

因此，當你換一個 agent、換一個 session，甚至中途停下來，仍然能靠 workflow file 恢復上下文，而不是靠「記得上次聊到哪」。

## Quick Start

如果你只是要看 multi-agent workflow，不必先研究完整 domain logic。先把專案跑起來即可。

### Prerequisites

- Node.js 20+
- Java 21+
- `npm`

### Install

```bash
npm run setup
```

### Start Backend + Frontend

```bash
npm run dev
```

若你想分開啟動：

```bash
npm run dev:api
npm run dev:web
```

### Run Checks

```bash
npm run check
```

### Run Smoke E2E

```bash
npm run e2e
```

本教學只要求一條 happy-path smoke E2E：

```text
create book -> add copy -> checkout -> return
```

## Demo 1: Agent-by-Agent

這種方式適合想理解每個角色如何交接 artifact 的讀者。

### Step 1. SA

你可以這樣下指令：

```text
請 sa 分析 SCN-LIB-001 情境，產出 REQ-LIB-001，並標註來源為 SCN-LIB-001
```

這一步通常會讀：

- `docs/scenarios/SCN-LIB-001.md`
- `docs/figma/library-mini-admin-console/`

這一步通常會產出：

- `docs/requirements/REQ-LIB-001.md`

下一個角色能接手的原因：

- Archi 可以根據已凍結的需求與 acceptance criteria 做 MVP 架構取捨

### Step 2. Archi

```text
請 archi 依 REQ-LIB-001 設計 MVP 架構，說明 NFR 取捨與 SD handoff 邊界
```

主要輸入：

- `docs/requirements/REQ-LIB-001.md`

主要輸出：

- `docs/architecture/ARCH-LIB-001.md`

下一個角色能接手的原因：

- SD 已經有足夠的系統邊界、部署假設與非功能要求可以落地成實作設計

### Step 3. SD

```text
請 sd 參考 REQ-LIB-001 與 ARCH-LIB-001 產出 implementation-ready 設計文件
```

主要輸入：

- `docs/requirements/REQ-LIB-001.md`
- `docs/architecture/ARCH-LIB-001.md`

主要輸出：

- `docs/openapi.yaml`
- `docs/schema/*.md`
- `docs/api/*.md`

下一個角色能接手的原因：

- PG 可以據此凍結 contract、拆分任務，並定義 FE/BE 的平行實作邊界

### Step 4. PG

```text
請 pg 根據 REQ、ARCH 與 SD artifacts 建立交付計畫，拆出 FE/BE 平行實作任務與驗收門檻
```

主要輸出：

- `docs/tasks/TASK-LIB-001_mvp-delivery.md`

下一個角色能接手的原因：

- FE / BE 已有穩定的 API contract、共享術語與清楚的 task boundary，可以平行實作

### Step 5. FE / BE 實作

在這個 repo 的 workflow 裡，`S5A FE` 與 `S5B BE` 是 PG 協調下的平行實作階段，不應被理解成「PG 一個人做完整個實作」。

你可以這樣要求 PG 推進：

```text
請 pg 依 TASK-LIB-001_mvp-delivery 的 gate 與切分推進 FE / BE 實作，完成後更新 workflow state
```

預期輸出：

- `apps/api/**`
- `apps/web/**`
- `docs/tasks/TASK-LIB-001_mvp-delivery-summary.md`

### Step 6. QA

```text
請 qa 驗證 SCN-LIB-001 是否符合 REQ-LIB-001，包含 smoke E2E 與必要 NFR 檢查
```

執行前先確保服務已啟動：

```bash
npm run dev
```

主要輸出：

- `apps/web/library-mini-admin-web/e2e/*.spec.ts`
- `docs/qa-report.md`

若 QA 回報缺口，應回到對應角色修正，而不是無限重跑。

## Demo 2: Orchestrator

這種方式適合想看 orchestrator 如何依 workflow 自動接續的讀者。

你可以直接對 orchestrator 下高層指令：

```text
請 orchestrator 接手 SCN-LIB-001，從目前 workflow state 繼續推進，直到可交付 QA
```

或：

```text
請開始進行 SCN-LIB-001 的規劃與實作，依 workflow 自動判斷下一步並持續推進
```

orchestrator 的工作方式應該是：

1. 先找對應 workflow state
2. 判斷 current stage 與 next role
3. 檢查上游 artifact 是否足夠
4. 若足夠則推進下一站
5. 若資訊不足則停下來要求補充
6. QA 完成後更新 workflow 為 done

這裡要刻意區分一件事：

- orchestrator 不是萬能 agent
- orchestrator 是 workflow coordinator

真正讓它能跨階段接續的，不是它「記性很好」，而是它會回到 [WF-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/workflows/WF-LIB-001.md) 讀 source of truth。

## Demo 3: Resume / Handoff

這個 demo 最能凸顯 workflow state 的價值。

假設某次 session 中斷，或你想換另一個 agent / 另一位工程師接手，不需要貼上一長串聊天歷史。你只需要要求 orchestrator 讀 workflow state 並回報現況。

例如：

```text
請 orchestrator 根據 WF-LIB-001 回報目前進度、下一步，以及這次接手前應先讀哪些文件
```

或：

```text
請 orchestrator 檢查 WF-LIB-001 現在停在哪個 stage，列出 blockers、open questions 與建議下一角色
```

理想行為是：

- orchestrator 先讀 `README.md`
- 再讀 `AGENTS.md`
- 再讀 `docs/workflows/WF-LIB-001.md`
- 再定位應先讀哪些 artifact
- 最後回報 next action

這就是 artifact-driven workflow 的核心價值：接手靠文件狀態，不靠聊天上下文。

## Project Layout

```text
/
├─ README.md
├─ AGENTS.md
├─ docs/
│  ├─ scenarios/
│  ├─ requirements/
│  ├─ architecture/
│  ├─ api/
│  ├─ schema/
│  ├─ tasks/
│  ├─ workflows/
│  ├─ openapi.yaml
│  ├─ qa-report.md
│  └─ figma/
├─ .codex/
│  ├─ config.toml
│  ├─ agents/
│  └─ skills/
├─ scripts/
└─ apps/
   ├─ api/
   └─ web/
```

如果你是第一次看這個 repo，最值得先讀的不是全部目錄，而是這幾個 artifact：

1. [AGENTS.md](/Users/cfh00902455/Projects/mini-library-console/AGENTS.md)
2. [docs/scenarios/SCN-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/scenarios/SCN-LIB-001.md)
3. [docs/workflows/WF-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/workflows/WF-LIB-001.md)

## Commands Reference

```bash
npm run setup      # install frontend dependencies
npm run dev        # start backend + frontend
npm run dev:api    # start Spring Boot API
npm run dev:web    # start Vite web app
npm run check      # backend tests + frontend lint/type-check
npm run check:api  # backend tests
npm run check:web  # frontend lint + type-check
npm run e2e        # Playwright smoke E2E
```

## Scenario 與交付物

這個 repo 的教學範例以 `SCN-LIB-001` 為主，對應的主要交付物如下：

- Scenario: [SCN-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/scenarios/SCN-LIB-001.md)
- Workflow state: [WF-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/workflows/WF-LIB-001.md)
- Requirements: [REQ-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/requirements/REQ-LIB-001.md)
- Architecture: [ARCH-LIB-001.md](/Users/cfh00902455/Projects/mini-library-console/docs/architecture/ARCH-LIB-001.md)
- OpenAPI contract: [openapi.yaml](/Users/cfh00902455/Projects/mini-library-console/docs/openapi.yaml)
- PG task plan: [TASK-LIB-001_mvp-delivery.md](/Users/cfh00902455/Projects/mini-library-console/docs/tasks/TASK-LIB-001_mvp-delivery.md)
- PG summary: [TASK-LIB-001_mvp-delivery-summary.md](/Users/cfh00902455/Projects/mini-library-console/docs/tasks/TASK-LIB-001_mvp-delivery-summary.md)
- QA report: [qa-report.md](/Users/cfh00902455/Projects/mini-library-console/docs/qa-report.md)

## Project Notes

### Tutorial Scope

目前 tutorial scope 以最小可交付為主，包含：

- 新增書目
- 增加 copies
- 借書
- 還書
- 查詢書籍狀態

刻意不納入：

- 登入 / 權限
- 預約
- 罰款
- 通知
- 搜尋

### Shell Environment Recommendation

若你在 macOS 使用 `zsh`，建議把 Node.js 初始化放進 `~/.zshrc`，並讓 `~/.zprofile` 載入 `~/.zshrc`，以避免不同 shell 模式下找不到 `node` 或 `npm`。

`~/.zshrc`

```bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && . "$NVM_DIR/bash_completion"
nvm use --silent
```

`~/.zprofile`

```bash
[ -f "$HOME/.zshrc" ] && source "$HOME/.zshrc"
```

本 repo 也提供 `.nvmrc`，進入專案後可先執行：

```bash
nvm use
node -v
npm -v
```

### Maven Cache Recommendation

本 repo 的 `scripts/run-api-command.js` 已預設將 Maven local repo 指到專案內的 `.m2-local`，因此建議優先使用：

```bash
npm run dev:api
npm run check:api
```

而不是直接手打未帶 repo-local Maven cache 設定的 `./mvnw ...`。

### UI Test Locator Rule

所有 UI 變更都應保留 `data-testid` 的穩定性。Playwright E2E 也應優先使用 `getByTestId()`，這是 repo 既有規範的一部分。
