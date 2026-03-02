# library-borrow-records-002 Return Book API

## 1. 基本資訊

- API ID: `library-borrow-records-002`
- 名稱: 歸還書籍
- Method/Path: `PATCH /borrow-records/{borrowRecordId}/return`
- operationId: `borrow-records-borrowRecordId-return.patch`
- 來源需求: `FR-LIB-004`, `FR-LIB-005`

## 2. Request DTO

- Schema: `PatchBorrowRecordsRequestDTO`
- 本版允許空 payload（`{}`）。

Path 參數：

| 欄位 | 型別 | 必填 | 規則 |
| --- | --- | --- | --- |
| borrowRecordId | integer(int64) | Y | >= 1 |

## 3. Response DTO

- Schema: `PatchBorrowRecordsResponseDTO`
- 成功碼: `00000`

`data` 需同時回傳：

- `borrowRecord`（狀態更新為 `RETURNED`）
- `book`（恢復後可借數量與狀態）

## 4. 錯誤碼

- `A0000`: 參數不合法、借閱紀錄不存在、重複歸還
- `B0000`: 交易失敗、資料庫錯誤

## 5. 業務規則

- 僅 `BORROWED` 紀錄可歸還。
- 歸還流程必須在單一交易中完成。
