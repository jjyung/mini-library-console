# API Design - library-books-002

## 1. 基本資訊

- API ID：`library-books-002`
- API 名稱：List Books
- 方法與路徑：`GET /api/v1/books`
- operationId：`getApiV1Books`
- 對應需求：`FR-LIB-002`, `AC-LIB-001`

---

## 2. Request DTO

- Schema：`GetBooksRequestDTO`
- Query Parameters：
  - `status`（optional，`AVAILABLE` / `CHECKED_OUT`）
  - `titleKeyword`（optional）

---

## 3. Response DTO

- Schema：`GetBooksResponseDTO`

成功範例（`00000`）：

```json
{
  "code": "00000",
  "message": "Success",
  "data": {
    "books": [
      {
        "id": 1,
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "totalCopies": 3,
        "availableCopies": 2,
        "status": "AVAILABLE",
        "createdAt": "2026-03-02T14:00:00Z",
        "updatedAt": "2026-03-02T14:05:00Z"
      }
    ]
  }
}
```

---

## 4. 錯誤碼

| code | subCode | 說明 |
| --- | --- | --- |
| `A0000` | `A0002` | 查詢參數格式錯誤 |
| `B0000` | `B0002` | 查詢書籍失敗（系統異常） |
