package com.example.library.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Loan {

    private UUID loanId;
    private UUID bookId;
    private String isbn;
    private String readerId;
    private LocalDate dueDate;
    private Instant loanedAt;
    private Instant returnedAt;

    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Instant getLoanedAt() { return loanedAt; }
    public void setLoanedAt(Instant loanedAt) { this.loanedAt = loanedAt; }
    public Instant getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Instant returnedAt) { this.returnedAt = returnedAt; }
}
