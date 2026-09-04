package com.example.library.dao.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class LoanRecord {

    private UUID loanId;
    private UUID bookId;
    private String readerId;
    private OffsetDateTime borrowedAt;
    private LocalDate dueDate;
    private OffsetDateTime returnedAt;

    public LoanRecord() {
    }

    public LoanRecord(UUID loanId, UUID bookId, String readerId, OffsetDateTime borrowedAt,
                      LocalDate dueDate, OffsetDateTime returnedAt) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.readerId = readerId;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
    }

    public UUID getLoanId() {
        return loanId;
    }

    public void setLoanId(UUID loanId) {
        this.loanId = loanId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public OffsetDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(OffsetDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public OffsetDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(OffsetDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
}
