package com.example.library.controller;

import com.example.library.domain.Book;
import com.example.library.domain.BookPage;
import com.example.library.domain.BookStatusEnum;
import com.example.library.domain.Loan;
import com.example.library.domain.LoanStatusEnum;
import com.example.library.generated.dto.BookDetailResponseDTO;
import com.example.library.generated.dto.BookSummaryResponseDTO;
import com.example.library.generated.dto.GetBooksDataResponseDTO;
import com.example.library.generated.dto.LoanSummaryResponseDTO;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LibraryResponseMapper {
    public GetBooksDataResponseDTO toBookPageResponse(BookPage bookPage, int page, int pageSize) {
        int total = bookPage.getTotal();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        List<BookSummaryResponseDTO> items = bookPage.getBooks().stream()
                .map(book -> toBookSummary(book, book.getActiveLoan()))
                .toList();
        return new GetBooksDataResponseDTO(items, page, pageSize, totalPages, total);
    }

    public BookSummaryResponseDTO toBookSummary(Book book, Loan activeLoan) {
        BookSummaryResponseDTO response = new BookSummaryResponseDTO(
                book.getBookId(),
                book.getTitle(),
                book.getIsbn(),
                book.getCategory(),
                toSummaryStatus(book.getStatus()),
                book.getAvailableCount(),
                book.getTotalCount()
        );
        response.setAuthor(JsonNullable.of(book.getAuthor()));
        response.setActiveLoan(JsonNullable.of(toLoanSummary(activeLoan)));
        return response;
    }

    public BookDetailResponseDTO toBookDetail(Book book, Loan activeLoan) {
        BookDetailResponseDTO response = new BookDetailResponseDTO(
                book.getBookId(),
                book.getTitle(),
                book.getIsbn(),
                book.getCategory(),
                toDetailStatus(book.getStatus()),
                book.getAvailableCount(),
                book.getTotalCount()
        );
        response.setAuthor(JsonNullable.of(book.getAuthor()));
        response.setActiveLoan(JsonNullable.of(toLoanSummary(activeLoan)));
        return response;
    }

    public LoanSummaryResponseDTO toLoanSummary(Loan loan) {
        if (loan == null) {
            return null;
        }
        LoanSummaryResponseDTO response = new LoanSummaryResponseDTO(
                loan.getLoanId(),
                loan.getBookId(),
                loan.getReaderId(),
                loan.getBorrowedAt(),
                LoanSummaryResponseDTO.StatusEnum.valueOf(loan.getStatus().name())
        );
        response.setDueDate(JsonNullable.of(loan.getDueDate()));
        response.setReturnedAt(JsonNullable.of(loan.getReturnedAt()));
        return response;
    }

    private BookSummaryResponseDTO.StatusEnum toSummaryStatus(BookStatusEnum status) {
        return BookSummaryResponseDTO.StatusEnum.valueOf(status.name());
    }

    private BookDetailResponseDTO.StatusEnum toDetailStatus(BookStatusEnum status) {
        return BookDetailResponseDTO.StatusEnum.valueOf(status.name());
    }
}
