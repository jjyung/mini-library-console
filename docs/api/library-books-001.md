# library-books-001 Create Book API

## 1. 基本資訊

- API ID: `library-books-001`
- 名稱: 建立書籍
- Method/Path: `POST /books`
- operationId: `books.post`
- 來源需求: `FR-LIB-001`

## 2. Request DTO

- Schema: `PostBooksRequestDTO`

| 欄位 | 型別 | 必填 | 規則 |
| --- | --- | --- | --- |
| title | string | Y | 1..200 |
| author | string | Y | 1..120 |
| totalQuantity | integer | Y | >= 1 |

## 3. Response DTO

- Schema: `PostBooksResponseDTO`
- 成功碼: `00000`

`data.book` 為新增後書籍資料。

## 4. 錯誤碼

- `A0000`: 請求欄位不合法（空值、長度、數值範圍）
- `B0000`: 系統異常（資料庫錯誤、交易失敗）

## 5. 業務規則

- 新增後需可被 `GET /books` 立即查得。
- 不依賴任何外部服務。
