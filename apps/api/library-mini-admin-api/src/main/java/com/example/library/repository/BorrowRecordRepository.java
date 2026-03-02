package com.example.library.repository;

import com.example.library.borrowrecord.model.BorrowRecordEntity;
import com.example.library.borrowrecord.model.BorrowRecordStatusEnum;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BorrowRecordRepository {

    private static final RowMapper<BorrowRecordEntity> BORROW_RECORD_ROW_MAPPER = (resultSet, rowNumber) -> {
        BorrowRecordEntity borrowRecordEntity = new BorrowRecordEntity();
        borrowRecordEntity.setId(resultSet.getLong("id"));
        borrowRecordEntity.setBookId(resultSet.getLong("book_id"));
        borrowRecordEntity.setBorrowerName(resultSet.getString("borrower_name"));
        borrowRecordEntity.setStatus(BorrowRecordStatusEnum.valueOf(resultSet.getString("status")));
        borrowRecordEntity.setBorrowedAt(Instant.parse(resultSet.getString("borrowed_at")));
        String returnedAtRaw = resultSet.getString("returned_at");
        if (returnedAtRaw != null) {
            borrowRecordEntity.setReturnedAt(Instant.parse(returnedAtRaw));
        }
        borrowRecordEntity.setCreatedAt(Instant.parse(resultSet.getString("created_at")));
        borrowRecordEntity.setUpdatedAt(Instant.parse(resultSet.getString("updated_at")));
        return borrowRecordEntity;
    };

    private final JdbcTemplate jdbcTemplate;

    public BorrowRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BorrowRecordEntity createBorrowRecord(Long bookId, String borrowerName, Instant nowInstant) {
        String insertSql = """
                INSERT INTO borrow_records(book_id, borrower_name, status, borrowed_at, returned_at, created_at, updated_at)
                VALUES (?, ?, 'BORROWED', ?, NULL, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(insertSql, new String[]{"id"});
            preparedStatement.setLong(1, bookId);
            preparedStatement.setString(2, borrowerName);
            preparedStatement.setString(3, nowInstant.toString());
            preparedStatement.setString(4, nowInstant.toString());
            preparedStatement.setString(5, nowInstant.toString());
            return preparedStatement;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Failed to create borrow record id.");
        }
        return findBorrowRecordById(generatedId.longValue())
                .orElseThrow(() -> new IllegalStateException("Created borrow record not found."));
    }

    public Optional<BorrowRecordEntity> findBorrowRecordById(Long borrowRecordId) {
        String querySql = """
                SELECT id, book_id, borrower_name, status, borrowed_at, returned_at, created_at, updated_at
                FROM borrow_records
                WHERE id = ?
                """;
        List<BorrowRecordEntity> rows = jdbcTemplate.query(querySql, BORROW_RECORD_ROW_MAPPER, borrowRecordId);
        return rows.stream().findFirst();
    }

    public void markAsReturned(Long borrowRecordId, Instant returnTimeInstant) {
        String updateSql = """
                UPDATE borrow_records
                SET status = 'RETURNED', returned_at = ?, updated_at = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(updateSql, returnTimeInstant.toString(), returnTimeInstant.toString(), borrowRecordId);
    }
}
