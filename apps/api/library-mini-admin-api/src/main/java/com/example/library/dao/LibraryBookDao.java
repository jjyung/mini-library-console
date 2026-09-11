package com.example.library.dao;

import com.example.library.domain.Book;
import com.example.library.domain.BookPage;
import com.example.library.domain.Loan;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface LibraryBookDao {
    BookPage listBooks(String query, int page, int pageSize);

    Optional<Book> findBookById(UUID bookId);

    Optional<Book> findBookByIdForUpdate(UUID bookId);

    Optional<Book> findBookByIsbn(String isbn);

    Optional<Loan> findActiveLoanByBookId(UUID bookId);

    Optional<Loan> findLoanByIdForUpdate(UUID bookId, UUID loanId);

    Book saveBook(Book book);

    Loan saveLoan(Loan loan);

    void updateBookInventory(UUID bookId, int availableCount, String status);

    void markLoanReturned(UUID bookId, UUID loanId, OffsetDateTime returnedAt);
}
