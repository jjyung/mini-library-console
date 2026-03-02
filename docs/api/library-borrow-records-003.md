# API Design - library-borrow-records-003

## 1. 基本資訊

- API ID：`library-borrow-records-003`
- API 名稱：List Borrow Records
- 方法與路徑：`GET /api/v1/borrow-records`
- operationId：`getApiV1BorrowRecords`
- 對應需求：`FR-LIB-005`, `AC-LIB-005`

---

## 2. Request DTO

- Schema：`GetBorrowRecordsRequestDTO`
- Query Parameters：
  - `status`（optional，`BORROWED` / `RETURNED`）
  - `bookId`（optional）
  - `borrowerName`（optional）

---

## 3. Response DTO

- Schema：`GetBorrowRecordsResponseDTO`

成功範例（`00000`）：

```json
{
  "code": "00000",
  "message": "Success",
  "data": {
    "borrowRecords": [
      {
        "id": 101,
        "bookId": 1,
        "bookTitle": "Clean Code",
        "borrowerName": "Samson",
        "borrowedAt": "2026-03-02T14:10:00Z",
        "returnedAt": "2026-03-02T14:30:00Z",
        "status": "RETURNED"
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
| `B0000` | `B0005` | 查詢借閱紀錄失敗（系統異常） |
