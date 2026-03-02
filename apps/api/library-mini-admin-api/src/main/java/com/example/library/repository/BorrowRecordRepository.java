package com.example.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.library.entity.BorrowRecordEntity;

@Repository
public class BorrowRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<BorrowRecordEntity> BORROW_RECORD_ROW_MAPPER = (resultSet, rowIndex) ->
        new BorrowRecordEntity(
            resultSet.getLong("id"),
            resultSet.getLong("book_id"),
            resultSet.getString("book_title"),
            resultSet.getString("borrower_name"),
            resultSet.getString("borrowed_at"),
            resultSet.getString("returned_at"),
            resultSet.getString("status"));

    public BorrowRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insertBorrowRecord(long bookId, String borrowerName, String createdAt) {
        KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var preparedStatement = connection.prepareStatement(
                """
                INSERT INTO borrow_records (book_id, borrower_name, borrowed_at, returned_at, status, created_at, updated_at)
                VALUES (?, ?, ?, NULL, 'BORROWED', ?, ?)
                """,
                new String[] { "id" });
            preparedStatement.setLong(1, bookId);
            preparedStatement.setString(2, borrowerName);
            preparedStatement.setString(3, createdAt);
            preparedStatement.setString(4, createdAt);
            preparedStatement.setString(5, createdAt);
            return preparedStatement;
        }, generatedKeyHolder);

        Number generatedKey = generatedKeyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("Cannot create borrow record id.");
        }
        return generatedKey.longValue();
    }

    public Optional<BorrowRecordEntity> findById(long borrowRecordId) {
        List<BorrowRecordEntity> borrowRecords = jdbcTemplate.query(
            """
            SELECT br.id,
                   br.book_id,
                   b.title AS book_title,
                   br.borrower_name,
                   br.borrowed_at,
                   br.returned_at,
                   br.status
            FROM borrow_records br
            INNER JOIN books b ON b.id = br.book_id
            WHERE br.id = ?
            """,
            BORROW_RECORD_ROW_MAPPER,
            borrowRecordId);

        if (borrowRecords.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(borrowRecords.getFirst());
    }

    public int markReturned(long borrowRecordId, String returnedAt) {
        return jdbcTemplate.update(
            """
            UPDATE borrow_records
            SET returned_at = ?,
                status = 'RETURNED',
                updated_at = ?
            WHERE id = ? AND status = 'BORROWED'
            """,
            returnedAt,
            returnedAt,
            borrowRecordId);
    }

    public List<BorrowRecordEntity> listBorrowRecords(String status, Long bookId, String borrowerName) {
        StringBuilder sqlBuilder = new StringBuilder(
            """
            SELECT br.id,
                   br.book_id,
                   b.title AS book_title,
                   br.borrower_name,
                   br.borrowed_at,
                   br.returned_at,
                   br.status
            FROM borrow_records br
            INNER JOIN books b ON b.id = br.book_id
            WHERE 1 = 1
            """);
        List<Object> parameters = new ArrayList<>();

        if (status != null && !status.isBlank()) {
            sqlBuilder.append(" AND br.status = ?");
            parameters.add(status);
        }
        if (bookId != null) {
            sqlBuilder.append(" AND br.book_id = ?");
            parameters.add(bookId);
        }
        if (borrowerName != null && !borrowerName.isBlank()) {
            sqlBuilder.append(" AND br.borrower_name LIKE ?");
            parameters.add("%" + borrowerName.trim() + "%");
        }

        sqlBuilder.append(" ORDER BY br.id DESC");
        return jdbcTemplate.query(sqlBuilder.toString(), BORROW_RECORD_ROW_MAPPER, parameters.toArray());
    }
}
