# loans Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | Codex / SD | 2026-09-08 | 建立 TEST MVP 借閱紀錄 schema；以唯一 loan identity 支援多複本歸還。 |

## 2. Schema

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `loan_id` | `UUID` | 否 | 無 | 主鍵；建立後不可變。 | 一次借閱紀錄的唯一識別值。 | `loanId` |
| `book_id` | `UUID` | 否 | 無 | 外鍵參照 `books.book_id`。 | 被借閱的書目識別值。 | `bookId` |
| `reader_id` | `VARCHAR(100)` | 否 | 無 | 去除前後空白後不可為空；MVP 可承載 reader name 或 reader ID。 | 借閱人識別值。 | `readerId` |
| `borrowed_at` | `TIMESTAMP WITH TIME ZONE` | 否 | `CURRENT_TIMESTAMP` | 使用 UTC；不可晚於 `returned_at`。 | 借出時間。 | `borrowedAt` |
| `due_date` | `DATE` | 是 | `NULL` | MVP 可選；不因逾期自動產生罰款。 | 借閱到期日。 | `dueDate` |
| `returned_at` | `TIMESTAMP WITH TIME ZONE` | 是 | `NULL` | ACTIVE 時為 NULL；RETURNED 時必須有值。 | 實際歸還時間。 | `returnedAt` |
| `status` | `VARCHAR(20)` | 否 | `'ACTIVE'` | 僅接受 `ACTIVE` 或 `RETURNED`，且須符合 returned_at mapping。 | 借閱紀錄狀態。 | `status` |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | 否 | `CURRENT_TIMESTAMP` | 建立後不可修改；使用 UTC。 | 資料建立時間。 | `createdAt` |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | 否 | `CURRENT_TIMESTAMP` | 每次成功異動更新；不可早於 `created_at`。 | 最近異動時間。 | `updatedAt` |

### 2.4 限制條件 (Constraints)

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_loans` | 主鍵 | `loan_id` 唯一識別一筆借閱。 | 回傳 `B0000` 或由服務層轉為可理解的錯誤；不可暴露資料庫細節。 |
| `fk_loans_book` | 外鍵 | `book_id` 必須對應既有 `books.book_id`。 | 書目不存在時由服務層回傳 `A0000`。 |
| `ck_loans_reader_not_blank` | 檢查條件 | `TRIM(reader_id)` 不可為空字串。 | 回傳 `A0000`，並可在 error details 指向 `readerId`。 |
| `ck_loans_status` | 檢查條件 | `status` 僅可為 `ACTIVE` 或 `RETURNED`。 | 回傳 `A0000` 或視為資料完整性錯誤回傳 `B0000`。 |
| `ck_loans_return_mapping` | 檢查條件 | `ACTIVE` 必須 `returned_at IS NULL`；`RETURNED` 必須 `returned_at IS NOT NULL`。 | 視為資料完整性錯誤，回傳 `B0000`。 |
| `ck_loans_time_order` | 檢查條件 | `returned_at` 有值時不可早於 `borrowed_at`。 | 視為資料完整性錯誤，回傳 `B0000`。 |
| `ck_loans_timestamp_order` | 檢查條件 | `updated_at` 不可早於 `created_at`。 | 視為資料完整性錯誤，回傳 `B0000`。 |
| `idx_loans_book_status` | 查詢索引 | 支援依書目與 ACTIVE 狀態定位可歸還借閱。 | 查詢效能不足時由 SD／BE 調整，不能改變業務結果。 |

## 3. DDL

```sql
--liquibase formatted sql
--changeset sd:loans-001
CREATE TABLE loans (
    loan_id UUID NOT NULL,
    book_id UUID NOT NULL,
    reader_id VARCHAR(100) NOT NULL,
    borrowed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NULL,
    returned_at TIMESTAMP WITH TIME ZONE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_loans PRIMARY KEY (loan_id),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT ck_loans_reader_not_blank CHECK (TRIM(reader_id) <> ''),
    CONSTRAINT ck_loans_status CHECK (status IN ('ACTIVE', 'RETURNED')),
    CONSTRAINT ck_loans_return_mapping CHECK (
        (status = 'ACTIVE' AND returned_at IS NULL)
        OR (status = 'RETURNED' AND returned_at IS NOT NULL)
    ),
    CONSTRAINT ck_loans_time_order CHECK (
        returned_at IS NULL OR returned_at >= borrowed_at
    ),
    CONSTRAINT ck_loans_timestamp_order CHECK (updated_at >= created_at)
);

CREATE INDEX idx_loans_book_status ON loans (book_id, status);
CREATE INDEX idx_loans_reader_status ON loans (reader_id, status);
```
