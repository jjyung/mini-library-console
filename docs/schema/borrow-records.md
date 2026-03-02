# Schema - Borrow Records

## 文件資訊

- 來源需求：`REQ-LIB-001`
- 來源架構：`ARCH-LIB-001`
- 實體名稱：`BorrowRecord`

---

## 1. 資料表

- Table: `borrow_records`

## 2. 欄位定義

| 欄位 | 型別 | 約束 | 說明 |
| --- | --- | --- | --- |
| `id` | INTEGER | PK, AUTOINCREMENT | 借閱紀錄唯一識別 |
| `book_id` | INTEGER | NOT NULL, FK -> `books.id` | 對應書籍 |
| `borrower_name` | TEXT | NOT NULL | 借閱人姓名 |
| `borrowed_at` | TEXT | NOT NULL | 借出時間（ISO-8601） |
| `returned_at` | TEXT | NULL | 歸還時間（ISO-8601） |
| `status` | TEXT | NOT NULL, CHECK (`status` IN ('BORROWED','RETURNED')) | 借閱狀態 |
| `created_at` | TEXT | NOT NULL | 建立時間（ISO-8601） |
| `updated_at` | TEXT | NOT NULL | 更新時間（ISO-8601） |

---

## 3. 索引設計

| 索引名稱 | 欄位 | 用途 |
| --- | --- | --- |
| `idx_borrow_records_book_id` | `book_id` | 依書籍查詢借閱紀錄 |
| `idx_borrow_records_status` | `status` | 查詢未歸還/已歸還紀錄 |
| `idx_borrow_records_borrower_name` | `borrower_name` | 依借閱人查詢 |

---

## 4. 交易一致性規則

借書交易（`library-borrow-records-001`）：

1. 驗證 `books.available_copies > 0`
2. 新增 `borrow_records`，`status = BORROWED`
3. 更新 `books.available_copies = available_copies - 1`
4. 依可借量重算 `books.status`

還書交易（`library-borrow-records-002`）：

1. 驗證 `borrow_records.status = BORROWED`
2. 更新 `borrow_records.status = RETURNED` 與 `returned_at`
3. 更新 `books.available_copies = available_copies + 1`
4. 依可借量重算 `books.status`

以上步驟必須在單一 transaction 內完成。

---

## 5. Traceability

- 對應 FR：`FR-LIB-003`, `FR-LIB-004`, `FR-LIB-005`
- 對應 AC：`AC-LIB-002`, `AC-LIB-003`, `AC-LIB-004`, `AC-LIB-005`
- 對應 NFR：`NFR-LIB-002`
