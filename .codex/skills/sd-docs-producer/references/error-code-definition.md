# Global Error Code Definition

> 將此檔案作為 `docs/error-codes.md` 的建立範本。此文件是所有 API 共用的
> business error code 唯一來源；個別 API flow doc 只需要引用此文件，並補充
> 該 API 實際會發生的 trigger 與 mapping。

## 1. 修改紀錄

每次修改都要記錄修改人、日期，以及修改原因與內容。

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-08-31 | 建立 global business error code definition template。 |
| [version] | [name or role] | [YYYY-MM-DD] | [填寫修改原因與具體變更內容] |

## 2. Global Rules

- 所有 API 的成功與錯誤 response 都必須在 response body 帶有 `code`。
- HTTP status 用來表達 transport 結果，不能取代 business code。
- 所有錯誤 response 必須帶有 `code`、中文 `message` 與 `traceId`；需要欄位
  細節時才附加 `details`。
- API-specific 的錯誤情境、HTTP status 與 retryability 必須在 API flow doc
  中明確 mapping，但不得改寫本文件的 global code 語意。
- 新增或修改 global code 前，必須同步檢查 `docs/openapi.yaml`、API flow
  docs、frontend error mapping 與 monitoring/alert 規則。

## 3. Error Code Registry

| Business Code | Type | Definition | Default HTTP Status | Retryable |
| --- | --- | --- | --- | --- |
| `00000` | Success | 請求成功完成。 | `[200 / 201 / 202]` | 依 API idempotency 定義 |
| `A0000` | Client Error | 請求格式、權限、資源狀態、資源不存在、唯一性限制或其他 client/business 條件不符合。OpenAPI constraint 的具體規則由 `docs/openapi.yaml` 定義。 | `[400 / 401 / 403 / 404 / 409]` | 否 |
| `B0000` | System Error | API、database 或內部系統發生非預期錯誤。 | `500` | 通常否；依 API operation 的安全性與 retry policy 判斷 |
| `C0000` | Third-party Error | API 依賴的第三方服務失敗、逾時或回傳無法接受的結果。沒有第三方依賴的 API 不得使用此代碼。 | `[502 / 503]` | 依第三方 retry policy 判斷 |

## 4. Shared Error Response Contract

錯誤 response 的 schema 必須與 `docs/openapi.yaml` 的
`ErrorResponseDTO` 保持一致。API 不得只依賴 HTTP status 判斷錯誤。

```json
{
  "code": "A0000",
  "message": "[中文錯誤訊息]",
  "traceId": "[trace-id]",
  "details": [
    {
      "field": "[fieldName]",
      "reason": "[中文欄位錯誤原因]"
    }
  ]
}
```

## 5. API Reference Rules

- 每個 API flow doc 的錯誤代碼章節都要連結 `docs/error-codes.md`。
- API flow doc 只補充該 API 的 `Trigger`、`HTTP Status`、`Response Behavior`
  與 `Retryable`，不複製本文件的完整 code definition。
- `message` 可依 API 情境提供中文內容，但不得讓 message 取代 machine-readable
  的 `code`。
- `traceId` 必須能串聯 API log、database operation log 與第三方呼叫紀錄。
- 不可將 password、token、完整 email 或其他敏感資料放入 message、details
  或 log。
