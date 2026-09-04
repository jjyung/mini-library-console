package com.example.library.dao;

import com.example.library.dao.model.LoanRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcLoanDao implements LoanDao {

    private static final String LOAN_COLUMNS = "loan_id, book_id, reader_id, borrowed_at, due_date, returned_at";
    private static final String ACTIVE_LOANS_SQL = "SELECT " + LOAN_COLUMNS + " FROM loans "
            + "WHERE book_id = ? AND returned_at IS NULL ORDER BY borrowed_at, loan_id FOR UPDATE";
    private static final String ACTIVE_LOANS_BY_READER_SQL = "SELECT " + LOAN_COLUMNS + " FROM loans "
            + "WHERE book_id = ? AND reader_id = ? AND returned_at IS NULL "
            + "ORDER BY borrowed_at, loan_id FOR UPDATE";
    private static final String INSERT_SQL = "INSERT INTO loans (loan_id, book_id, reader_id, borrowed_at, "
            + "due_date, returned_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String MARK_RETURNED_SQL = "UPDATE loans SET returned_at = ?, updated_at = ? "
            + "WHERE loan_id = ? AND returned_at IS NULL";

    private final JdbcTemplate jdbcTemplate;

    public JdbcLoanDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LoanRecord insert(LoanRecord loan) {
        Timestamp borrowedAt = toTimestamp(loan.getBorrowedAt());
        Timestamp returnedAt = loan.getReturnedAt() == null ? null : toTimestamp(loan.getReturnedAt());
        Timestamp now = Timestamp.valueOf(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        jdbcTemplate.update(INSERT_SQL, loan.getLoanId(), loan.getBookId(), loan.getReaderId(), borrowedAt,
                loan.getDueDate(), returnedAt, now, now);
        return loan;
    }

    @Override
    public List<LoanRecord> findActiveByBookId(UUID bookId, String readerId) {
        if (readerId == null) {
            return jdbcTemplate.query(ACTIVE_LOANS_SQL, this::mapLoan, bookId);
        }
        return jdbcTemplate.query(ACTIVE_LOANS_BY_READER_SQL, this::mapLoan, bookId, readerId);
    }

    @Override
    public int markReturned(UUID loanId, OffsetDateTime returnedAt) {
        Timestamp returnedTimestamp = toTimestamp(returnedAt);
        return jdbcTemplate.update(MARK_RETURNED_SQL, returnedTimestamp, returnedTimestamp, loanId);
    }

    private LoanRecord mapLoan(ResultSet resultSet, int rowNumber) throws SQLException {
        return new LoanRecord(
                resultSet.getObject("loan_id", UUID.class),
                resultSet.getObject("book_id", UUID.class),
                resultSet.getString("reader_id"),
                toOffsetDateTime(resultSet.getTimestamp("borrowed_at")),
                toLocalDate(resultSet),
                toNullableOffsetDateTime(resultSet.getTimestamp("returned_at")));
    }

    private LocalDate toLocalDate(ResultSet resultSet) throws SQLException {
        java.sql.Date dueDate = resultSet.getDate("due_date");
        return dueDate == null ? null : dueDate.toLocalDate();
    }

    private Timestamp toTimestamp(OffsetDateTime value) {
        return Timestamp.valueOf(value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
    }

    private OffsetDateTime toNullableOffsetDateTime(Timestamp value) {
        return value == null ? null : toOffsetDateTime(value);
    }

    private OffsetDateTime toOffsetDateTime(Timestamp value) {
        return value.toLocalDateTime().atOffset(ZoneOffset.UTC);
    }
}
