package com.example.library.service;

import com.example.library.dao.BookDao;
import com.example.library.dao.LoanDao;
import com.example.library.dao.model.BookRecord;
import com.example.library.dao.model.BorrowCommand;
import com.example.library.dao.model.CreateBookCommand;
import com.example.library.dao.model.LoanRecord;
import com.example.library.dao.model.ReturnCommand;
import com.example.library.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class LibraryServiceImpl implements LibraryService {

    private static final String ISBN_FIELD = "isbn";
    private static final String READER_ID_FIELD = "readerId";
    private static final String REQUIRED_TEXT_REASON = "must contain at least one non-whitespace character";
    private static final String BOOK_UNAVAILABLE_MESSAGE = "The book is inactive or has no available copy.";
    private static final String DUPLICATE_ISBN_MESSAGE = "A book with this ISBN already exists.";
    private static final String NO_ACTIVE_LOAN_MESSAGE = "No active loan matches this ISBN.";
    private static final String AMBIGUOUS_LOAN_MESSAGE = "Multiple active loans match this ISBN; readerId is required.";
    private static final String BOOK_COUNT_UPDATE_MESSAGE = "Book availability could not be updated.";
    private static final String LOAN_UPDATE_MESSAGE = "Loan state could not be updated.";

    private final BookDao bookDao;
    private final LoanDao loanDao;

    public LibraryServiceImpl(BookDao bookDao, LoanDao loanDao) {
        this.bookDao = bookDao;
        this.loanDao = loanDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookRecord> listBooks() {
        return bookDao.listBooks();
    }

    @Override
    @Transactional
    public BookRecord createBook(CreateBookCommand command) {
        validateCreateCommand(command);
        if (bookDao.findByIsbn(command.getIsbn()).isPresent()) {
            throw new BusinessException(DUPLICATE_ISBN_MESSAGE, ISBN_FIELD, "ISBN must be unique");
        }

        UUID bookId = UUID.randomUUID();
        BookRecord book = new BookRecord(bookId, command.getTitle(), command.getIsbn(), command.getAuthor(),
                command.getCategory(), command.getQuantity(), command.getQuantity(), command.isActive());
        try {
            return bookDao.insert(book);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(DUPLICATE_ISBN_MESSAGE, exception);
        }
    }

    @Override
    @Transactional
    public LoanRecord borrow(BorrowCommand command) {
        validateText(command.getReaderId(), READER_ID_FIELD);
        validateText(command.getIsbn(), ISBN_FIELD);

        BookRecord book = bookDao.findByIsbnForUpdate(command.getIsbn())
                .orElseThrow(() -> new BusinessException(BOOK_UNAVAILABLE_MESSAGE, ISBN_FIELD, "book not found"));
        if (!book.isActive() || book.getAvailableCount() <= 0) {
            throw new BusinessException(BOOK_UNAVAILABLE_MESSAGE, ISBN_FIELD, "book is inactive or unavailable");
        }

        OffsetDateTime borrowedAt = OffsetDateTime.now(ZoneOffset.UTC);
        LoanRecord loan = new LoanRecord(UUID.randomUUID(), book.getBookId(), command.getReaderId(), borrowedAt,
                command.getDueDate(), null);
        loanDao.insert(loan);
        if (bookDao.decrementAvailableCount(book.getBookId()) != 1) {
            throw new IllegalStateException(BOOK_COUNT_UPDATE_MESSAGE);
        }
        return loan;
    }

    @Override
    @Transactional
    public LoanRecord returnLoan(ReturnCommand command) {
        validateText(command.getIsbn(), ISBN_FIELD);
        if (command.getReaderId() != null) {
            validateText(command.getReaderId(), READER_ID_FIELD);
        }

        BookRecord book = bookDao.findByIsbnForUpdate(command.getIsbn())
                .orElseThrow(() -> new BusinessException(NO_ACTIVE_LOAN_MESSAGE, ISBN_FIELD, "book not found"));
        List<LoanRecord> activeLoans = loanDao.findActiveByBookId(book.getBookId(), command.getReaderId());
        if (activeLoans.isEmpty()) {
            throw new BusinessException(NO_ACTIVE_LOAN_MESSAGE, ISBN_FIELD, "no active loan found");
        }
        if (command.getReaderId() == null && activeLoans.size() > 1) {
            throw new BusinessException(AMBIGUOUS_LOAN_MESSAGE, READER_ID_FIELD, "identify the borrowing reader");
        }

        LoanRecord loan = activeLoans.get(0);
        OffsetDateTime returnedAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (loanDao.markReturned(loan.getLoanId(), returnedAt) != 1) {
            throw new IllegalStateException(LOAN_UPDATE_MESSAGE);
        }
        if (bookDao.incrementAvailableCount(book.getBookId()) != 1) {
            throw new IllegalStateException(BOOK_COUNT_UPDATE_MESSAGE);
        }
        loan.setReturnedAt(returnedAt);
        return loan;
    }

    private void validateCreateCommand(CreateBookCommand command) {
        if (command == null) {
            throw new BusinessException("Book request is required.");
        }
        validateText(command.getTitle(), "title");
        validateText(command.getIsbn(), ISBN_FIELD);
        validateText(command.getCategory(), "category");
        if (command.getQuantity() < 1) {
            throw new BusinessException("Quantity must be at least one.", "quantity", "must be at least 1");
        }
    }

    private void validateText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Invalid " + field + ".", field, REQUIRED_TEXT_REASON);
        }
    }
}
