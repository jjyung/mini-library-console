# library-borrow-records-001 Borrow Book API

## 1. 基本資訊

- API ID: `library-borrow-records-001`
- 名稱: 借出書籍
- Method/Path: `POST /borrow-records`
- operationId: `borrow-records.post`
- 來源需求: `FR-LIB-003`, `FR-LIB-005`

## 2. Request DTO

- Schema: `PostBorrowRecordsRequestDTO`

| 欄位 | 型別 | 必填 | 規則 |
| --- | --- | --- | --- |
| bookId | integer(int64) | Y | >= 1 |
| borrowerName | string | Y | 1..80 |

## 3. Response DTO

- Schema: `PostBorrowRecordsResponseDTO`
- 成功碼: `00000`

`data` 需同時回傳：

- `borrowRecord`（新建 `BORROWED`）
- `book`（更新後可借數量與狀態）

## 4. 錯誤碼

- `A0000`: 請求不合法、書籍不存在、可借數量不足
- `B0000`: 交易失敗、資料庫錯誤

## 5. 業務規則

- 借出前需檢查 `availableQuantity > 0`。
- 借出流程必須在單一交易中完成。
