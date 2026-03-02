package com.example.library.service;

import com.example.library.api.dto.PostBooksRequestDTO;
import com.example.library.domain.BookRecord;
import com.example.library.domain.CirculationStatusEnum;
import com.example.library.domain.ShelfStatusEnum;
import com.example.library.exception.BusinessException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.IdSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BooksService {
    private final BookRepository bookRepository;
    private final IdSequenceRepository idSequenceRepository;

    public BooksService(BookRepository bookRepository, IdSequenceRepository idSequenceRepository) {
        this.bookRepository = bookRepository;
        this.idSequenceRepository = idSequenceRepository;
    }

    @Transactional
    public Map<String, Object> createBook(PostBooksRequestDTO requestDTO) {
        validateShelfStatus(requestDTO.getShelfStatus());
        if (bookRepository.existsByIsbn(requestDTO.getIsbn())) {
            throw new BusinessException("A0002", "ISBN already exists");
        }
        String createdAt = Instant.now().toString();
        BookRecord bookRecord = new BookRecord();
        bookRecord.setBookId(generateBookId());
        bookRecord.setIsbn(requestDTO.getIsbn());
        bookRecord.setTitle(requestDTO.getTitle());
        bookRecord.setAuthor(requestDTO.getAuthor());
        bookRecord.setCategory(requestDTO.getCategory());
        bookRecord.setTotalQuantity(requestDTO.getQuantity());
        bookRecord.setAvailableQuantity(requestDTO.getQuantity());
        bookRecord.setShelfStatus(requestDTO.getShelfStatus());
        bookRecord.setCirculationStatus(resolveCirculationStatus(requestDTO.getShelfStatus(), requestDTO.getQuantity()));
        bookRecord.setCreatedAt(createdAt);
        bookRecord.setUpdatedAt(createdAt);
        bookRepository.insert(bookRecord);
        return Map.of("book", bookRecord);
    }

    public Map<String, Object> listBooks(String shelfStatus) {
        if (shelfStatus != null && !shelfStatus.isBlank()) {
            validateShelfStatus(shelfStatus);
        }
        List<BookRecord> books = bookRepository.listBooks(shelfStatus);
        return Map.of("books", books);
    }

    public Map<String, Object> searchBooks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException("A0003", "Missing required field or invalid format");
        }
        List<BookRecord> books = bookRepository.searchBooks(keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("books", books);
        return data;
    }

    public BookRecord getBookOrThrow(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BusinessException("A0001", "Book not found"));
    }

    public String resolveCirculationStatus(String shelfStatus, int availableQuantity) {
        if (ShelfStatusEnum.OFF_SHELF.name().equals(shelfStatus)) {
            return CirculationStatusEnum.OFF_SHELF.name();
        }
        if (availableQuantity <= 0) {
            return CirculationStatusEnum.BORROWED_OUT.name();
        }
        return CirculationStatusEnum.AVAILABLE.name();
    }

    private void validateShelfStatus(String shelfStatus) {
        try {
            ShelfStatusEnum.valueOf(shelfStatus);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new BusinessException("A0003", "Missing required field or invalid format");
        }
    }

    private String generateBookId() {
        long value = idSequenceRepository.nextValue("BOOK");
        return String.format("BK%08d", value);
    }
}
