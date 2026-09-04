# books Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | SD | 2026-09-04 | 建立書籍與館藏數量的測試資料表設計。 |

## 2. Schema

`books` 是書籍目錄與目前館藏數量的唯一來源。每一筆資料代表一種可被 ISBN 識別的書籍；副本不拆成獨立 row，借出與歸還透過數量欄位與 `loans` 的 active loan 紀錄維護。

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `book_id` | UUID | No | generated UUID | Primary key | 書籍內部識別值。 | `BookDTO.bookId`; all book and loan responses |
| `title` | VARCHAR(200) | No | None | Length 1-200 | 書名。 | `PostBooksRequestDTO.title`, `BookDTO.title` |
| `isbn` | VARCHAR(20) | No | None | Unique; length 1-20 | 書籍外部識別值；MVP 以 ISBN 實作。 | `PostBooksRequestDTO.isbn`, borrow/return requests, `BookDTO.isbn` |
| `author` | VARCHAR(200) | Yes | NULL | Length 0-200 when present | 作者；可選。 | `PostBooksRequestDTO.author`, `BookDTO.author` |
| `category` | VARCHAR(30) | No | None | Length 1-30 | 書籍分類。 | `PostBooksRequestDTO.category`, `BookDTO.category` |
| `total_count` | INTEGER | No | None | Must be greater than or equal to 1 | 初始建立的副本總數；本 MVP 不提供後續增補副本 API。 | `PostBooksRequestDTO.quantity`, `BookDTO.totalCount` |
| `available_count` | INTEGER | No | None | 0 <= value <= `total_count` | 尚可借出的副本數；借出減 1，歸還加 1。 | Derived `BookDTO.availableCount`; borrow/return transaction |
| `is_active` | BOOLEAN | No | TRUE | Inactive books cannot be borrowed | 書籍是否可供營運使用；建立時預設上架。 | `PostBooksRequestDTO.isActive`, `BookDTO.isActive` |
| `created_at` | TIMESTAMP | No | current timestamp | Immutable after insert | 建立時間。 | Persistence metadata; not exposed by current DTOs |
| `updated_at` | TIMESTAMP | No | current timestamp | Updated on mutation | 最近一次異動時間。 | Persistence metadata; not exposed by current DTOs |

書籍回應的 `status` 不落地儲存，依以下規則由 `is_active` 與 `available_count` 推導：`INACTIVE` when `is_active = false`; otherwise `BORROWED` when `available_count = 0`; otherwise `AVAILABLE`。

### 2.4 限制條件

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_books` | Primary key | `book_id` uniquely identifies a book row. | Persistence failure maps to `B0000`. |
| `uq_books_isbn` | Unique | One ISBN maps to one book row. | Duplicate create request maps to `A0000`. |
| `ck_books_total_count_positive` | Check | `total_count >= 1`. | Invalid create request maps to `A0000`. |
| `ck_books_available_count_range` | Check | `available_count >= 0 AND available_count <= total_count`. | Inconsistent transaction maps to `B0000`; client cannot set this field directly. |
| `ck_books_author_length` | Check | `author` is nullable; when present, value length is at most 200. | Invalid create request maps to `A0000`. |
| `books_status_derived` | Business rule | `status` is calculated and must not be persisted as an independent value. | Service maps the row to a stable `BookDTO.status`. |

## 3. DDL

```sql
CREATE TABLE books (
    book_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    author VARCHAR(200),
    category VARCHAR(30) NOT NULL,
    total_count INTEGER NOT NULL,
    available_count INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_total_count_positive CHECK (total_count >= 1),
    CONSTRAINT ck_books_available_count_range CHECK (available_count >= 0 AND available_count <= total_count),
    CONSTRAINT ck_books_author_length CHECK (author IS NULL OR CHAR_LENGTH(author) <= 200)
);
```
