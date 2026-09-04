package com.example.library.service;

import com.example.library.dao.BookDao;
import com.example.library.dao.LoanDao;
import com.example.library.dao.model.BookRecord;
import com.example.library.dao.model.CreateBookCommand;
import com.example.library.dao.model.LoanRecord;
import com.example.library.dao.model.BorrowCommand;
import com.example.library.dao.model.ReturnCommand;
import com.example.library.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceImplTest {

    private static final UUID BOOK_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LOAN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final OffsetDateTime BORROWED_AT = OffsetDateTime.of(2026, 9, 4, 8, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private BookDao bookDao;

    @Mock
    private LoanDao loanDao;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Test
    void listBooksDerivesInactiveAndBorrowedStatuses() {
        BookRecord inactiveBook = book(false, 2, 2, "inactive-isbn");
        BookRecord borrowedBook = book(true, 2, 0, "borrowed-isbn");
        when(bookDao.listBooks()).thenReturn(List.of(inactiveBook, borrowedBook));

        List<BookRecord> result = libraryService.listBooks();

        assertThat(result).extracting(BookRecord::getStatus)
                .containsExactly(BookRecord.Status.INACTIVE, BookRecord.Status.BORROWED);
    }

    @Test
    void createBookRejectsDuplicateIsbn() {
        when(bookDao.findByIsbn("duplicate-isbn")).thenReturn(Optional.of(book(true, 1, 1, "duplicate-isbn")));

        assertThatThrownBy(() -> libraryService.createBook(
                new CreateBookCommand("Title", "duplicate-isbn", "Author", "Category", 1, true)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A book with this ISBN already exists.");

        verify(bookDao, never()).insert(any(BookRecord.class));
    }

    @Test
    void borrowRejectsInactiveOrUnavailableBooks() {
        when(bookDao.findByIsbnForUpdate("isbn")).thenReturn(Optional.of(book(false, 1, 1, "isbn")));

        assertThatThrownBy(() -> libraryService.borrow(new BorrowCommand("reader", "isbn", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("The book is inactive or has no available copy.");

        when(bookDao.findByIsbnForUpdate("isbn")).thenReturn(Optional.of(book(true, 1, 0, "isbn")));

        assertThatThrownBy(() -> libraryService.borrow(new BorrowCommand("reader", "isbn", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("The book is inactive or has no available copy.");

        verify(loanDao, never()).insert(any(LoanRecord.class));
    }

    @Test
    void returnRejectsAmbiguousActiveLoansWithoutReader() {
        when(bookDao.findByIsbnForUpdate("isbn")).thenReturn(Optional.of(book(true, 2, 0, "isbn")));
        when(loanDao.findActiveByBookId(BOOK_ID, null)).thenReturn(List.of(
                loan("reader-a"), loan("reader-b")));

        assertThatThrownBy(() -> libraryService.returnLoan(new ReturnCommand("isbn", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Multiple active loans match this ISBN; readerId is required.");

        verify(loanDao, never()).markReturned(any(UUID.class), any(OffsetDateTime.class));
        verify(bookDao, never()).incrementAvailableCount(any(UUID.class));
    }

    @Test
    void returnClosesSelectedLoanAndRestoresAvailability() {
        LoanRecord activeLoan = loan("reader-a");
        when(bookDao.findByIsbnForUpdate("isbn")).thenReturn(Optional.of(book(true, 2, 1, "isbn")));
        when(loanDao.findActiveByBookId(BOOK_ID, "reader-a")).thenReturn(List.of(activeLoan));
        when(loanDao.markReturned(any(UUID.class), any(OffsetDateTime.class))).thenReturn(1);
        when(bookDao.incrementAvailableCount(BOOK_ID)).thenReturn(1);

        LoanRecord returnedLoan = libraryService.returnLoan(new ReturnCommand("isbn", "reader-a"));

        assertThat(returnedLoan.getReturnedAt()).isNotNull();
        verify(loanDao).markReturned(LOAN_ID, returnedLoan.getReturnedAt());
        verify(bookDao).incrementAvailableCount(BOOK_ID);
    }

    private BookRecord book(boolean active, int totalCount, int availableCount, String isbn) {
        return new BookRecord(BOOK_ID, "Title", isbn, "Author", "Category", totalCount, availableCount, active);
    }

    private LoanRecord loan(String readerId) {
        return new LoanRecord(LOAN_ID, BOOK_ID, readerId, BORROWED_AT, LocalDate.of(2026, 9, 30), null);
    }
}
