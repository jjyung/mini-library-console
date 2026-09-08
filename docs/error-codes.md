# Global Error Code Definition

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-09-08 | 建立 SCN-LIB-001 TEST MVP 的全域 business error code、traceId 與 shared error response 規則。 |

## 2. Global Rules

- 所有 API 的成功與錯誤 response 都必須在 response body 帶有 `code`。
- HTTP status 用來表達 transport 結果，不能取代 business code。
- 所有錯誤 response 必須帶有 `code`、中文 `message` 與 `traceId`；需要欄位細節時才附加 `details`。
- API-specific 的錯誤情境、HTTP status 與 retryability 必須在 API flow doc 中明確 mapping，但不得改寫本文件的 global code 語意。
- TEST MVP 無 runtime third-party dependency；目前四個 API 不使用 `C0000`，但保留全域代碼供未來依賴導入。
- 新增或修改 global code 前，必須同步檢查 `docs/openapi.yaml`、API flow docs、frontend error mapping 與 monitoring／alert 規則。

## 3. Error Code Registry

| Business Code | Type | Definition | Default HTTP Status | Retryable |
| --- | --- | --- | --- | --- |
| `00000` | Success | 請求成功完成；成功 response 的 `code` 固定為此值。 | `200` 或 `201` | 依 API idempotency 定義 |
| `A0000` | Client Error | 請求格式、權限、資源狀態、資源不存在、唯一性限制或其他 client／business 條件不符合。具體 constraint 由 `docs/openapi.yaml` 定義。 | `400`、`404` 或 `409` | 否；修正輸入或重新取得資源後可重新操作 |
| `B0000` | System Error | API、database 或內部系統發生非預期錯誤。 | `500` | 通常否；需依 operation 是否已確認結果決定 |
| `C0000` | Third-party Error | API 依賴的第三方服務失敗、逾時或回傳無法接受的結果；沒有第三方依賴的 API 不得使用此代碼。 | `502` 或 `503` | 依第三方 retry policy 判斷 |

## 4. Shared Error Response Contract

錯誤 response 的 schema 與 `docs/openapi.yaml` 的 `ErrorResponseDTO` 保持一致。API 不得只依賴 HTTP status 判斷錯誤。

```json
{
  "code": "A0000",
  "message": "ISBN 已存在，無法重複新增",
  "traceId": "test-20260908-0001",
  "details": [
    {
      "field": "isbn",
      "reason": "此 ISBN 已存在"
    }
  ]
}
```

- `traceId` 由 API ingress 建立或沿用 `X-Correlation-Id` 的關聯值，並可串聯 API log、database operation log 與 CI evidence。
- 不可將 password、token、完整讀者個資或其他敏感資料放入 message、details 或 log。
- TEST 的 `message` 以可理解的繁體中文呈現；內部 exception、SQL 或 stack trace 不得直接回傳。

## 5. API Reference Rules

- 每個 API flow doc 的錯誤代碼章節都要連結 `docs/error-codes.md`。
- API flow doc 只補充該 API 的 `Trigger`、`HTTP Status`、`Response Behavior` 與 `Retryable`，不複製本文件的完整 code definition。
- `message` 可依 API 情境提供中文內容，但不得讓 message 取代 machine-readable 的 `code`。
- `traceId` 必須能串聯 API log、database operation log 與 CI／Playwright evidence。
