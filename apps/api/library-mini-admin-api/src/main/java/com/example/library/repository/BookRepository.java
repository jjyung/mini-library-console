package com.example.library.repository;

import com.example.library.domain.BookRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {
    private static final RowMapper<BookRecord> BOOK_ROW_MAPPER = (resultSet, rowNumber) -> {
        BookRecord bookRecord = new BookRecord();
        bookRecord.setBookId(resultSet.getString("book_id"));
        bookRecord.setIsbn(resultSet.getString("isbn"));
        bookRecord.setTitle(resultSet.getString("title"));
        bookRecord.setAuthor(resultSet.getString("author"));
        bookRecord.setCategory(resultSet.getString("category"));
        bookRecord.setTotalQuantity(resultSet.getInt("total_quantity"));
        bookRecord.setAvailableQuantity(resultSet.getInt("available_quantity"));
        bookRecord.setShelfStatus(resultSet.getString("shelf_status"));
        bookRecord.setCirculationStatus(resultSet.getString("circulation_status"));
        bookRecord.setCurrentBorrowerReaderId(resultSet.getString("current_borrower_reader_id"));
        bookRecord.setCurrentDueDate(resultSet.getString("current_due_date"));
        bookRecord.setCreatedAt(resultSet.getString("created_at"));
        bookRecord.setUpdatedAt(resultSet.getString("updated_at"));
        return bookRecord;
    };

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByIsbn(String isbn) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE isbn = ?", Integer.class, isbn);
        return count != null && count > 0;
    }

    public void insert(BookRecord bookRecord) {
        jdbcTemplate.update("""
                INSERT INTO books(book_id,isbn,title,author,category,total_quantity,available_quantity,shelf_status,circulation_status,created_at,updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """,
                bookRecord.getBookId(),
                bookRecord.getIsbn(),
                bookRecord.getTitle(),
                bookRecord.getAuthor(),
                bookRecord.getCategory(),
                bookRecord.getTotalQuantity(),
                bookRecord.getAvailableQuantity(),
                bookRecord.getShelfStatus(),
                bookRecord.getCirculationStatus(),
                bookRecord.getCreatedAt(),
                bookRecord.getUpdatedAt());
    }

    public Optional<BookRecord> findByIsbn(String isbn) {
        List<BookRecord> records = jdbcTemplate.query(queryWithBorrower() + " WHERE b.isbn = ?", BOOK_ROW_MAPPER, isbn);
        return records.stream().findFirst();
    }

    public List<BookRecord> listBooks(String shelfStatus) {
        if (shelfStatus == null || shelfStatus.isBlank()) {
            return jdbcTemplate.query(queryWithBorrower() + " ORDER BY b.updated_at DESC", BOOK_ROW_MAPPER);
        }
        return jdbcTemplate.query(queryWithBorrower() + " WHERE b.shelf_status = ? ORDER BY b.updated_at DESC", BOOK_ROW_MAPPER, shelfStatus);
    }

    public List<BookRecord> searchBooks(String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return jdbcTemplate.query(queryWithBorrower() + " WHERE lower(b.title) LIKE ? OR lower(b.isbn) LIKE ? OR lower(IFNULL(b.author, '')) LIKE ? ORDER BY b.updated_at DESC",
                BOOK_ROW_MAPPER, pattern, pattern, pattern);
    }

    public int decreaseAvailable(String isbn, String circulationStatus, String updatedAt) {
        return jdbcTemplate.update("""
                UPDATE books
                SET available_quantity = available_quantity - 1,
                    circulation_status = ?,
                    updated_at = ?
                WHERE isbn = ? AND available_quantity > 0
                """, circulationStatus, updatedAt, isbn);
    }

    public int increaseAvailable(String isbn, String circulationStatus, String updatedAt) {
        return jdbcTemplate.update("""
                UPDATE books
                SET available_quantity = available_quantity + 1,
                    circulation_status = ?,
                    updated_at = ?
                WHERE isbn = ? AND available_quantity < total_quantity
                """, circulationStatus, updatedAt, isbn);
    }

    private String queryWithBorrower() {
        return """
                SELECT b.book_id, b.isbn, b.title, b.author, b.category, b.total_quantity, b.available_quantity,
                       b.shelf_status, b.circulation_status, b.created_at, b.updated_at,
                       (SELECT l.reader_id FROM loans l WHERE l.isbn = b.isbn AND l.status = 'BORROWED' ORDER BY l.due_date ASC LIMIT 1) AS current_borrower_reader_id,
                       (SELECT l.due_date FROM loans l WHERE l.isbn = b.isbn AND l.status = 'BORROWED' ORDER BY l.due_date ASC LIMIT 1) AS current_due_date
                FROM books b
                """;
    }
}
