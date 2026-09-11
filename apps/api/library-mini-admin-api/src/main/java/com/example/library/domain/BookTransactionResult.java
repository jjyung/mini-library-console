package com.example.library.domain;

public class BookTransactionResult {
    private Book book;
    private Loan loan;

    public BookTransactionResult(Book book, Loan loan) {
        this.book = book;
        this.loan = loan;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }
}
