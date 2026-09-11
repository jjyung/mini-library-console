package com.example.library.application;

import com.example.library.dao.LibraryBookDao;
import com.example.library.domain.Book;
import com.example.library.domain.BookPage;
import com.example.library.domain.BookStatusEnum;
import com.example.library.domain.BookTransactionResult;
import com.example.library.domain.ErrorDetail;
import com.example.library.domain.Loan;
import com.example.library.domain.LoanStatusEnum;
import com.example.library.generated.dto.PostBookBorrowsRequestDTO;
import com.example.library.generated.dto.PostBookReturnsRequestDTO;
import com.example.library.generated.dto.PostBooksRequestDTO;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LibraryServiceImpl implements LibraryService {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String CLIENT_ERROR_CODE = "A0000";
    private static final String SYSTEM_ERROR_CODE = "B0000";
    private static final String CREATE_API_ID = "library-books-002";
    private static final String BORROW_API_ID = "library-books-003";
    private static final String RETURN_API_ID = "library-books-004";

    private final LibraryBookDao libraryBookDao;
    private final Map<String, StoredBookResult> createResults = new HashMap<>();
    private final Map<String, StoredBookTransactionResult> borrowResults = new HashMap<>();
    private final Map<String, StoredBookTransactionResult> returnResults = new HashMap<>();

    public LibraryServiceImpl(LibraryBookDao libraryBookDao) {
        this.libraryBookDao = libraryBookDao;
    }

    @Override
    @Transactional(readOnly = true)
    public BookPage listBooks(String query, Integer page, Integer pageSize) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        validatePagination(resolvedPage, resolvedPageSize);

        BookPage bookPage = libraryBookDao.listBooks(query, resolvedPage, resolvedPageSize);
        int total = bookPage.getTotal();
        int totalPages = (int) Math.ceil((double) total / resolvedPageSize);
        int lastAllowedPage = Math.max(DEFAULT_PAGE, totalPages);
        if (resolvedPage > lastAllowedPage) {
            throw badRequest(
                    "page 超出可用頁數",
                    List.of(new ErrorDetail("page", "請提供不超過可用頁數的 page"))
            );
        }
        return bookPage;
    }

    @Override
    @Transactional
    public synchronized Book createBook(String idempotencyKey, PostBooksRequestDTO request) {
        String normalizedKey = requireIdempotencyKey(idempotencyKey);
        String fingerprint = createFingerprint(request);
        String scopedKey = CREATE_API_ID + ":" + normalizedKey;
        StoredBookResult storedResult = createResults.get(scopedKey);
        if (storedResult != null) {
            ensureSameRequest(storedResult.getFingerprint(), fingerprint);
            return storedResult.getBook();
        }

        Book book = new Book();
        book.setBookId(UUID.randomUUID());
        book.setTitle(normalizeRequired(request.getTitle(), "title", "書名不可為空"));
        book.setIsbn(normalizeRequired(request.getIsbn(), "isbn", "ISBN 不可為空"));
        book.setAuthor(normalizeOptional(request.getAuthor(), "author"));
        if (request.getCategory() == null) {
            throw badRequest("分類不可為空", List.of(new ErrorDetail("category", "請選擇書籍分類")));
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw badRequest("數量必須大於零", List.of(new ErrorDetail("quantity", "請提供正整數數量")));
        }
        if (request.getIsActive() == null) {
            throw badRequest("上架狀態不可為空", List.of(new ErrorDetail("isActive", "請提供上架狀態")));
        }
        book.setCategory(request.getCategory().getValue());
        book.setTotalCount(request.getQuantity());
        book.setAvailableCount(request.getQuantity());
        book.setActive(request.getIsActive());
        book.setStatus(request.getIsActive() ? BookStatusEnum.AVAILABLE : BookStatusEnum.INACTIVE);

        try {
            Book savedBook = libraryBookDao.saveBook(book);
            createResults.put(scopedKey, new StoredBookResult(fingerprint, savedBook));
            return savedBook;
        } catch (DuplicateKeyException exception) {
            throw conflict("ISBN 已存在，無法重複新增", List.of(new ErrorDetail("isbn", "此 ISBN 已存在")));
        }
    }

    @Override
    @Transactional
    public synchronized BookTransactionResult borrowBook(
            UUID bookId,
            String idempotencyKey,
            PostBookBorrowsRequestDTO request
    ) {
        String normalizedKey = requireIdempotencyKey(idempotencyKey);
        String fingerprint = borrowFingerprint(bookId, request);
        String scopedKey = BORROW_API_ID + ":" + normalizedKey;
        StoredBookTransactionResult storedResult = borrowResults.get(scopedKey);
        if (storedResult != null) {
            ensureSameRequest(storedResult.getFingerprint(), fingerprint);
            return storedResult.getResult();
        }

        Book book = libraryBookDao.findBookByIdForUpdate(bookId)
                .orElseThrow(() -> notFound("找不到該書籍", List.of(new ErrorDetail("bookId", "找不到指定書目"))));
        if (!Boolean.TRUE.equals(book.getActive())) {
            throw conflict("該書籍未上架，無法借閱", List.of(new ErrorDetail("bookId", "書籍目前未上架")));
        }
        if (book.getAvailableCount() < 1) {
            throw conflict("該書籍已全數借出", List.of(new ErrorDetail("bookId", "目前沒有可借複本")));
        }

        String readerId = normalizeRequired(request.getReaderId(), "readerId", "借閱人不可為空");
        LocalDate dueDate = normalizeDueDate(request.getDueDate());
        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID());
        loan.setBookId(bookId);
        loan.setReaderId(readerId);
        loan.setBorrowedAt(OffsetDateTime.now(ZoneOffset.UTC));
        loan.setDueDate(dueDate);
        loan.setStatus(LoanStatusEnum.ACTIVE);

        int availableCount = book.getAvailableCount() - 1;
        BookStatusEnum nextStatus = availableCount == 0 ? BookStatusEnum.BORROWED : BookStatusEnum.AVAILABLE;
        Loan savedLoan = libraryBookDao.saveLoan(loan);
        libraryBookDao.updateBookInventory(bookId, availableCount, nextStatus.getDatabaseValue());
        Book savedBook = libraryBookDao.findBookById(bookId)
                .orElseThrow(() -> new IllegalStateException("Borrowed book cannot be reloaded"));
        BookTransactionResult result = new BookTransactionResult(savedBook, savedLoan);
        borrowResults.put(scopedKey, new StoredBookTransactionResult(fingerprint, result));
        return result;
    }

    @Override
    @Transactional
    public synchronized BookTransactionResult returnBook(
            UUID bookId,
            String idempotencyKey,
            PostBookReturnsRequestDTO request
    ) {
        String normalizedKey = requireIdempotencyKey(idempotencyKey);
        String fingerprint = returnFingerprint(bookId, request);
        String scopedKey = RETURN_API_ID + ":" + normalizedKey;
        StoredBookTransactionResult storedResult = returnResults.get(scopedKey);
        if (storedResult != null) {
            ensureSameRequest(storedResult.getFingerprint(), fingerprint);
            return storedResult.getResult();
        }

        Book book = libraryBookDao.findBookByIdForUpdate(bookId)
                .orElseThrow(() -> notFound("找不到該書籍", List.of(new ErrorDetail("bookId", "找不到指定書目"))));
        if (request.getLoanId() == null) {
            throw badRequest("loanId 不可為空", List.of(new ErrorDetail("loanId", "請提供唯一借閱紀錄識別值")));
        }
        Loan loan = libraryBookDao.findLoanByIdForUpdate(bookId, request.getLoanId())
                .orElseThrow(() -> notFound("找不到該借閱紀錄", List.of(new ErrorDetail("loanId", "找不到指定借閱紀錄"))));
        if (loan.getStatus() != LoanStatusEnum.ACTIVE) {
            throw conflict("該借閱紀錄已歸還", List.of(new ErrorDetail("loanId", "不可重複歸還")));
        }

        String readerId = normalizeOptional(request.getReaderId(), "readerId");
        if (readerId != null && !readerId.equals(loan.getReaderId())) {
            throw conflict("借閱人識別值不一致", List.of(new ErrorDetail("readerId", "請確認借閱人識別值")));
        }
        if (book.getAvailableCount() >= book.getTotalCount()) {
            throw new LibraryBusinessException(
                    SYSTEM_ERROR_CODE,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "館藏數量狀態不一致",
                    List.of(new ErrorDetail("availableCount", "可借數不可超過總數"))
            );
        }

        libraryBookDao.markLoanReturned(bookId, request.getLoanId(), OffsetDateTime.now(ZoneOffset.UTC));
        int availableCount = book.getAvailableCount() + 1;
        BookStatusEnum nextStatus = availableCount == book.getTotalCount()
                ? BookStatusEnum.AVAILABLE
                : BookStatusEnum.BORROWED;
        libraryBookDao.updateBookInventory(bookId, availableCount, nextStatus.getDatabaseValue());
        Book savedBook = libraryBookDao.findBookById(bookId)
                .orElseThrow(() -> new IllegalStateException("Returned book cannot be reloaded"));
        Loan savedLoan = libraryBookDao.findLoanByIdForUpdate(bookId, request.getLoanId())
                .orElseThrow(() -> new IllegalStateException("Returned loan cannot be reloaded"));
        BookTransactionResult result = new BookTransactionResult(savedBook, savedLoan);
        returnResults.put(scopedKey, new StoredBookTransactionResult(fingerprint, result));
        return result;
    }

    private void validatePagination(int page, int pageSize) {
        if (page < DEFAULT_PAGE) {
            throw badRequest("page 必須從 1 開始", List.of(new ErrorDetail("page", "page 最小值為 1")));
        }
        if (pageSize < DEFAULT_PAGE || pageSize > MAX_PAGE_SIZE) {
            throw badRequest(
                    "pageSize 超出範圍",
                    List.of(new ErrorDetail("pageSize", "pageSize 必須介於 1 與 100 之間"))
            );
        }
    }

    private String createFingerprint(PostBooksRequestDTO request) {
        return request.getTitle() + "|" + request.getIsbn() + "|"
                + normalizeOptional(request.getAuthor(), "author") + "|"
                + request.getCategory() + "|" + request.getQuantity() + "|" + request.getIsActive();
    }

    private String borrowFingerprint(UUID bookId, PostBookBorrowsRequestDTO request) {
        return bookId + "|" + request.getReaderId() + "|" + normalizeDueDate(request.getDueDate());
    }

    private String returnFingerprint(UUID bookId, PostBookReturnsRequestDTO request) {
        return bookId + "|" + request.getLoanId() + "|" + normalizeOptional(request.getReaderId(), "readerId");
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        return normalizeRequired(idempotencyKey, "Idempotency-Key", "Idempotency-Key 不可為空");
    }

    private String normalizeRequired(String value, String field, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw badRequest(message, List.of(new ErrorDetail(field, message)));
        }
        return value.trim();
    }

    private String normalizeOptional(JsonNullable<String> value, String field) {
        if (value == null || !value.isPresent()) {
            return null;
        }
        String normalizedValue = value.get() == null ? null : value.get().trim();
        if (normalizedValue != null && normalizedValue.isEmpty()) {
            throw badRequest(field + " 不可為空白", List.of(new ErrorDetail(field, field + " 不可為空白")));
        }
        return normalizedValue;
    }

    private LocalDate normalizeDueDate(JsonNullable<LocalDate> value) {
        return value != null && value.isPresent() ? value.get() : null;
    }

    private void ensureSameRequest(String storedFingerprint, String requestedFingerprint) {
        if (!storedFingerprint.equals(requestedFingerprint)) {
            throw conflict("Idempotency-Key 已用於不同請求", List.of(new ErrorDetail("Idempotency-Key", "請使用新的 key")));
        }
    }

    private LibraryBusinessException badRequest(String message, List<ErrorDetail> details) {
        return new LibraryBusinessException(CLIENT_ERROR_CODE, HttpStatus.BAD_REQUEST, message, details);
    }

    private LibraryBusinessException notFound(String message, List<ErrorDetail> details) {
        return new LibraryBusinessException(CLIENT_ERROR_CODE, HttpStatus.NOT_FOUND, message, details);
    }

    private LibraryBusinessException conflict(String message, List<ErrorDetail> details) {
        return new LibraryBusinessException(CLIENT_ERROR_CODE, HttpStatus.CONFLICT, message, details);
    }

    private static class StoredBookResult {
        private final String fingerprint;
        private final Book book;

        private StoredBookResult(String fingerprint, Book book) {
            this.fingerprint = fingerprint;
            this.book = book;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public Book getBook() {
            return book;
        }
    }

    private static class StoredBookTransactionResult {
        private final String fingerprint;
        private final BookTransactionResult result;

        private StoredBookTransactionResult(String fingerprint, BookTransactionResult result) {
            this.fingerprint = fingerprint;
            this.result = result;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public BookTransactionResult getResult() {
            return result;
        }
    }
}
