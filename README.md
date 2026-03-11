# Library Mini Admin Console (Tutorial)

最小教學專案：**圖書館借書 / 還書**管理台（Vue + Spring Boot），用來示範 **Codex multi-agent** 分工協作與可重現的工程流程。

## 目標

- **SA**：可選用 Figma MCP 取得 UI 設計上下文，產出需求文件（FR/NFR/AC）
- **Archi**：依 NFR 控制複雜度與成本，提出最小可行架構
- **SD**：設計資料模型、API、domain rules
- **PG**：後端採 **TDD**（unit + integration）實作核心借/還書規則
- **QA**：只做 **1 條 Playwright smoke E2E**（happy path），避免教學超時  
  - UI 元件統一使用 `data-testid`，E2E 只用 `getByTestId()` 定位（更穩）  
  - 有時間再示範 Playwright MCP 協助生成/維護測試

> 本 repo 使用 `AGENTS.md` 作為 agent 的專案操作契約；Codex 會在開始工作前讀取它。  
> Skills 放在 `.codex/skills/`（Codex 會掃描這個位置）。  
> 以上行為與路徑依官方文件。 :contentReference[oaicite:0]{index=0}

---

## Scope（刻意保持最小）

### In scope

- 新增書目
- 增加 copies
- 借書（checkout）
- 還書（return）
- 查詢書籍狀態（total / available / checkedOut）

### Out of scope

- 預約、罰款、登入/權限、通知、搜尋

---

## Repo Structure

```text

/
├─ README.md                        # 專案說明與教學流程
├─ AGENTS.md                        # 專案工程規範（命名、OpenAPI、測試等）
├─ docs/                            # 文件交付根目錄
│  ├─ scenarios/                    # 情境文件（SCN-*）
│  ├─ requirements/                 # 需求文件（REQ-*）
│  ├─ architecture/                 # 架構文件（ARCH-*）
│  ├─ api/                          # SD 逐 API 設計文件
│  ├─ schema/                       # SD 資料模型/資料庫設計
│  ├─ tasks/                        # PG 任務規劃與交付總結
│  ├─ openapi.yaml                  # 系統 OpenAPI 契約
│  ├─ qa-report.md                  # QA 驗證報告
│  └─ figma/                        # Figma 匯出參考（UI 對齊依據，支援多需求分段）
├─ .codex/                          # Agent 與 Skill 設定
│  ├─ agents/                       # 各角色 agent prompt / 規範
│  └─ skills/                       # 可重用工作技能（SA/Archi/SD/PG/QA）
│     ├─ scenario-requirements-writer/
│     ├─ architecture-planner/
│     ├─ sd-docs-producer/
│     ├─ pg-task-orchestrator/
│     └─ qa-e2e-verifier/
├─ scripts/                         # 根目錄腳本（啟動、檢查、包裝命令）
└─ apps/                            # 可執行應用程式
   ├─ api/                          # Spring Boot 後端
   └─ web/                          # Vue + Vite 前端（含 Playwright）
```

---

## Prerequisites

- Node.js 20+
- Java 21+
- (optional) pnpm（沒有也可用 npm）

> Windows 使用者不需要 WSL：直接使用 `npm run ...` 即可

### Shell Environment Recommendation（macOS zsh）

若你在 macOS 使用 `zsh`，建議把 Node.js 初始化放進 `~/.zshrc`，並讓 `~/.zprofile` 載入 `~/.zshrc`。

原因：

- 有些 terminal / agent / sandbox 會用 `login shell`
- 有些只會讀 `~/.zprofile`，不一定會自動讀 `~/.zshrc`
- 若 `node` / `npm` 只在 `~/.zshrc` 內設定，實際執行 `npm run check`、`npm run dev`、`npm run e2e` 時可能會出現 `command not found: node` 或 `command not found: npm`

建議設定如下：

`~/.zshrc`

```bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
[ -s "$NVM_DIR/bash_completion" ] && . "$NVM_DIR/bash_completion"

# 建議使用 repo 對應版本
nvm use --silent
```

`~/.zprofile`

```bash
# 讓 login shell 也載入 Node / npm 設定
[ -f "$HOME/.zshrc" ] && source "$HOME/.zshrc"
```

本 repo 也提供 [.nvmrc](./.nvmrc)，所以進入專案後可先執行：

```bash
nvm use
node -v
npm -v
```

如果 `node -v` 或 `npm -v` 失敗，先修正 shell 設定，再執行後續流程，否則 `setup`、`check`、`dev`、`e2e` 都可能中斷。

### Maven Cache Recommendation

若你在受限制環境、sandbox 或 CI 內執行 Maven，建議避免把快取寫到 `~/.m2/repository`，因為可能會遇到權限問題。

本 repo 的 [scripts/run-api-command.js](./scripts/run-api-command.js) 已預設將 Maven local repo 指到專案內的 `.m2-local`，因此建議優先使用：

```bash
npm run dev:api
npm run check:api
```

而不是直接手打未帶 `-Dmaven.repo.local=...` 的 `./mvnw ...`。

---

## Quick Start（建議兩個 Terminal）

在專案根目錄先安裝前端依賴（首次）：

```bash
npm run setup
```

### Terminal 1：Start Backend

```bash
npm run dev:api
```

### Terminal 2：Start Frontend

```bash
npm run dev:web
```

### 或單指令同時啟動前後端

```bash
npm run dev
```

---

## Check（unit + integration + lint）

```bash
npm run check
```

