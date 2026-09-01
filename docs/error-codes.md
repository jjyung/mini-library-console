# Library Mini Admin 全域業務錯誤碼

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-09-01 | 依 REQ-LIB-001 建立全域 business code、錯誤 envelope 與 API 對照。 |

## 2. Global Rules

- 所有 API 的成功與錯誤 response body 都必須帶有 `code`。
- HTTP status 表達 transport 結果，不能取代 business code。
- 所有錯誤 response 都必須帶有 `code`、繁體中文 `message` 與 `traceId`；欄位錯誤才附 `details`。
- `traceId` 必須能串聯 API log、資料存取紀錄與前端錯誤回報；不可將敏感資料放入 `message`、`details` 或 log。
- API-specific 的觸發條件、HTTP status 與 retryability 以各 `docs/api/*.md` 為準，本文件是 code 語意唯一來源。
- 本次 MVP 無第三方依賴，因此各 API 不應回傳 `C0000`；若日後新增依賴，須同步更新 OpenAPI、API flow 與監控規則。

## 3. Error Code Registry

| Business Code | Type | Definition | Default HTTP Status | Retryable |
| --- | --- | --- | --- | --- |
| `00000` | Success | 請求成功完成。 | `200 / 201` | 依 API idempotency 定義 |
| `A0000` | Client Error | 通用輸入、資源或業務條件不符合。 | `400 / 404 / 409` | 否 |
| `A0001` | Client Error | 必填欄位缺漏、格式錯誤或數量小於 1。 | `400` | 否 |
| `A0002` | Client Error | ISBN 找不到對應書籍。 | `404` | 否 |
| `A0003` | Client Error | ISBN 已存在，不能重複建立。 | `409` | 否 |
| `A0004` | Client Error | 書籍已無可借副本。 | `409` | 否 |
| `A0005` | Client Error | 書籍未上架，不能借閱。 | `409` | 否 |
| `A0006` | Client Error | 書籍沒有借閱中的副本可歸還。 | `404` | 否 |
| `B0000` | System Error | API、資料庫或內部系統發生非預期錯誤。 | `500` | 通常否；依操作安全性判斷 |
| `B0001` | System Error | 館藏狀態更新失敗或資料一致性檢查失敗。 | `500` | 僅在確認未提交時可重試 |
| `C0000` | Third-party Error | 第三方服務失敗、逾時或回傳不可接受結果。MVP 不使用。 | `502 / 503` | 依第三方 retry policy |

## 4. Shared Error Response Contract

錯誤 response 必須與 `docs/openapi.yaml` 的 `ErrorResponseDTO` 一致：

```json
{
  "code": "A0002",
  "message": "找不到該 ISBN 的書籍",
  "traceId": "01J...",
  "details": []
}
```

`details` 為可選陣列，每筆包含 `field` 與 `reason`。不得用 HTTP status 或 message 取代 machine-readable `code`。

## 5. API Reference

| API ID | API-specific codes |
| --- | --- |
| `library-books-001` | `00000`, `A0001`, `A0003`, `B0000`, `B0001` |
| `library-books-002` | `00000`, `B0000`, `B0001` |
| `library-loans-001` | `00000`, `A0001`, `A0002`, `A0004`, `A0005`, `B0000`, `B0001` |
| `library-loans-002` | `00000`, `A0001`, `A0002`, `A0006`, `B0000`, `B0001` |
