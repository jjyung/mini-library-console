# API Design - library-borrow-records-001

## 1. 基本資訊

- API ID：`library-borrow-records-001`
- API 名稱：Borrow Book
- 方法與路徑：`POST /api/v1/borrow-records`
- operationId：`postApiV1BorrowRecords`
- 對應需求：`FR-LIB-003`, `FR-LIB-005`, `AC-LIB-002`, `AC-LIB-003`

---

## 2. Request DTO

- Schema：`PostBorrowRecordsRequestDTO`

```json
{
  "bookId": 1,
  "borrowerName": "Samson"
}
```

驗證：

- `bookId` 必填，正整數
- `borrowerName` 必填，1..80 字元

---

## 3. Response DTO

- Schema：`PostBorrowRecordsResponseDTO`

成功範例（`00000`）：

```json
{
  "code": "00000",
  "message": "Success",
  "data": {
    "borrowRecord": {
      "id": 101,
      "bookId": 1,
      "bookTitle": "Clean Code",
      "borrowerName": "Samson",
      "borrowedAt": "2026-03-02T14:10:00Z",
      "returnedAt": null,
      "status": "BORROWED"
    }
  }
}
```

---

## 4. 錯誤碼

| code | subCode | 說明 |
| --- | --- | --- |
| `A0000` | `A0003` | 書籍不存在 |
| `A0000` | `A0004` | 可借庫存不足 |
| `A0000` | `A0001` | 欄位驗證失敗 |
| `B0000` | `B0003` | 借書交易執行失敗 |
