---
name: architecture-planner
description: Plan and document system-level architecture from requirement documents for MVP delivery. Use when user asks Archi to produce architecture outputs (e.g., ARCH-*.md) with C4 context, deployment topology, NFR strategy, cost/complexity tradeoffs, and explicit SD handoff boundaries.
---

# Architecture Planner

Produce an architecture document at system level only.

## Workflow

1. Read inputs.
- Read `docs/requirements/*.md`.
- Read current architecture constraints from `.codex/agents/archi.toml`.
- If scenario context is needed, read `docs/scenarios/*.md`.

2. Define output scope.
- Target file: `docs/architecture/ARCH-<DOMAIN>-<SEQ>.md`.
- Keep focus on system-level decisions: context, containers, deployment, NFR strategy, cost/risk.

3. Write architecture document with fixed sections.
- 文件目的與範圍
- 架構決策與取捨
- 系統脈絡（C4-L1）
- 容器視圖（C4-L2）
- 部署拓撲與本地運行模型
- NFR 對應（系統層）
- 成本與複雜度控制
- 風險與非目標
- 移交 SD 項目（Archi 不定稿）
- 擴展觸發條件（後續）

4. Enforce boundaries.
- Do not output API contract details.
- Do not output DB schema/DDL/index definitions.
- Do not output transaction step-by-step implementation.
- Do not output error-code field mapping or DTO details.
- If such details are requested, mention them in `移交 SD 項目` only.

5. Validate before finish.
- Ensure C4-L1 and C4-L2 are both present.
- Ensure at least one Mermaid diagram exists.
- Ensure explicit SD handoff list exists.
- Ensure document states local-run assumptions and external dependency policy.

## Output Contract

When complete, report:
- Output file path.
- Scope statement (system-level only).
- Handoff items for SD.

## Reference

Use [references/architecture-template.md](references/architecture-template.md) as the base skeleton.
