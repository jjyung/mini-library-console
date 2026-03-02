# books table design

## Table Purpose

- Store book catalog and inventory at ISBN aggregate level.
- Support add, list, search, borrow, and return business flows.

## DDL (SQLite)

```sql
CREATE TABLE books (
  book_id TEXT PRIMARY KEY,
  isbn TEXT NOT NULL UNIQUE,
  title TEXT NOT NULL,
  author TEXT,
  category TEXT NOT NULL,
  total_quantity INTEGER NOT NULL CHECK (total_quantity >= 1),
  available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0),
  shelf_status TEXT NOT NULL CHECK (shelf_status IN ('ON_SHELF', 'OFF_SHELF')),
  circulation_status TEXT NOT NULL CHECK (circulation_status IN ('AVAILABLE', 'BORROWED_OUT', 'OFF_SHELF')),
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  CHECK (available_quantity <= total_quantity)
);

CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_shelf_status ON books(shelf_status);
CREATE INDEX idx_books_circulation_status ON books(circulation_status);
```

## Columns

| Column | Type | Null | Constraints | Description |
| --- | --- | --- | --- | --- |
| book_id | TEXT | N | PK | Internal book identifier (e.g. BK00000001) |
| isbn | TEXT | N | UNIQUE | Business key for book aggregate |
| title | TEXT | N |  | Book title |
| author | TEXT | Y |  | Book author |
| category | TEXT | N |  | Book category |
| total_quantity | INTEGER | N | `>= 1` | Total copies |
| available_quantity | INTEGER | N | `>= 0`, `<= total_quantity` | Available copies |
| shelf_status | TEXT | N | enum | ON_SHELF or OFF_SHELF |
| circulation_status | TEXT | N | enum | AVAILABLE / BORROWED_OUT / OFF_SHELF |
| created_at | TEXT | N | ISO-8601 datetime | Create timestamp |
| updated_at | TEXT | N | ISO-8601 datetime | Last update timestamp |

## State Mapping Rules

- `shelf_status = OFF_SHELF` => `circulation_status = OFF_SHELF`.
- `shelf_status = ON_SHELF` and `available_quantity = 0` => `circulation_status = BORROWED_OUT`.
- `shelf_status = ON_SHELF` and `available_quantity > 0` => `circulation_status = AVAILABLE`.

## Domain Rules (Given/When/Then)

1. Given `isbn` already exists
   When creating a new book with same `isbn`
   Then reject with `A0002` and do not insert row.

2. Given required fields (`title`, `isbn`, `category`, `quantity`, `shelf_status`) are missing or invalid
   When creating or updating inventory
   Then reject with `A0003`.

3. Given book exists and is on shelf with `available_quantity > 0`
   When borrow transaction succeeds
   Then `available_quantity = available_quantity - 1` and `updated_at` changes.

4. Given book exists
   When return transaction succeeds
   Then `available_quantity = available_quantity + 1` (up to `total_quantity`) and `updated_at` changes.

5. Given `isbn` not found in books table
   When borrowing or returning
   Then reject with `A0001`.

## Concurrency and Transaction Boundary

- Borrow/return must run inside single DB transaction.
- Borrow SQL update condition must include `available_quantity > 0` to prevent oversell under race.
- Return SQL update must cap at `total_quantity` and validate active loan exists before increment.
