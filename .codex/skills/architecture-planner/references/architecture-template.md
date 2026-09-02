# ARCH-<DOMAIN>-<SEQ> 系統架構（Archi）

## 1. 文件目的與範圍

- 需求來源：
- 場景 / workflow：
- 架構目標：
- 文件範圍：系統層
- 不含範圍：API / DB / transaction / DTO 等 SD 細節
- 文件狀態：Draft / Ready for SD / Blocked

### 1.1 標準環境 profile 對應表

環境名稱是 label，profile 才是用途。只選擇需求需要的 profile；未採用的
profile 填 `N/A`，不可因模板存在就建立環境。

| Profile | 主要目的 | 典型資料 | HA / 可用性 | 依賴模式 | 觀測與發布閘門 | 生命週期 |
| --- | --- | --- | --- | --- | --- | --- |
| `LOCAL` / `DEV` | 個人開發與快速回饋 | synthetic / local seed | 預設無 HA | local / stub / mock | developer log、automated check | 長期或 branch-based |
| `POC` | 技術可行性與學習 | synthetic / 嚴格遮罩 | 最小拓撲、成本優先 | mock / stub / sandbox / 選定 real | 決策證據、targeted telemetry | 臨時 |
| `TEST` / `QA` | 功能、回歸與品質測試 | generated / synthetic / 遮罩 | 通常單一執行個體 | mock / sandbox | test report、quality gate | 可選、長期或 ephemeral |
| `SIT` | 系統與外部整合驗證 | controlled / synthetic / 遮罩代表資料 | 代表性拓撲；除非測 HA 否則不強制 | sandbox / controlled endpoint | centralized telemetry、integration gate | 通常長期 |
| `UAT` | 業務驗收 release candidate | 遮罩代表資料 | 依驗收需要接近 PROD | business-approved sandbox / pre-prod | acceptance evidence、sign-off | 可選、臨時或長期 |
| `PROD` | 正式業務運作 | 受正式政策管理的 production data | agreed SLO、HA、RTO/RPO、backup、DR | production dependencies | full telemetry、alert、on-call、change/recovery gate | 長期 |

`TEST` / `QA` 必須補充實際測試層級與 owner；它不是 `SIT` 的同義詞。
`POC` 通常是臨時階段，不可直接取代 `SIT` 或 `UAT` 的驗收證據。

### 1.2 本專案實際環境與需求確認

| 實際環境名稱 | 對應 profile | 是否納入 | 目的與成功條件 | 使用者確認狀態 |
| --- | --- | --- | --- | --- |
| | LOCAL / DEV / POC / TEST / SIT / UAT / PROD | | | Confirmed / Assumed / TBD |

### 1.3 假設、問題與追蹤

| ID | 假設 / 待確認問題 | 為何影響架構 | 狀態 | 負責人 / 驗證點 |
| --- | --- | --- | --- | --- |
| A-001 | | | Confirmed / Assumed / TBD | |

## 2. 技術選型與版本治理

| 領域 | 選定技術／版本 | 版本固定與變更規則 | 選擇理由 | 替代方案與取捨 |
| --- | --- | --- | --- | --- |
| Backend runtime/framework | Java / Spring Boot | Pin runtime/framework versions in the build configuration |  |  |
| Build/tooling | Maven Wrapper + `pom.xml` | Build plugins and versions are reviewable and pinned |  |  |
| API contract generation | OpenAPI Generator Maven plugin | Plugin version is pinned in `pom.xml`; generated code is committed and never manually edited |  |  |
| Persistence |  |  |  |  |
| Database migration | Liquibase formatted SQL | Changesets are immutable; SQL only; Liquibase XML is prohibited |  |  |
| Messaging | N/A or  |  |  |  |

## 3. 應用架構選型與依賴方向

- 選定模式：三層式 `controller -> service -> dao`／Clean Architecture／Hexagonal Architecture
- 複雜度訊號：
- 主要邊界與依賴方向：
- 若有 MQ：inbound adapter、inbound port、application/domain core、outbound port、outbound adapter：
- 交易、delivery、retry、dead-letter、ordering、idempotency 與 consistency 考量：
- 選擇理由：
- 未採用替代方案與原因：

```mermaid
flowchart LR
    inbound[Inbound adapters]
    portsIn[Inbound ports]
    core[Application / Domain core]
    portsOut[Outbound ports]
    outbound[Outbound adapters]
    inbound --> portsIn --> core --> portsOut --> outbound
```

## 4. 架構決策與取捨

### 4.1 關鍵決策

| ID | 決策 | 選項與取捨 | 需求依據 | 影響 / 後續觸發條件 |
| --- | --- | --- | --- | --- |
| ADR-001 | | | | |

### 4.2 主要取捨

- 成本：
- 複雜度：
- 可用性 / 可靠性：
- 安全性 / 維運性：

## 5. 系統脈絡（C4-L1）

### 5.1 描述

- 使用者與角色：
- 外部系統：
- 信任邊界：
- 重要資料 / 控制流：

### 5.2 Mermaid

```mermaid
flowchart LR
```

## 6. 容器視圖（C4-L2）

### 6.1 描述

| 容器 | 職責 | 所有者 | 邊界 / 主要依賴 |
| --- | --- | --- | --- |
|  |  |  |  |

### 6.2 Mermaid

```mermaid
flowchart LR
```

## 7. 部署拓撲與本地運行模型

### 5.1 系統部署拓撲

- 網路區域與 ingress / egress：
- 失效域與擴展邊界：
- 託管服務與責任分界：
- 外部依賴失效時的隔離 / 降級：

### 5.2 各環境部署矩陣

