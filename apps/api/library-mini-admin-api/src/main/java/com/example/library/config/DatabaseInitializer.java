package com.example.library.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS books (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              title TEXT NOT NULL,
              author TEXT NOT NULL,
              total_copies INTEGER NOT NULL CHECK (total_copies >= 1),
              available_copies INTEGER NOT NULL CHECK (available_copies >= 0 AND available_copies <= total_copies),
              status TEXT NOT NULL CHECK (status IN ('AVAILABLE','CHECKED_OUT')),
              created_at TEXT NOT NULL,
              updated_at TEXT NOT NULL
            )
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS borrow_records (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              book_id INTEGER NOT NULL,
              borrower_name TEXT NOT NULL,
              borrowed_at TEXT NOT NULL,
              returned_at TEXT,
              status TEXT NOT NULL CHECK (status IN ('BORROWED','RETURNED')),
              created_at TEXT NOT NULL,
              updated_at TEXT NOT NULL,
              FOREIGN KEY (book_id) REFERENCES books(id)
            )
            """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_books_status ON books(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_borrow_records_book_id ON borrow_records(book_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_borrow_records_status ON borrow_records(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_borrow_records_borrower_name ON borrow_records(borrower_name)");
    }
}
