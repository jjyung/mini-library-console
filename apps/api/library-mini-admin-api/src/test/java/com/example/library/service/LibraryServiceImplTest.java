package com.example.library.service;

import com.example.library.application.LibraryBusinessException;
import com.example.library.application.LibraryServiceImpl;
import com.example.library.dao.LibraryBookDao;
import com.example.library.domain.Book;
import com.example.library.domain.BookPage;
import com.example.library.domain.BookStatusEnum;
import com.example.library.domain.BookTransactionResult;
import com.example.library.domain.Loan;
import com.example.library.domain.LoanStatusEnum;
import com.example.library.generated.dto.PostBookBorrowsRequestDTO;
import com.example.library.generated.dto.PostBookReturnsRequestDTO;
import com.example.library.generated.dto.PostBooksRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceImplTest {
    @Mock
    private LibraryBookDao libraryBookDao;

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Test
    void AC_UI_001_listsRequestedPageWithDeterministicMetadata() {
        Book firstBook = availableBook("First");
        when(libraryBookDao.listBooks(null, 2, 1)).thenReturn(new BookPage(List.of(firstBook), 3));

        BookPage result = libraryService.listBooks(null, 2, 1);

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getBooks()).containsExactly(firstBook);
        verify(libraryBookDao).listBooks(null, 2, 1);
    }

    @Test
    void AC_002_replaysCreateWithoutSecondMutationForSameIdempotencyKey() {
        PostBooksRequestDTO request = new PostBooksRequestDTO(
                "Clean Code",
                "978-0-13-235088-4",
                PostBooksRequestDTO.CategoryEnum.TECHNOLOGY,
                2,
                true
        );
        Book savedBook = availableBook("Clean Code");
        when(libraryBookDao.saveBook(any(Book.class))).thenReturn(savedBook);

        Book firstResult = libraryService.createBook("create-key", request);
        Book replayResult = libraryService.createBook("create-key", request);

        assertThat(replayResult).isSameAs(firstResult);
        verify(libraryBookDao, times(1)).saveBook(any(Book.class));
    }

    @Test
    void AC_003_borrowDecrementsOneCopyAndCreatesActiveLoan() {
        UUID bookId = UUID.randomUUID();
        Book book = availableBook("Clean Code");
        book.setBookId(bookId);
        book.setAvailableCount(1);
        book.setTotalCount(1);
        Loan savedLoan = activeLoan(bookId, "qa-reader-001");
        Book borrowedBook = availableBook("Clean Code");
        borrowedBook.setBookId(bookId);
        borrowedBook.setAvailableCount(0);
        borrowedBook.setTotalCount(1);
        borrowedBook.setStatus(BookStatusEnum.BORROWED);
        PostBookBorrowsRequestDTO request = new PostBookBorrowsRequestDTO("qa-reader-001");
        when(libraryBookDao.findBookByIdForUpdate(bookId)).thenReturn(java.util.Optional.of(book));
        when(libraryBookDao.saveLoan(any(Loan.class))).thenReturn(savedLoan);
        when(libraryBookDao.findBookById(bookId)).thenReturn(java.util.Optional.of(borrowedBook));

        BookTransactionResult result = libraryService.borrowBook(bookId, "borrow-key", request);

        assertThat(result.getBook().getAvailableCount()).isZero();
        assertThat(result.getLoan().getStatus()).isEqualTo(LoanStatusEnum.ACTIVE);
        verify(libraryBookDao).updateBookInventory(bookId, 0, "BORROWED");
    }

    @Test
    void AC_005_rejectsBorrowWhenNoCopyIsAvailable() {
        UUID bookId = UUID.randomUUID();
        Book book = availableBook("Clean Code");
        book.setBookId(bookId);
        book.setAvailableCount(0);
        book.setStatus(BookStatusEnum.BORROWED);
        when(libraryBookDao.findBookByIdForUpdate(bookId)).thenReturn(java.util.Optional.of(book));

        assertThatThrownBy(() -> libraryService.borrowBook(
                bookId,
                "borrow-key",
                new PostBookBorrowsRequestDTO("qa-reader-001")
        ))
                .isInstanceOf(LibraryBusinessException.class)
                .hasMessage("該書籍已全數借出");
    }

    @Test
    void AC_006_returnRestoresLastCopyAndClosesLoan() {
        UUID bookId = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();
        Book book = availableBook("Clean Code");
        book.setBookId(bookId);
        book.setAvailableCount(0);
        book.setTotalCount(1);
        book.setStatus(BookStatusEnum.BORROWED);
        Loan activeLoan = activeLoan(bookId, "qa-reader-001");
        activeLoan.setLoanId(loanId);
        Loan returnedLoan = activeLoan(bookId, "qa-reader-001");
        returnedLoan.setLoanId(loanId);
        returnedLoan.setStatus(LoanStatusEnum.RETURNED);
        Book returnedBook = availableBook("Clean Code");
        returnedBook.setBookId(bookId);
        when(libraryBookDao.findBookByIdForUpdate(bookId)).thenReturn(java.util.Optional.of(book));
        when(libraryBookDao.findLoanByIdForUpdate(bookId, loanId))
                .thenReturn(java.util.Optional.of(activeLoan), java.util.Optional.of(returnedLoan));
        when(libraryBookDao.findBookById(bookId)).thenReturn(java.util.Optional.of(returnedBook));

        BookTransactionResult result = libraryService.returnBook(
                bookId,
                "return-key",
                new PostBookReturnsRequestDTO(loanId)
        );

        assertThat(result.getLoan().getStatus()).isEqualTo(LoanStatusEnum.RETURNED);
        verify(libraryBookDao).markLoanReturned(any(UUID.class), any(UUID.class), any());
        verify(libraryBookDao).updateBookInventory(bookId, 1, "AVAILABLE");
    }

    @Test
    void AC_009_rejectsPageOutsideAvailableRangeWithBusinessCode() {
        when(libraryBookDao.listBooks(null, 3, 2)).thenReturn(new BookPage(List.of(availableBook("Only")), 1));

        assertThatThrownBy(() -> libraryService.listBooks(null, 3, 2))
                .isInstanceOf(LibraryBusinessException.class)
                .extracting("businessCode")
                .isEqualTo("A0000");
    }

    private Book availableBook(String title) {
        Book book = new Book();
        book.setBookId(UUID.randomUUID());
        book.setTitle(title);
        book.setIsbn(title + "-isbn");
        book.setCategory("technology");
        book.setTotalCount(1);
        book.setAvailableCount(1);
        book.setStatus(BookStatusEnum.AVAILABLE);
        book.setActive(true);
        return book;
    }

    private Loan activeLoan(UUID bookId, String readerId) {
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setBookId(bookId);
        loan.setReaderId(readerId);
        loan.setStatus(LoanStatusEnum.ACTIVE);
        loan.setBorrowedAt(java.time.OffsetDateTime.now());
        return loan;
    }
}