欄位必須依 `1.2 本專案實際環境與需求確認` 的實際環境填寫；不要在此
重複列出未採用的 profile。標準 profile 的定義唯一來源是 `1.1`。本表只
記錄部署差異；安全、資料、恢復、logging、tracing、monitoring 與
alerting 的詳細決策唯一記錄在 6.2～6.7。

| 面向 | `<ENV-1>` (`<PROFILE>`) | `<ENV-2>` (`<PROFILE>`) |
| --- | --- | --- |
| 網路區域 / ingress / egress |  |  |
| 拓撲 / 執行個體 / HA |  |  |
| 外部依賴（real / sandbox / stub / mock） |  |  |
| scaling / failure domain / maintenance |  |  |
| deploy / promotion / rollback |  |  |
| 成本護欄 |  |  |

### 5.3 本地運行模型

- 啟動方式與必要依賴：
- 本地替身 / 測試資料政策：
- 與各環境的差異及不可模擬項：

## 8. NFR 與架構控制面

本章是安全與維運控制面的唯一內容來源。若控制面在不同環境有差異，
請在本章記錄實際環境名稱或 profile；`5.2` 只引用本章，不重複填寫。

### 6.1 NFR 對應與驗證

本表只作為索引；詳細系統層對策唯一寫在 6.2～6.7 對應章節，避免同一
項目填寫兩次。

| NFR / 需求 ID | 目標或約束摘要 | 詳細控制面章節 | 驗證證據 / owner | 狀態 |
| --- | --- | --- | --- | --- |
| | | 6.2～6.7 | | Pass / Fail / TBD |

### 6.2 存取控制與安全

- 人員 / service identity 與 authentication：
- authorization、RBAC / ABAC、least privilege：
- tenant / environment / network isolation：
- secrets、key ownership、rotation：
- privileged / admin / break-glass access：
- access review、revocation、deny-by-default：

### 6.3 可追蹤性與 auditability

- correlation / request ID 與 distributed trace ID 傳遞：
- 必須 audit 的安全與業務動作：
- audit event 最低內容（actor、target、action、time、outcome、reason）：
- 儲存、不可竄改性、存取權限、保留與隱私：
- 如何回連需求、部署版本與 incident：

### 6.4 Logging

- structured / centralized logging：
- log 類別、severity、timestamp、correlation 欄位：
- PII / secret redaction、sampling、retention：
- log pipeline 失效時的行為：
- operational log 與 audit record 的責任區分：

### 6.5 Availability、HA、resilience 與 recovery

- availability target / SLA / SLO：
- failure domain、replica、health/readiness、failover：
- timeout、retry / backoff、rate limit、graceful degradation：
- dependency isolation：
- backup / restore、RTO、RPO、DR：
- maintenance 與 recovery drill 頻率：
- lower-tier 的刻意降級與 PROD gate：

### 6.6 效能、存取效率與 cache

- latency / throughput / concurrency 目標：
- 預期瓶頸與量測方式：
- cache 是否必要及適用資料：
- scope、TTL / freshness、invalidation / versioning：
- consistency、stampede / poisoning、sensitive-data policy：
- capacity / eviction、cache outage fallback：
- hit / miss、staleness、eviction metrics：

### 6.7 Monitoring、dashboard 與 alerting

- metrics、logs、traces、audit、synthetic / dependency checks：
- traffic、latency、errors、saturation、availability / SLO：
- dependency、queue / storage、cache、security、business metrics：
- dashboard audience 與 ownership：
- alert condition、severity、routing、on-call、escalation：
- deduplication / noise control、runbook、response expectation：

## 9. 成本與複雜度控制

- 各環境 profile 的成本護欄與代表性取捨：
- PROD 成本驅動因素：
- 為何不採用更複雜方案：
- 擴容 / 升級觸發條件：

## 10. 風險、非目標與開放決策

| ID | 風險 / 非目標 / 未決策 | 影響 | 緩解或需要的決策 | Owner / deadline |
| --- | --- | --- | --- | --- |
| R-001 | | | | |

## 11. 移交 SD 項目（Archi 不定稿）

1. 將 authentication / authorization 邊界落成 API 與服務間契約。
2. 將 correlation / trace / audit / log 最低欄位與 redaction 規則落成設計。
3. 將 NFR、cache、timeout、retry、degradation 與 recovery 約束落成可測試規格。
4. 將部署、secrets、backup、telemetry 與 alert 的責任分界落成環境設定與流程。
5. 補齊本文件列出的未決 API、schema、資料保留與實作細節。

## 12. 擴展觸發條件（後續）

- 流量 / latency / storage / cache 指標達到：
- 可用性、RTO / RPO 或 failure drill 不再滿足：
- 權限、audit、合規或資料區域要求改變：
- 外部依賴 SLA / 成本 / 風險改變：

## 13. 完成檢查與證據

| 檢查項目 | 結果 | 證據 / 備註 |
| --- | --- | --- |
| 需求與使用者確認可追蹤 | Pass / Fail / TBD | |
| 實際環境到 profile 的對應與部署矩陣完整 | Pass / Fail / TBD | |
| C4-L1 / C4-L2 / deployment topology | Pass / Fail / TBD | |
| 存取控制與 secrets | Pass / Fail / TBD | |
| correlation / trace / auditability | Pass / Fail / TBD | |
| logging | Pass / Fail / TBD | |
| HA / resilience / RTO / RPO / DR | Pass / Fail / TBD | |
| 效能與 cache 或 no-cache rationale | Pass / Fail / TBD | |
| monitoring / dashboard / alerting | Pass / Fail / TBD | |
| SD handoff 與 Archi 邊界 | Pass / Fail / TBD | |
