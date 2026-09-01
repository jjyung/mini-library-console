package com.example.library.dto;

import java.time.Instant;
import java.util.UUID;

public class LoanReturnDTO {
    private UUID loanId;
    private BookDTO book;
    private Instant returnedAt;

    public UUID getLoanId() { return loanId; }
    public void setLoanId(UUID loanId) { this.loanId = loanId; }
    public BookDTO getBook() { return book; }
    public void setBook(BookDTO book) { this.book = book; }
    public Instant getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Instant returnedAt) { this.returnedAt = returnedAt; }
}
