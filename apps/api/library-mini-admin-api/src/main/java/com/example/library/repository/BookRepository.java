package com.example.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.library.entity.BookEntity;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<BookEntity> BOOK_ROW_MAPPER = (resultSet, rowIndex) -> new BookEntity(
        resultSet.getLong("id"),
        resultSet.getString("title"),
        resultSet.getString("author"),
        resultSet.getInt("total_copies"),
        resultSet.getInt("available_copies"),
        resultSet.getString("status"),
        resultSet.getString("created_at"),
        resultSet.getString("updated_at"));

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insertBook(String title, String author, int totalCopies, String createdAt) {
        KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var preparedStatement = connection.prepareStatement(
                """
                INSERT INTO books (title, author, total_copies, available_copies, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, 'AVAILABLE', ?, ?)
                """,
                new String[] { "id" });
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setInt(3, totalCopies);
            preparedStatement.setInt(4, totalCopies);
            preparedStatement.setString(5, createdAt);
            preparedStatement.setString(6, createdAt);
            return preparedStatement;
        }, generatedKeyHolder);

        Number generatedKey = generatedKeyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("Cannot create book id.");
        }
        return generatedKey.longValue();
    }

    public Optional<BookEntity> findById(long bookId) {
        List<BookEntity> books = jdbcTemplate.query("SELECT * FROM books WHERE id = ?", BOOK_ROW_MAPPER, bookId);
        if (books.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(books.getFirst());
    }

    public List<BookEntity> listBooks(String status, String titleKeyword) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM books WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sqlBuilder.append(" AND status = ?");
            parameters.add(status);
        }
        if (titleKeyword != null && !titleKeyword.isBlank()) {
            sqlBuilder.append(" AND title LIKE ?");
            parameters.add("%" + titleKeyword.trim() + "%");
        }

        sqlBuilder.append(" ORDER BY id ASC");
        return jdbcTemplate.query(sqlBuilder.toString(), BOOK_ROW_MAPPER, parameters.toArray());
    }

    public int decreaseAvailableCopies(long bookId, String updatedAt) {
        return jdbcTemplate.update(
            """
            UPDATE books
            SET available_copies = available_copies - 1,
                status = CASE WHEN available_copies - 1 = 0 THEN 'CHECKED_OUT' ELSE 'AVAILABLE' END,
                updated_at = ?
            WHERE id = ? AND available_copies > 0
            """,
            updatedAt,
            bookId);
    }

    public int increaseAvailableCopies(long bookId, String updatedAt) {
        return jdbcTemplate.update(
            """
            UPDATE books
            SET available_copies = available_copies + 1,
                status = 'AVAILABLE',
                updated_at = ?
            WHERE id = ? AND available_copies < total_copies
            """,
            updatedAt,
            bookId);
    }
}
