# books Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-09-08 | 建立 TEST MVP 書目與館藏數量 schema；支援新增書籍、狀態查詢與庫存不變量。 |

## 2. Schema

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `book_id` | `UUID` | 否 | 無 | 主鍵；建立後不可變。 | 書目的穩定識別值。 | `bookId` |
| `title` | `VARCHAR(200)` | 否 | 無 | 去除前後空白後不可為空。 | 書名。 | `title` |
| `isbn` | `VARCHAR(20)` | 否 | 無 | 不可為空；全域唯一。 | 書目的唯一 ISBN。 | `isbn` |
| `author` | `VARCHAR(100)` | 是 | `NULL` | 若有值，去除前後空白後不可為空；MVP baseline 可省略。 | 作者名稱。 | `author` |
| `category` | `VARCHAR(20)` | 否 | 無 | 僅接受 Figma baseline 分類值。 | 書籍分類。 | `category` |
| `total_count` | `INTEGER` | 否 | 無 | 必須大於零。 | 書目總複本數。 | `totalCount` |
| `available_count` | `INTEGER` | 否 | 無 | 必須介於零與 `total_count` 之間。 | 目前可借複本數。 | `availableCount` |
| `status` | `VARCHAR(20)` | 否 | `'AVAILABLE'` | 僅接受 `AVAILABLE`、`BORROWED`、`INACTIVE`，且須符合數量與上架狀態 mapping。 | 書目目前狀態。 | `status` |
| `is_active` | `BOOLEAN` | 否 | `TRUE` | `FALSE` 時 status 必須為 `INACTIVE`。 | 是否上架。 | `isActive` |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | 否 | `CURRENT_TIMESTAMP` | 建立後不可修改；使用 UTC。 | 建立時間。 | `createdAt` |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | 否 | `CURRENT_TIMESTAMP` | 每次成功異動更新；不可早於 `created_at`。 | 最近異動時間。 | `updatedAt` |

### 2.4 限制條件 (Constraints)

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_books` | 主鍵 | `book_id` 唯一識別一筆書目。 | 回傳 `B0000` 或由服務層轉為可理解的錯誤；不可直接暴露資料庫細節。 |
| `uk_books_isbn` | 唯一索引 | `isbn` 在所有書目中唯一。 | 回傳 `A0000`；API flow 對應 ISBN conflict。 |
| `ck_books_title_not_blank` | 檢查條件 | `TRIM(title)` 不可為空字串。 | 回傳 `A0000`，並可在 error details 指向 `title`。 |
| `ck_books_isbn_not_blank` | 檢查條件 | `TRIM(isbn)` 不可為空字串。 | 回傳 `A0000`，並可在 error details 指向 `isbn`。 |
| `ck_books_author_not_blank` | 檢查條件 | `author` 為 NULL 或 `TRIM(author)` 不可為空字串。 | 回傳 `A0000`。 |
| `ck_books_category` | 檢查條件 | `category` 僅可為 `literature`、`science`、`technology`、`history`、`art`、`philosophy`、`business` 或 `education`。 | 回傳 `A0000`。 |
| `ck_books_total_positive` | 檢查條件 | `total_count` 必須大於零。 | 回傳 `A0000`。 |
| `ck_books_available_range` | 檢查條件 | `available_count` 必須大於或等於零且不大於 `total_count`。 | 視為資料完整性錯誤，回傳 `B0000`。 |
| `ck_books_status_mapping` | 檢查條件 | 上架且可借數大於零為 `AVAILABLE`；上架且可借數為零為 `BORROWED`；未上架為 `INACTIVE`。 | 視為資料完整性錯誤，回傳 `B0000`。 |
| `ck_books_timestamp_order` | 檢查條件 | `updated_at` 不可早於 `created_at`。 | 視為資料完整性錯誤，回傳 `B0000`。 |

## 3. DDL

```sql
--liquibase formatted sql
--changeset sd:books-001
CREATE TABLE books (
    book_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    author VARCHAR(100) NULL,
    category VARCHAR(20) NOT NULL,
    total_count INTEGER NOT NULL,
    available_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT ck_books_title_not_blank CHECK (TRIM(title) <> ''),
    CONSTRAINT ck_books_isbn_not_blank CHECK (TRIM(isbn) <> ''),
    CONSTRAINT ck_books_author_not_blank CHECK (author IS NULL OR TRIM(author) <> ''),
    CONSTRAINT ck_books_category CHECK (
        category IN (
            'literature', 'science', 'technology', 'history',
            'art', 'philosophy', 'business', 'education'
        )
    ),
    CONSTRAINT ck_books_total_positive CHECK (total_count > 0),
    CONSTRAINT ck_books_available_range CHECK (
        available_count >= 0 AND available_count <= total_count
    ),
    CONSTRAINT ck_books_status_mapping CHECK (
        (is_active = FALSE AND status = 'INACTIVE')
        OR (
            is_active = TRUE
            AND (
                (available_count > 0 AND status = 'AVAILABLE')
                OR (available_count = 0 AND status = 'BORROWED')
            )
        )
    ),
    CONSTRAINT ck_books_timestamp_order CHECK (updated_at >= created_at)
);

CREATE UNIQUE INDEX uk_books_isbn ON books (isbn);
CREATE INDEX idx_books_status_updated_at ON books (status, updated_at);
```
