# Global Error Code Definition

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | SD | 2026-09-04 | 建立 Library Mini Admin 測試情境的全域業務錯誤碼與回應規則。 |

## 2. Global Rules

1. 所有 API 都必須在回應 body 提供業務 `code`；HTTP status 只表達傳輸層結果，不取代業務錯誤碼。
2. 成功回應使用 `00000`；客戶端輸入、業務狀態或唯一性條件不符合使用 `A0000`。
3. 系統內部例外、資料庫不可用或交易失敗使用 `B0000`；本 MVP 沒有第三方整合，沒有實際使用 `C0000` 的 API。
4. 每次回應都應帶有 `traceId`，供測試環境的 request、service 與 database log 關聯。
5. 錯誤訊息不得依賴登入或個人資料；本情境為 test-only、免登入，預設使用 Admin 操作上下文。

## 3. Error Code Registry

| Code | Type | Meaning | HTTP status guidance | Handling |
| --- | --- | --- | --- | --- |
| 00000 | Success | 請求成功完成 | 200、201 或符合 API 定義的 2xx | Client consumes `data` and treats the operation as complete. |
| A0000 | Client Error | 請求格式、欄位、唯一性或業務前置條件不符合 | 400 | Client shows a correctable business message and does not retry automatically. |
| B0000 | System Error | 服務內部、資料庫或不可預期例外 | 500 | Client shows a generic failure message; retry only when the flow explicitly permits it. |
| C0000 | Third-party Error | 第三方服務回應失敗或不可用 | 502 或依整合契約定義 | Client shows an integration failure; this code is reserved and not used by current APIs. |

## 4. Shared Error Response Contract

所有失敗 API 使用 `ErrorResponseDTO`，定義於 `docs/openapi.yaml`。最低欄位如下：

| Field | Required | Description |
| --- | --- | --- |
| `code` | Yes | `A0000`、`B0000` 或 `C0000`。 |
| `message` | Yes | 可供前端顯示或測試斷言的業務訊息。 |
| `traceId` | Yes | 本次請求的關聯識別值。 |
| `details` | No | 可選的欄位或條件錯誤清單。 |

HTTP status 與業務 `code` 必須同時存在。例如輸入錯誤回傳 HTTP 400 且 body code 為 `A0000`；資料庫故障回傳 HTTP 500 且 body code 為 `B0000`。不得只回傳 HTTP status 或把 HTTP status 填入 `code`。

## 5. API Reference Rules

- 每一個 API 的成功 DTO 必須以 `00000` 作為 `code` enum，並符合 Request/Response 命名規則。
- 每一個 API flow 必須引用本文件 `docs/error-codes.md`，並描述成功與適用的錯誤碼對照。
- API-specific mapping 以實際可發生的錯誤為主；目前 Library Mini Admin 沒有外部服務，因此各 API 不建立虛假的 `C0000` mapping。
- `traceId` 應由 API 邊界建立或沿用既有 request context，並在錯誤 log 中保留。
