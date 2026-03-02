# loans table design

## Table Purpose

- Store each borrow-return transaction record.
- Support active loan lookup and overdue/fine calculation at return time.

## DDL (SQLite)

```sql
CREATE TABLE loans (
  loan_id TEXT PRIMARY KEY,
  isbn TEXT NOT NULL,
  reader_id TEXT NOT NULL,
  borrowed_at TEXT NOT NULL,
  due_date TEXT NOT NULL,
  returned_at TEXT,
  overdue_days INTEGER NOT NULL DEFAULT 0 CHECK (overdue_days >= 0),
  fine_amount INTEGER NOT NULL DEFAULT 0 CHECK (fine_amount >= 0),
  status TEXT NOT NULL CHECK (status IN ('BORROWED', 'RETURNED')),
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (isbn) REFERENCES books(isbn)
);

CREATE INDEX idx_loans_isbn_status ON loans(isbn, status);
CREATE INDEX idx_loans_reader_status ON loans(reader_id, status);
CREATE INDEX idx_loans_due_date_status ON loans(due_date, status);
```

## Columns

| Column | Type | Null | Constraints | Description |
| --- | --- | --- | --- | --- |
| loan_id | TEXT | N | PK | Loan transaction ID (e.g. LN00000001) |
| isbn | TEXT | N | FK -> books.isbn | Borrowed book ISBN |
| reader_id | TEXT | N |  | Borrowing reader ID |
| borrowed_at | TEXT | N | ISO-8601 datetime | Borrow timestamp |
| due_date | TEXT | N | YYYY-MM-DD | Due date |
| returned_at | TEXT | Y | ISO-8601 datetime | Return timestamp |
| overdue_days | INTEGER | N | `>= 0` | Computed overdue days at return |
| fine_amount | INTEGER | N | `>= 0` | Computed fine amount (NTD) |
| status | TEXT | N | enum | BORROWED or RETURNED |
| created_at | TEXT | N | ISO-8601 datetime | Create timestamp |
| updated_at | TEXT | N | ISO-8601 datetime | Last update timestamp |

## Business Constants

- `DEFAULT_LOAN_PERIOD_DAYS = 14`
- `FINE_PER_DAY_TWD = 5`

## Domain Rules (Given/When/Then)

1. Given borrow request without `dueDate`
   When borrow succeeds
   Then `due_date = borrowed_at_date + 14 days` and response marks default due date applied.

2. Given active inventory is available and on shelf
   When borrow succeeds
   Then create one `BORROWED` loan row with `returned_at = null`.

3. Given no `BORROWED` row exists for target `isbn` (and optional `reader_id` condition)
   When return is requested
   Then reject with `A0006`.

4. Given return timestamp date is after `due_date`
   When return succeeds
   Then set `overdue_days > 0`, `fine_amount = overdue_days * 5`, and response code `A0007`.

5. Given return timestamp date is on/before `due_date`
   When return succeeds
   Then set `overdue_days = 0`, `fine_amount = 0`, and response code `00000`.

6. Given a loan row is already `RETURNED`
   When duplicate return is requested
   Then keep data unchanged and return `A0006` as no active borrow record.

## Transaction Boundary

- Borrow:
  1. validate book state
  2. decrement inventory
  3. insert loan row
  4. commit
- Return:
  1. load active loan row
  2. compute overdue/fine
  3. update loan to RETURNED
  4. increment inventory
  5. commit
