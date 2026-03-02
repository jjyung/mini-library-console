CREATE TABLE IF NOT EXISTS id_sequences (
  sequence_name TEXT PRIMARY KEY,
  current_value INTEGER NOT NULL
);

INSERT OR IGNORE INTO id_sequences (sequence_name, current_value) VALUES ('BOOK', 0);
INSERT OR IGNORE INTO id_sequences (sequence_name, current_value) VALUES ('LOAN', 0);

CREATE TABLE IF NOT EXISTS books (
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

CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_shelf_status ON books(shelf_status);
CREATE INDEX IF NOT EXISTS idx_books_circulation_status ON books(circulation_status);

CREATE TABLE IF NOT EXISTS loans (
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

CREATE INDEX IF NOT EXISTS idx_loans_isbn_status ON loans(isbn, status);
CREATE INDEX IF NOT EXISTS idx_loans_reader_status ON loans(reader_id, status);
CREATE INDEX IF NOT EXISTS idx_loans_due_date_status ON loans(due_date, status);
