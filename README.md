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
> Skills 放在 `.agents/skills/`（Codex 會掃描這個位置）。  
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
├─ README.md
├─ AGENTS.md
├─ docs/
│  ├─ scenario.md
│  ├─ requirements.md
│  ├─ architecture.md
│  ├─ design.md
│  ├─ qa-report.md
│  └─ runbook.md
├─ .agents/
│  └─ skills/
│     ├─ sa-from-figma/
│     ├─ archi-cost-nfr/
│     ├─ sd-api-db-design/
│     ├─ pg-tdd-spring/
│     └─ qa-e2e-playwright/
├─ scripts/         # cross-platform wrappers (sh + cmd)
└─ apps/
├─ api/          # Spring Boot
└─ web/          # Vue (Vite) + Playwright
```

---

## Prerequisites

- Node.js 20+
- Java 21+
- (optional) pnpm（沒有也可用 npm）

> Windows 使用者不需要 WSL：直接使用 `npm run ...` 即可

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

```bash
npm run e2e
```

### UI Test Rule（很重要）

- 所有可互動元件都必須有 `data-testid`
- E2E 一律用 `page.getByTestId()` 定位（避免 CSS 結構改動造成 flaky）

Playwright 官方文件也建議以 test id 作為最 resilient 的定位方式。

---

## Docs（交付物）

- `docs/scenario.md`：情境
- `docs/requirements.md`：SA 需求（FR/NFR/AC/錯誤碼/UI→API mapping）
- `docs/architecture.md`：Archi 架構與成本控制
- `docs/design.md`：SD 設計（DB/API/domain rules）
- `docs/qa-report.md`：QA 測試結果（smoke + NFR 最小檢核）
- `docs/runbook.md`：運維/排錯（最小版）

---

## Optional: MCP 插點（有時間再示範）

- SA：可接 Figma MCP 讀設計上下文 → 更快產出 requirements
- QA：可接 Playwright MCP 協助走 UI、生成/修正 smoke spec

Codex 支援在 CLI/IDE 連 MCP servers。
