package com.example.library.repository;

import com.example.library.domain.LoanRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LoanRepository {
    private static final RowMapper<LoanRecord> LOAN_ROW_MAPPER = (resultSet, rowNumber) -> {
        LoanRecord loanRecord = new LoanRecord();
        loanRecord.setLoanId(resultSet.getString("loan_id"));
        loanRecord.setIsbn(resultSet.getString("isbn"));
        loanRecord.setReaderId(resultSet.getString("reader_id"));
        loanRecord.setBorrowedAt(resultSet.getString("borrowed_at"));
        loanRecord.setDueDate(resultSet.getString("due_date"));
        loanRecord.setReturnedAt(resultSet.getString("returned_at"));
        loanRecord.setOverdueDays(resultSet.getInt("overdue_days"));
        loanRecord.setFineAmount(resultSet.getInt("fine_amount"));
        loanRecord.setStatus(resultSet.getString("status"));
        return loanRecord;
    };

    private final JdbcTemplate jdbcTemplate;

    public LoanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(LoanRecord loanRecord, String createdAt) {
        jdbcTemplate.update("""
                INSERT INTO loans(loan_id,isbn,reader_id,borrowed_at,due_date,returned_at,overdue_days,fine_amount,status,created_at,updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """,
                loanRecord.getLoanId(),
                loanRecord.getIsbn(),
                loanRecord.getReaderId(),
                loanRecord.getBorrowedAt(),
                loanRecord.getDueDate(),
                null,
                0,
                0,
                loanRecord.getStatus(),
                createdAt,
                createdAt);
    }

    public Optional<LoanRecord> findActiveLoan(String isbn, String readerId) {
        List<LoanRecord> records;
        if (readerId == null || readerId.isBlank()) {
            records = jdbcTemplate.query("SELECT * FROM loans WHERE isbn = ? AND status = 'BORROWED' ORDER BY due_date ASC LIMIT 1",
                    LOAN_ROW_MAPPER, isbn);
        } else {
            records = jdbcTemplate.query("SELECT * FROM loans WHERE isbn = ? AND reader_id = ? AND status = 'BORROWED' ORDER BY due_date ASC LIMIT 1",
                    LOAN_ROW_MAPPER, isbn, readerId);
        }
        return records.stream().findFirst();
    }

    public int closeLoan(String loanId, String returnedAt, int overdueDays, int fineAmount, String updatedAt) {
        return jdbcTemplate.update("""
                UPDATE loans
                SET returned_at = ?, overdue_days = ?, fine_amount = ?, status = 'RETURNED', updated_at = ?
                WHERE loan_id = ? AND status = 'BORROWED'
                """, returnedAt, overdueDays, fineAmount, updatedAt, loanId);
    }
}
