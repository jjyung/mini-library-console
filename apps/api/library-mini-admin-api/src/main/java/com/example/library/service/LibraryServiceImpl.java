package com.example.library.service;

import com.example.library.domain.Book;
import com.example.library.domain.BookStatusEnum;
import com.example.library.domain.Loan;
import com.example.library.dto.BookDTO;
import com.example.library.dto.LoanDTO;
import com.example.library.dto.LoanReturnDTO;
import com.example.library.dto.PostBooksRequestDTO;
import com.example.library.dto.PostLoansRequestDTO;
import com.example.library.dto.PostLoansReturnsRequestDTO;
import com.example.library.error.BusinessCodeEnum;
import com.example.library.error.BusinessException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LibraryServiceImpl implements LibraryService {

    private static final int MIN_QUANTITY = 1;
    private static final int COUNT_INCREMENT = 1;
    private static final String ISBN_ALLOWED_PATTERN = "^[0-9X]+$";
    private static final Pattern ISBN_PATTERN = Pattern.compile(ISBN_ALLOWED_PATTERN);
    private static final Set<String> CATEGORIES = Set.of(
            "literature", "science", "technology", "history", "art", "philosophy", "business", "education");

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public LibraryServiceImpl(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public synchronized BookDTO createBook(PostBooksRequestDTO request) {
        validateBookRequest(request);
        String normalizedIsbn = normalizeIsbn(request.getIsbn());
        if (bookRepository.findByIsbn(normalizedIsbn).isPresent()) {
            throw new BusinessException(BusinessCodeEnum.DUPLICATE_ISBN, HttpStatus.CONFLICT, "ISBN 已存在");
        }

        Book book = new Book();
        book.setBookId(UUID.randomUUID());
        book.setTitle(request.getTitle().trim());
        book.setIsbn(normalizedIsbn);
        book.setAuthor(normalizeOptional(request.getAuthor()));
        book.setCategory(request.getCategory());
        book.setActive(Boolean.TRUE.equals(request.getActive()));
        book.setAvailableCount(request.getQuantity());
        book.setTotalCount(request.getQuantity());
        book.setStatus(book.isActive() ? BookStatusEnum.AVAILABLE : BookStatusEnum.INACTIVE);
        return toBookDto(bookRepository.save(book));
    }

    @Override
    public List<BookDTO> listBooks() {
        return bookRepository.findAll().stream().map(this::toBookDto).collect(Collectors.toList());
    }

    @Override
    public synchronized LoanDTO checkoutBook(PostLoansRequestDTO request) {
        if (request == null) {
            throw new BusinessException(BusinessCodeEnum.INVALID_INPUT, HttpStatus.BAD_REQUEST, "借閱欄位格式錯誤");
        }
        validateLoanRequest(request.getReaderId(), request.getIsbn());
        Book book = findBook(request.getIsbn());
        if (!book.isActive()) {
            throw new BusinessException(BusinessCodeEnum.BOOK_INACTIVE, HttpStatus.CONFLICT, "書籍未上架");
        }
        if (book.getAvailableCount() < MIN_QUANTITY) {
            throw new BusinessException(BusinessCodeEnum.NO_AVAILABLE_COPY, HttpStatus.CONFLICT, "書籍已無可借副本");
        }

        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setBookId(book.getBookId());
        loan.setIsbn(book.getIsbn());
        loan.setReaderId(request.getReaderId().trim());
        loan.setDueDate(parseDueDate(request.getDueDate()));
        loan.setLoanedAt(Instant.now());
        loanRepository.save(loan);
        book.setAvailableCount(book.getAvailableCount() - COUNT_INCREMENT);
        book.setStatus(book.getAvailableCount() == 0 ? BookStatusEnum.BORROWED : BookStatusEnum.AVAILABLE);
        bookRepository.save(book);
        return toLoanDto(loan);
    }

    @Override
    public synchronized LoanReturnDTO returnBook(PostLoansReturnsRequestDTO request) {
        if (request == null) {
            throw new BusinessException(BusinessCodeEnum.INVALID_INPUT, HttpStatus.BAD_REQUEST, "歸還欄位格式錯誤");
        }
        validateLoanRequest(request.getReaderId(), request.getIsbn());
        Book book = findBook(request.getIsbn());
        String readerId = normalizeOptional(request.getReaderId());
        Loan loan = (readerId == null
                ? loanRepository.findOldestActiveByBookId(book.getBookId())
                : loanRepository.findOldestActiveByBookIdAndReaderId(book.getBookId(), readerId))
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.NO_ACTIVE_LOAN, HttpStatus.NOT_FOUND, "沒有借閱中的紀錄"));

        if (book.getAvailableCount() >= book.getTotalCount()) {
            throw new BusinessException(BusinessCodeEnum.CONSISTENCY_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
                    "館藏數量狀態不一致");
        }
        Instant returnedAt = Instant.now();
        loan.setReturnedAt(returnedAt);
        loanRepository.save(loan);
        book.setAvailableCount(book.getAvailableCount() + COUNT_INCREMENT);
        book.setStatus(book.getAvailableCount() == book.getTotalCount()
                ? BookStatusEnum.AVAILABLE : BookStatusEnum.BORROWED);
        bookRepository.save(book);

        LoanReturnDTO result = new LoanReturnDTO();
        result.setLoanId(loan.getLoanId());
        result.setBook(toBookDto(book));
        result.setReturnedAt(returnedAt);
        return result;
    }

    private void validateBookRequest(PostBooksRequestDTO request) {
        if (request == null || isBlank(request.getTitle()) || isBlank(request.getIsbn())
                || isBlank(request.getCategory()) || request.getQuantity() == null
                || request.getQuantity() < MIN_QUANTITY || request.getActive() == null
                || !CATEGORIES.contains(request.getCategory())) {
            throw new BusinessException(BusinessCodeEnum.INVALID_INPUT, HttpStatus.BAD_REQUEST, "書籍欄位格式錯誤");
        }
        validateIsbn(request.getIsbn());
    }

    private void validateLoanRequest(String readerId, String isbn) {
        if (isBlank(isbn) || (readerId != null && readerId.isBlank())) {
            throw new BusinessException(BusinessCodeEnum.INVALID_INPUT, HttpStatus.BAD_REQUEST, "借閱欄位格式錯誤");
        }
        validateIsbn(isbn);
    }

    private void validateIsbn(String isbn) {
        String normalizedIsbn = normalizeIsbn(isbn);
        if ((normalizedIsbn.length() != 10 && normalizedIsbn.length() != 13)
                || !ISBN_PATTERN.matcher(normalizedIsbn).matches()) {
            throw new BusinessException(BusinessCodeEnum.INVALID_INPUT, HttpStatus.BAD_REQUEST, "ISBN 格式錯誤");
        }
    }

    private Book findBook(String isbn) {
        return bookRepository.findByIsbn(normalizeIsbn(isbn)).orElseThrow(() ->
                new BusinessException(BusinessCodeEnum.BOOK_NOT_FOUND, HttpStatus.NOT_FOUND, "找不到該 ISBN 的書籍"));
    }

    private LocalDate parseDueDate(String dueDate) {
        if (isBlank(dueDate)) {
            return null;
        }
        try {
            return LocalDate.parse(dueDate);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(BusinessCodeEnum.INVALID_INPUT, HttpStatus.BAD_REQUEST, "到期日格式錯誤", exception);
        }
    }

    private String normalizeIsbn(String isbn) {
        return isbn.replace("-", "").trim().toUpperCase();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BookDTO toBookDto(Book book) {
        BookDTO result = new BookDTO();
        result.setBookId(book.getBookId());
        result.setTitle(book.getTitle());
        result.setIsbn(book.getIsbn());
        result.setAuthor(book.getAuthor());
        result.setCategory(book.getCategory());
        result.setStatus(book.getStatus().getValue());
        result.setAvailableCount(book.getAvailableCount());
        result.setTotalCount(book.getTotalCount());
        result.setActive(book.isActive());
        return result;
    }

    private LoanDTO toLoanDto(Loan loan) {
        LoanDTO result = new LoanDTO();
        result.setLoanId(loan.getLoanId());
        result.setBookId(loan.getBookId());
        result.setIsbn(loan.getIsbn());
        result.setReaderId(loan.getReaderId());
        result.setDueDate(loan.getDueDate());
        result.setLoanedAt(loan.getLoanedAt());
        result.setReturned(loan.getReturnedAt() != null);
        return result;
    }
}
