# API Design - library-books-001

## 1. 基本資訊

- API ID：`library-books-001`
- API 名稱：Create Book
- 方法與路徑：`POST /api/v1/books`
- operationId：`postApiV1Books`
- 對應需求：`FR-LIB-001`, `AC-LIB-001`

---

## 2. Request DTO

- Schema：`PostBooksRequestDTO`

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "totalCopies": 3
}
```

驗證：

- `title` 必填，1..120 字元
- `author` 必填，1..80 字元
- `totalCopies` 必填，1..9999

---

## 3. Response DTO

- Schema：`PostBooksResponseDTO`

成功範例（`00000`）：

```json
{
  "code": "00000",
  "message": "Success",
  "data": {
    "book": {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "totalCopies": 3,
      "availableCopies": 3,
      "status": "AVAILABLE",
      "createdAt": "2026-03-02T14:00:00Z",
      "updatedAt": "2026-03-02T14:00:00Z"
    }
  }
}
```

---

## 4. 錯誤碼

| code | subCode | 說明 |
| --- | --- | --- |
| `A0000` | `A0001` | 欄位驗證失敗 |
| `B0000` | `B0001` | 建立書籍失敗（系統異常） |
