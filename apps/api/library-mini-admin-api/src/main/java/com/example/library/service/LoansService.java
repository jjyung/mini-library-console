package com.example.library.service;

import com.example.library.api.dto.PostLoansRequestDTO;
import com.example.library.api.dto.PostLoansReturnsRequestDTO;
import com.example.library.domain.BookRecord;
import com.example.library.domain.LoanRecord;
import com.example.library.domain.LoanStatusEnum;
import com.example.library.exception.BusinessException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.IdSequenceRepository;
import com.example.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoansService {
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final IdSequenceRepository idSequenceRepository;
    private final BooksService booksService;

    @Value("${app.loan.default-period-days:14}")
    private int defaultPeriodDays;

    @Value("${app.loan.fine-per-day:5}")
    private int finePerDay;

    public LoansService(BookRepository bookRepository,
                        LoanRepository loanRepository,
                        IdSequenceRepository idSequenceRepository,
                        BooksService booksService) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.idSequenceRepository = idSequenceRepository;
        this.booksService = booksService;
    }

    @Transactional
    public Map<String, Object> borrow(PostLoansRequestDTO requestDTO) {
        BookRecord bookRecord = booksService.getBookOrThrow(requestDTO.getIsbn());
        if ("OFF_SHELF".equals(bookRecord.getShelfStatus())) {
            throw new BusinessException("A0005", "Book is not on shelf");
        }
        if (bookRecord.getAvailableQuantity() <= 0) {
            throw new BusinessException("A0004", "No available stock");
        }

        boolean defaultDueDateApplied = false;
        LocalDate dueDate;
        if (requestDTO.getDueDate() == null || requestDTO.getDueDate().isBlank()) {
            dueDate = LocalDate.now().plusDays(defaultPeriodDays);
            defaultDueDateApplied = true;
        } else {
            dueDate = parseLocalDateOrThrow(requestDTO.getDueDate());
            if (dueDate.isBefore(LocalDate.now())) {
                throw new BusinessException("A0003", "Missing required field or invalid format");
            }
        }

        String now = Instant.now().toString();
        String circulationStatus = booksService.resolveCirculationStatus(bookRecord.getShelfStatus(), bookRecord.getAvailableQuantity() - 1);
        int updatedRowCount = bookRepository.decreaseAvailable(bookRecord.getIsbn(), circulationStatus, now);
        if (updatedRowCount == 0) {
            throw new BusinessException("A0004", "No available stock");
        }

        LoanRecord loanRecord = new LoanRecord();
        loanRecord.setLoanId(generateLoanId());
        loanRecord.setIsbn(bookRecord.getIsbn());
        loanRecord.setReaderId(requestDTO.getReaderId());
        loanRecord.setBorrowedAt(now);
        loanRecord.setDueDate(dueDate.toString());
        loanRecord.setStatus(LoanStatusEnum.BORROWED.name());
        loanRepository.insert(loanRecord, now);

        BookRecord reloadedBook = booksService.getBookOrThrow(requestDTO.getIsbn());
        Map<String, Object> data = new HashMap<>();
        data.put("loan", Map.of(
                "loanId", loanRecord.getLoanId(),
                "isbn", loanRecord.getIsbn(),
                "readerId", loanRecord.getReaderId(),
                "borrowedAt", loanRecord.getBorrowedAt(),
                "dueDate", loanRecord.getDueDate(),
                "defaultDueDateApplied", defaultDueDateApplied,
                "status", loanRecord.getStatus()
        ));
        data.put("bookInventory", Map.of(
                "isbn", reloadedBook.getIsbn(),
                "totalQuantity", reloadedBook.getTotalQuantity(),
                "availableQuantity", reloadedBook.getAvailableQuantity(),
                "circulationStatus", reloadedBook.getCirculationStatus()
        ));
        return data;
    }

    @Transactional
    public ReturnResult returnBook(PostLoansReturnsRequestDTO requestDTO) {
        BookRecord bookRecord = booksService.getBookOrThrow(requestDTO.getIsbn());
        LoanRecord activeLoan = loanRepository.findActiveLoan(requestDTO.getIsbn(), requestDTO.getReaderId())
                .orElseThrow(() -> new BusinessException("A0006", "No active borrow record"));

        String returnTimestamp = requestDTO.getReturnedAt();
        if (returnTimestamp == null || returnTimestamp.isBlank()) {
            returnTimestamp = Instant.now().toString();
        } else {
            parseOffsetDateTimeOrThrow(returnTimestamp);
        }

        LocalDate dueDate = parseLocalDateOrThrow(activeLoan.getDueDate());
        LocalDate returnDate = OffsetDateTime.parse(returnTimestamp).toLocalDate();
        int overdueDays = (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        int fineAmount = overdueDays * finePerDay;
        String code = overdueDays > 0 ? "A0007" : "00000";
        String message = overdueDays > 0 ? "Overdue return warning" : "Success";

        String updatedAt = Instant.now().toString();
        int closeRowCount = loanRepository.closeLoan(activeLoan.getLoanId(), returnTimestamp, overdueDays, fineAmount, updatedAt);
        if (closeRowCount == 0) {
            throw new BusinessException("A0006", "No active borrow record");
        }

        String circulationStatus = booksService.resolveCirculationStatus(bookRecord.getShelfStatus(), bookRecord.getAvailableQuantity() + 1);
        bookRepository.increaseAvailable(bookRecord.getIsbn(), circulationStatus, updatedAt);

        BookRecord reloadedBook = booksService.getBookOrThrow(requestDTO.getIsbn());
        Map<String, Object> data = new HashMap<>();
        data.put("returnRecord", Map.of(
                "loanId", activeLoan.getLoanId(),
                "isbn", activeLoan.getIsbn(),
                "readerId", activeLoan.getReaderId(),
                "returnedAt", returnTimestamp,
                "overdueDays", overdueDays,
                "fineAmount", fineAmount,
                "currency", "TWD",
                "status", LoanStatusEnum.RETURNED.name()
        ));
        data.put("bookInventory", Map.of(
                "isbn", reloadedBook.getIsbn(),
                "totalQuantity", reloadedBook.getTotalQuantity(),
                "availableQuantity", reloadedBook.getAvailableQuantity(),
                "circulationStatus", reloadedBook.getCirculationStatus()
        ));
        return new ReturnResult(code, message, data);
    }

    public static class ReturnResult {
        private final String code;
        private final String message;
        private final Map<String, Object> data;

        public ReturnResult(String code, String message, Map<String, Object> data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Map<String, Object> getData() { return data; }
    }

    private String generateLoanId() {
        long value = idSequenceRepository.nextValue("LOAN");
        return String.format("LN%08d", value);
    }

    private LocalDate parseLocalDateOrThrow(String dateText) {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException dateTimeParseException) {
            throw new BusinessException("A0003", "Missing required field or invalid format");
        }
    }

    private OffsetDateTime parseOffsetDateTimeOrThrow(String dateTimeText) {
        try {
            return OffsetDateTime.parse(dateTimeText).withOffsetSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException dateTimeParseException) {
            throw new BusinessException("A0003", "Missing required field or invalid format");
        }
    }
}
