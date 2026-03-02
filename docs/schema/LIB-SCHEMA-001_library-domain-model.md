# LIB-SCHEMA-001 Library Domain Model (SQLite)

## 文件資訊

- Schema ID: `LIB-SCHEMA-001`
- 來源需求: `REQ-LIB-001`
- 來源架構: `ARCH-LIB-001`
- 版本: `1.0.0`

## 1. 資料表設計

### 1.1 `books`

| 欄位 | 型別 | 約束 | 說明 |
| --- | --- | --- | --- |
| id | INTEGER | PK AUTOINCREMENT | 書籍主鍵 |
| title | TEXT | NOT NULL | 書名 |
| author | TEXT | NOT NULL | 作者 |
| total_quantity | INTEGER | NOT NULL CHECK(total_quantity > 0) | 庫存總數 |
| created_at | TEXT | NOT NULL | 建立時間（ISO-8601 UTC） |
| updated_at | TEXT | NOT NULL | 更新時間（ISO-8601 UTC） |

### 1.2 `borrow_records`

| 欄位 | 型別 | 約束 | 說明 |
| --- | --- | --- | --- |
| id | INTEGER | PK AUTOINCREMENT | 借閱紀錄主鍵 |
| book_id | INTEGER | NOT NULL FK -> books(id) | 關聯書籍 |
| borrower_name | TEXT | NOT NULL | 借閱人姓名 |
| status | TEXT | NOT NULL CHECK(status IN ('BORROWED', 'RETURNED')) | 借閱狀態 |
| borrowed_at | TEXT | NOT NULL | 借出時間（ISO-8601 UTC） |
| returned_at | TEXT | NULL | 歸還時間（ISO-8601 UTC） |
| created_at | TEXT | NOT NULL | 建立時間（ISO-8601 UTC） |
| updated_at | TEXT | NOT NULL | 更新時間（ISO-8601 UTC） |

## 2. 索引設計

```sql
CREATE INDEX idx_borrow_records_book_id ON borrow_records(book_id);
CREATE INDEX idx_borrow_records_status ON borrow_records(status);
CREATE INDEX idx_borrow_records_book_status ON borrow_records(book_id, status);
```

## 3. SQLite DDL（建議）

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE books (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    total_quantity INTEGER NOT NULL CHECK (total_quantity > 0),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE borrow_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id INTEGER NOT NULL,
    borrower_name TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('BORROWED', 'RETURNED')),
    borrowed_at TEXT NOT NULL,
    returned_at TEXT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id)
);
```

## 4. 關鍵業務規則對應

- 可借數量計算:

```sql
available_quantity = books.total_quantity
  - COUNT(borrow_records WHERE status='BORROWED' AND book_id=:bookId)
```

- 借書前檢查 `available_quantity > 0`，否則回傳業務錯誤碼（`A0000`）。
- 歸還時檢查借閱紀錄狀態為 `BORROWED`，若為 `RETURNED` 則拒絕並回傳 `A0000`。

## 5. 交易邊界

- 借書: 檢查庫存 + 新增 borrow_records 必須同交易。
- 還書: 驗證狀態 + 更新 borrow_records 必須同交易。
- 交易衝突或資料庫錯誤: 回傳 `B0000`，並保留例外堆疊。

## 6. 與 REQ/ARCH 對應

| 項目 | 對應 |
| --- | --- |
| FR-LIB-001 | `books` 建檔 |
| FR-LIB-002 | `books` 與 `borrow_records` 聚合計算 |
| FR-LIB-003 | `borrow_records` 新增 `BORROWED` |
| FR-LIB-004 | `borrow_records` 更新 `RETURNED` |
| FR-LIB-005 | `CHECK` + 交易 + 狀態驗證 |
| ARCH-LIB-001 4.x | SQLite 實體模型與一致性策略 |
