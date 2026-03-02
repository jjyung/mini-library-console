# API Design - library-borrow-records-002

## 1. 基本資訊

- API ID：`library-borrow-records-002`
- API 名稱：Return Borrow Record
- 方法與路徑：`PATCH /api/v1/borrow-records/{borrowRecordId}/return`
- operationId：`patchApiV1BorrowRecordsByBorrowRecordIdReturn`
- 對應需求：`FR-LIB-004`, `FR-LIB-005`, `AC-LIB-004`

---

## 2. Request DTO

- Schema：`PatchBorrowRecordsRequestDTO`
- Path Parameter：`borrowRecordId`（required）

```json
{
  "returnedBy": "Samson"
}
```

驗證：

- `borrowRecordId` 必填，正整數
- `returnedBy` 必填，1..80 字元

---

## 3. Response DTO

- Schema：`PatchBorrowRecordsResponseDTO`

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
      "returnedAt": "2026-03-02T14:30:00Z",
      "status": "RETURNED"
    }
  }
}
```

---

## 4. 錯誤碼

| code | subCode | 說明 |
| --- | --- | --- |
| `A0000` | `A0005` | 借閱紀錄不存在 |
| `A0000` | `A0006` | 借閱紀錄已歸還 |
| `A0000` | `A0001` | 欄位驗證失敗 |
| `B0000` | `B0004` | 還書交易執行失敗 |
