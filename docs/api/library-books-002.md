# library-books-002 List Books API

## 1. 基本資訊

- API ID: `library-books-002`
- 名稱: 查詢書籍清單
- Method/Path: `GET /books`
- operationId: `books.get`
- 來源需求: `FR-LIB-002`

## 2. Request DTO

- Schema: `GetBooksRequestDTO`
- 本版無查詢欄位，保留擴充空間。

## 3. Response DTO

- Schema: `GetBooksResponseDTO`
- 成功碼: `00000`

`data.books[*]` 欄位包含：

- `totalQuantity`
- `availableQuantity`
- `status`（`AVAILABLE`/`BORROWED`）

## 4. 錯誤碼

- `B0000`: 系統異常（資料庫讀取失敗）

## 5. 業務規則

- `availableQuantity = totalQuantity - BORROWED紀錄數`
- 若 `availableQuantity == 0`，`status` 顯示 `BORROWED`。
