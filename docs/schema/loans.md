# loans Schema

## 1. 修改紀錄

| Version | Who | When | Why / What |
| --- | --- | --- | --- |
| 1.0.0 | SD | 2026-09-04 | 建立借閱、歸還與 synthetic reader identity 的測試資料表設計。 |

## 2. Schema

`loans` 紀錄每次借出事件。借閱中的 loan 以 `returned_at IS NULL` 表示；歸還時只關閉指定的 active loan，不刪除歷史資料。`reader_id` 是 test-only synthetic value，不代表登入帳號，也不建立 users table。

### 2.3 欄位定義

| Column | Type | Nullable | Default | Constraint / Rule | Description | API Mapping |
| --- | --- | --- | --- | --- | --- | --- |
| `loan_id` | UUID | No | generated UUID | Primary key | 借閱事件內部識別值。 | `LoanDTO.loanId` |
| `book_id` | UUID | No | None | Foreign key to `books.book_id` | 被借閱的書籍識別值。 | `LoanDTO.bookId`; resolved from request ISBN |
| `reader_id` | VARCHAR(100) | No | None | Length 1-100; synthetic test value | 借閱人測試識別值；不連結登入帳號。 | `PostLoansBorrowRequestDTO.readerId`, optional return selector, `LoanDTO.readerId` |
| `borrowed_at` | TIMESTAMP | No | current timestamp | Immutable after insert | 借出時間。 | `LoanDTO.borrowedAt` |
| `due_date` | DATE | Yes | NULL | Optional date; no fine calculation in MVP | 預定歸還日；僅保存資料，不計算罰款。 | `PostLoansBorrowRequestDTO.dueDate`, `LoanDTO.dueDate` |
| `returned_at` | TIMESTAMP | Yes | NULL | Null means active loan | 實際歸還時間。 | `LoanDTO.returnedAt`; set by return API |
| `created_at` | TIMESTAMP | No | current timestamp | Immutable after insert | 建立時間。 | Persistence metadata; not exposed by current DTOs |
| `updated_at` | TIMESTAMP | No | current timestamp | Updated on return | 最近一次異動時間。 | Persistence metadata; not exposed by current DTOs |

### 2.4 限制條件

| Constraint | Type | Definition / Intent | Failure Handling |
| --- | --- | --- | --- |
| `pk_loans` | Primary key | `loan_id` uniquely identifies a loan event. | Persistence failure maps to `B0000`. |
| `fk_loans_book` | Foreign key | Every loan references an existing `books.book_id`. | Unknown book maps to `A0000`; referential failure maps to `B0000`. |
| `ck_loans_reader_id_non_empty` | Check | `reader_id` must contain at least one character. | Invalid borrow request maps to `A0000`. |
| `ck_loans_return_order` | Check | `returned_at` is null or not earlier than `borrowed_at`. | Inconsistent update maps to `B0000`. |
| `uq_loans_active_selection` | Business rule | A book may have multiple active loans for quantity greater than one; return without `readerId` is valid only when exactly one active loan matches the ISBN. | Ambiguous or missing active loan maps to `A0000`. |
| `loan_active_definition` | Business rule | Active loan means `returned_at IS NULL`; returned rows remain queryable history. | Return closes one row and maps the updated row to `LoanDTO`. |

## 3. DDL

```sql
CREATE TABLE loans (
    loan_id UUID NOT NULL,
    book_id UUID NOT NULL,
    reader_id VARCHAR(100) NOT NULL,
    borrowed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE,
    returned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_loans PRIMARY KEY (loan_id),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT ck_loans_reader_id_non_empty CHECK (CHAR_LENGTH(reader_id) >= 1),
    CONSTRAINT ck_loans_return_order CHECK (returned_at IS NULL OR returned_at >= borrowed_at)
);
```
