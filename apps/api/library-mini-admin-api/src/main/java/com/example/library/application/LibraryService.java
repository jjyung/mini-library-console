package com.example.library.application;

import com.example.library.domain.BookPage;
import com.example.library.domain.BookTransactionResult;
import com.example.library.domain.Book;
import com.example.library.domain.Loan;
import com.example.library.generated.dto.PostBookBorrowsRequestDTO;
import com.example.library.generated.dto.PostBookReturnsRequestDTO;
import com.example.library.generated.dto.PostBooksRequestDTO;

import java.util.UUID;

public interface LibraryService {
    BookPage listBooks(String query, Integer page, Integer pageSize);

    Book createBook(String idempotencyKey, PostBooksRequestDTO request);

    BookTransactionResult borrowBook(
            UUID bookId,
            String idempotencyKey,
            PostBookBorrowsRequestDTO request
    );

    BookTransactionResult returnBook(
            UUID bookId,
            String idempotencyKey,
            PostBookReturnsRequestDTO request
    );
}