---

## E2E（Smoke Only）

本教學只要求 **1 條 happy-path smoke E2E**：

> create book → add copy → checkout(ok) → return(ok)

### E2E 前置檢查（必做）

在執行 E2E 前，先確認前後端服務都已啟動：

```bash
# 啟動服務（擇一）
npm run dev
# 或分開啟動
npm run dev:api
npm run dev:web
```

```bash
# 確認服務埠
lsof -iTCP:8080 -sTCP:LISTEN -n -P
lsof -iTCP:5173 -sTCP:LISTEN -n -P
```

若任一服務未啟動，先修復服務啟動問題，再執行 E2E。

```bash
npm run e2e
```

### UI Test Rule（很重要）

- 所有可互動元件都必須有 `data-testid`
- E2E 一律用 `page.getByTestId()` 定位（避免 CSS 結構改動造成 flaky）

Playwright 官方文件也建議以 test id 作為最 resilient 的定位方式。

---

## Docs（交付物）

- `docs/scenarios/`：情境文件（`SCN-*`）
- `docs/requirements/`：需求分析文件（`REQ-*`，含 FR/NFR/AC 與 traceability）
- `docs/architecture/`：架構文件（`ARCH-*`）
- `docs/openapi.yaml`：整體 API 契約
- `docs/api/`：逐 API 設計文件
- `docs/schema/`：資料模型與資料庫設計文件
- `docs/tasks/`：任務規劃與交付總結文件
- `docs/qa-report.md`：QA 驗證報告

---

## SCN-LIB-001 教學流程（Orchestrator）

如果目前 session 預設角色是 orchestrator，你可以直接提問要求開始進行 `SCN-LIB-001` 的規劃與實作，讓它依 workflow 狀態自動判斷下一步並持續推進。

可直接這樣問：

```text
請開始進行 SCN-LIB-001 的規劃與實作，依 workflow 自動判斷下一步並持續推進
```

```text
請 orchestrator 告訴我 SCN-LIB-001 現在進度、下一步與需要先讀哪些文件
```

```text
請 orchestrator 接手 SCN-LIB-001，從需求、架構、設計、任務規劃到實作與 QA 依序推進
```

---

## SCN-LIB-001 教學流程（Agent-by-Agent）

以下是從情境到交付的建議順序，可直接照這個流程操作。

1. SA：從情境產出需求文件（REQ）
   - 你下指令：

    ```text
    請 sa 分析 SCN-LIB-001 情境，產出 REQ-LIB-001（來源情境需標註 SCN-LIB-001）
    ```

   - 主要輸入：
     - `docs/scenarios/SCN-LIB-001.md`
     - `docs/figma/library-mini-admin-console/`
   - 主要輸出：
     - `docs/requirements/REQ-LIB-001.md`

2. Archi：依 REQ 做系統層架構（MVP）
   - 你下指令：

   ```text
   請 archi 依 REQ-LIB-001 設計 MVP 架構：sqlite、本地可跑、不依賴外部服務、不含快取
   ```

   - 主要輸入：
     - `docs/requirements/REQ-LIB-001.md`
   - 主要輸出：
     - `docs/architecture/ARCH-LIB-001.md`

3. SD：依 REQ + ARCH 產出實作設計文件
   - 你下指令：

   ```text
   請 sd 參考 ARCH-LIB-001 與 REQ-LIB-001 進行系統設計
   ```

   - 主要輸入：
     - `docs/requirements/REQ-LIB-001.md`
     - `docs/architecture/ARCH-LIB-001.md`
   - 主要輸出：
     - `docs/openapi.yaml`
     - `docs/schema/*.md`
     - `docs/api/*.md`

4. PG：任務規劃與 BE/FE 分派
   - 你下指令：

   ```text
   請 pg 完成 API 任務規劃，並拆成 BE/FE 明確分派版
   ```

   - 主要輸出：
     - `docs/tasks/LIB-API-001_api-delivery-plan.md`

5. PG：實作交付（依任務計畫）
   - 你下指令：

   ```text
   請 pg 開始分派並實作，按照文件走
   ```

   - 主要輸出：
     - `apps/api/**`
     - `apps/web/**`
     - `docs/tasks/LIB-API-001_api-delivery-summary.md`

6. QA：驗證需求符合度（E2E + NFR）
   - 你下指令：

   ```text
   請 qa 驗證網頁功能是否完全符合 REQ-LIB-001
   ```

   - 執行前置（必做）：
     - 啟動服務：`npm run dev`（或分開 `npm run dev:api` + `npm run dev:web`）
     - 確認服務埠：`8080`、`5173`
   - 主要輸出：
     - `apps/web/library-mini-admin-web/e2e/*.spec.ts`
     - `docs/qa-report.md`

7. 回歸修正循環（PG/QA）
   - QA 若回報缺口，交給 PG 修正後再回 QA 重測，直到 `REQ-LIB-001` AC 與 NFR 通過。

---

## Workflow State Management

This project uses **artifact-driven workflow state** so that different agents or sessions can continue work safely without relying on chat history alone.

### Workflow IDs

Each scenario-driven development flow should have a workflow record:

- Scenario: `SCN-<DOMAIN>-<NNN>`
- Workflow: `WF-<DOMAIN>-<NNN>`

Example:

- `SCN-LIB-001`
- `WF-LIB-001`

### Workflow state file

Each workflow must maintain a state file under:

```text
docs/workflows/WF-<DOMAIN>-<NNN>.md
```
