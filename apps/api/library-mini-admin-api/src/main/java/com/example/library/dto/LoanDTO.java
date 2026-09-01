package com.example.library.dto;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public class LoanDTO {
    private UUID loanId;
    private UUID bookId;
    private String isbn;
    private String readerId;
    private LocalDate dueDate;
    private Instant loanedAt;
    private boolean returned;

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
    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }
}
