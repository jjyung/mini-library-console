# Schema - Books

## 文件資訊

- 來源需求：`REQ-LIB-001`
- 來源架構：`ARCH-LIB-001`
- 實體名稱：`Book`

---

## 1. 資料表

- Table: `books`

## 2. 欄位定義

| 欄位 | 型別 | 約束 | 說明 |
| --- | --- | --- | --- |
| `id` | INTEGER | PK, AUTOINCREMENT | 書籍唯一識別 |
| `title` | TEXT | NOT NULL | 書名 |
| `author` | TEXT | NOT NULL | 作者 |
| `total_copies` | INTEGER | NOT NULL, CHECK (`total_copies` >= 1) | 總庫存 |
| `available_copies` | INTEGER | NOT NULL, CHECK (`available_copies` >= 0 AND `available_copies` <= `total_copies`) | 可借庫存 |
| `status` | TEXT | NOT NULL, CHECK (`status` IN ('AVAILABLE','CHECKED_OUT')) | 書籍狀態 |
| `created_at` | TEXT | NOT NULL | 建立時間（ISO-8601） |
| `updated_at` | TEXT | NOT NULL | 更新時間（ISO-8601） |

---

## 3. 索引設計

| 索引名稱 | 欄位 | 用途 |
| --- | --- | --- |
| `idx_books_title` | `title` | 支援書名關鍵字查詢 |
| `idx_books_status` | `status` | 支援狀態過濾 |

---

## 4. 狀態轉換規則

- `available_copies` > 0 -> `status = AVAILABLE`
- `available_copies` = 0 -> `status = CHECKED_OUT`

後端不得信任前端狀態輸入，狀態一律由領域規則推導。

---

## 5. Traceability

- 對應 FR：`FR-LIB-001`, `FR-LIB-002`, `FR-LIB-003`, `FR-LIB-004`
- 對應 NFR：`NFR-LIB-002`, `NFR-LIB-003`
