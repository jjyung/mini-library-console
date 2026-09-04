package com.example.library.dao;

import com.example.library.dao.model.BookRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcBookDao implements BookDao {

    private static final String BOOK_COLUMNS = "book_id, title, isbn, author, category, "
            + "total_count, available_count, is_active";
    private static final String FIND_BY_ISBN_SQL = "SELECT " + BOOK_COLUMNS
            + " FROM books WHERE isbn = ?";
    private static final String FIND_BY_ISBN_FOR_UPDATE_SQL = FIND_BY_ISBN_SQL + " FOR UPDATE";
    private static final String INSERT_SQL = "INSERT INTO books (book_id, title, isbn, author, category, "
            + "total_count, available_count, is_active, created_at, updated_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DECREMENT_SQL = "UPDATE books SET available_count = available_count - 1, "
            + "updated_at = CURRENT_TIMESTAMP WHERE book_id = ? AND available_count > 0";
    private static final String INCREMENT_SQL = "UPDATE books SET available_count = available_count + 1, "
            + "updated_at = CURRENT_TIMESTAMP WHERE book_id = ? AND available_count < total_count";

    private final JdbcTemplate jdbcTemplate;

    public JdbcBookDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<BookRecord> listBooks() {
        return jdbcTemplate.query("SELECT " + BOOK_COLUMNS + " FROM books ORDER BY created_at, book_id",
                this::mapBook);
    }

    @Override
    public Optional<BookRecord> findByIsbn(String isbn) {
        return querySingle(FIND_BY_ISBN_SQL, isbn);
    }

    @Override
    public Optional<BookRecord> findByIsbnForUpdate(String isbn) {
        return querySingle(FIND_BY_ISBN_FOR_UPDATE_SQL, isbn);
    }

    @Override
    public BookRecord insert(BookRecord book) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Timestamp timestamp = Timestamp.valueOf(now.toLocalDateTime());
        jdbcTemplate.update(INSERT_SQL, book.getBookId(), book.getTitle(), book.getIsbn(), book.getAuthor(),
                book.getCategory(), book.getTotalCount(), book.getAvailableCount(), book.isActive(),
                timestamp, timestamp);
        return book;
    }

    @Override
    public int decrementAvailableCount(UUID bookId) {
        return jdbcTemplate.update(DECREMENT_SQL, bookId);
    }

    @Override
    public int incrementAvailableCount(UUID bookId) {
        return jdbcTemplate.update(INCREMENT_SQL, bookId);
    }

    private Optional<BookRecord> querySingle(String sql, String isbn) {
        return jdbcTemplate.query(sql, this::mapBook, isbn).stream().findFirst();
    }

    private BookRecord mapBook(ResultSet resultSet, int rowNumber) throws SQLException {
        return new BookRecord(
                resultSet.getObject("book_id", UUID.class),
                resultSet.getString("title"),
                resultSet.getString("isbn"),
                resultSet.getString("author"),
                resultSet.getString("category"),
                resultSet.getInt("total_count"),
                resultSet.getInt("available_count"),
                resultSet.getBoolean("is_active"));
    }
}
