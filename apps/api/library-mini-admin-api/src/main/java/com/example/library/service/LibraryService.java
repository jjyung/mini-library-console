package com.example.library.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.library.dto.BookDTO;
import com.example.library.dto.BorrowRecordDTO;
import com.example.library.dto.GetBooksRequestDTO;
import com.example.library.dto.GetBorrowRecordsRequestDTO;
import com.example.library.dto.PatchBorrowRecordsRequestDTO;
import com.example.library.dto.PostBooksRequestDTO;
import com.example.library.dto.PostBorrowRecordsRequestDTO;
import com.example.library.entity.BookEntity;
import com.example.library.entity.BorrowRecordEntity;
import com.example.library.exception.BusinessException;
import com.example.library.exception.SystemOperationException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public LibraryService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Transactional
    public BookDTO createBook(PostBooksRequestDTO requestDto) {
        String currentTime = Instant.now().toString();
        try {
            long bookId = bookRepository.insertBook(
                requestDto.getTitle().trim(),
                requestDto.getAuthor().trim(),
                requestDto.getTotalCopies(),
                currentTime);
            BookEntity bookEntity = bookRepository.findById(bookId)
                .orElseThrow(() -> new SystemOperationException("Book creation lookup failed."));
            return mapBook(bookEntity);
        } catch (RuntimeException runtimeException) {
            throw new SystemOperationException("Create book failed.", runtimeException);
        }
    }

    @Transactional(readOnly = true)
    public List<BookDTO> listBooks(GetBooksRequestDTO requestDto) {
        String status = normalizeBookStatus(requestDto.getStatus());
        return bookRepository.listBooks(status, requestDto.getTitleKeyword()).stream()
            .map(this::mapBook)
            .toList();
    }

    @Transactional
    public BorrowRecordDTO borrowBook(PostBorrowRecordsRequestDTO requestDto) {
        BookEntity existingBook = bookRepository.findById(requestDto.getBookId())
            .orElseThrow(() -> new BusinessException("A0000", "A0003", "Book does not exist."));

        if (existingBook.getAvailableCopies() <= 0) {
            throw new BusinessException("A0000", "A0004", "Available copies are not enough.");
        }

        String currentTime = Instant.now().toString();
        int updatedRows = bookRepository.decreaseAvailableCopies(requestDto.getBookId(), currentTime);
        if (updatedRows == 0) {
            throw new BusinessException("A0000", "A0004", "Available copies are not enough.");
        }

        long borrowRecordId;
        try {
            borrowRecordId = borrowRecordRepository.insertBorrowRecord(
                requestDto.getBookId(),
                requestDto.getBorrowerName().trim(),
                currentTime);
        } catch (RuntimeException runtimeException) {
            throw new SystemOperationException("Borrow transaction failed.", runtimeException);
        }

        BorrowRecordEntity borrowRecordEntity = borrowRecordRepository.findById(borrowRecordId)
            .orElseThrow(() -> new SystemOperationException("Borrow record lookup failed."));
        return mapBorrowRecord(borrowRecordEntity);
    }

    @Transactional
    public BorrowRecordDTO returnBook(long borrowRecordId, PatchBorrowRecordsRequestDTO requestDto) {
        BorrowRecordEntity existingRecord = borrowRecordRepository.findById(borrowRecordId)
            .orElseThrow(() -> new BusinessException("A0000", "A0005", "Borrow record does not exist."));

        if ("RETURNED".equals(existingRecord.getStatus())) {
            throw new BusinessException("A0000", "A0006", "Borrow record was already returned.");
        }

        String currentTime = Instant.now().toString();
        int updatedBorrowRecordRows = borrowRecordRepository.markReturned(borrowRecordId, currentTime);
        if (updatedBorrowRecordRows == 0) {
            throw new BusinessException("A0000", "A0006", "Borrow record was already returned.");
        }

        int updatedBookRows = bookRepository.increaseAvailableCopies(existingRecord.getBookId(), currentTime);
        if (updatedBookRows == 0) {
            throw new SystemOperationException("Book stock update failed.");
        }

        BorrowRecordEntity returnedRecord = borrowRecordRepository.findById(borrowRecordId)
            .orElseThrow(() -> new SystemOperationException("Returned record lookup failed."));
        return mapBorrowRecord(returnedRecord);
    }

    @Transactional(readOnly = true)
    public List<BorrowRecordDTO> listBorrowRecords(GetBorrowRecordsRequestDTO requestDto) {
        String status = normalizeBorrowRecordStatus(requestDto.getStatus());
        return borrowRecordRepository.listBorrowRecords(status, requestDto.getBookId(), requestDto.getBorrowerName()).stream()
            .map(this::mapBorrowRecord)
            .toList();
    }

    private String normalizeBookStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        if (!"AVAILABLE".equals(status) && !"CHECKED_OUT".equals(status)) {
            throw new IllegalArgumentException("Invalid book status.");
        }
        return status;
    }

    private String normalizeBorrowRecordStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        if (!"BORROWED".equals(status) && !"RETURNED".equals(status)) {
            throw new IllegalArgumentException("Invalid borrow record status.");
        }
        return status;
    }

    private BookDTO mapBook(BookEntity bookEntity) {
        return new BookDTO(
            bookEntity.getId(),
            bookEntity.getTitle(),
            bookEntity.getAuthor(),
            bookEntity.getTotalCopies(),
            bookEntity.getAvailableCopies(),
            bookEntity.getStatus(),
            bookEntity.getCreatedAt(),
            bookEntity.getUpdatedAt());
    }

    private BorrowRecordDTO mapBorrowRecord(BorrowRecordEntity borrowRecordEntity) {
        return new BorrowRecordDTO(
            borrowRecordEntity.getId(),
            borrowRecordEntity.getBookId(),
            borrowRecordEntity.getBookTitle(),
            borrowRecordEntity.getBorrowerName(),
            borrowRecordEntity.getBorrowedAt(),
            borrowRecordEntity.getReturnedAt(),
            borrowRecordEntity.getStatus());
    }
}
