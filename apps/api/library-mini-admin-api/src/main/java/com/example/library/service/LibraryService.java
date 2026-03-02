package com.example.library.service;

import com.example.library.book.dto.BookDTO;
import com.example.library.book.dto.GetBooksResponseBodyDTO;
import com.example.library.book.dto.PostBooksRequestDTO;
import com.example.library.book.dto.PostBooksResponseBodyDTO;
import com.example.library.book.model.BookEntity;
import com.example.library.borrowrecord.dto.BorrowRecordDTO;
import com.example.library.borrowrecord.dto.PatchBorrowRecordsResponseBodyDTO;
import com.example.library.borrowrecord.dto.PostBorrowRecordsRequestDTO;
import com.example.library.borrowrecord.dto.PostBorrowRecordsResponseBodyDTO;
import com.example.library.borrowrecord.model.BorrowRecordEntity;
import com.example.library.borrowrecord.model.BorrowRecordStatusEnum;
import com.example.library.common.BusinessCodeEnum;
import com.example.library.common.exception.BusinessException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public LibraryService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Transactional
    public PostBooksResponseBodyDTO createBook(PostBooksRequestDTO postBooksRequestDTO) {
        Instant nowInstant = Instant.now();
        BookEntity bookEntity = bookRepository.createBook(
                postBooksRequestDTO.getTitle().trim(),
                postBooksRequestDTO.getAuthor().trim(),
                postBooksRequestDTO.getTotalQuantity(),
                nowInstant
        );

        BookDTO bookDTO = bookRepository.findBookViewById(bookEntity.getId())
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.SYSTEM_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Created book not found."
                ));

        PostBooksResponseBodyDTO postBooksResponseBodyDTO = new PostBooksResponseBodyDTO();
        postBooksResponseBodyDTO.setBook(bookDTO);
        return postBooksResponseBodyDTO;
    }

    public GetBooksResponseBodyDTO getBooks() {
        List<BookDTO> books = bookRepository.listBooksWithAvailability();
        GetBooksResponseBodyDTO getBooksResponseBodyDTO = new GetBooksResponseBodyDTO();
        getBooksResponseBodyDTO.setBooks(books);
        return getBooksResponseBodyDTO;
    }

    @Transactional
    public PostBorrowRecordsResponseBodyDTO borrowBook(PostBorrowRecordsRequestDTO postBorrowRecordsRequestDTO) {
        BookDTO bookDTO = bookRepository.findBookViewById(postBorrowRecordsRequestDTO.getBookId())
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.CLIENT_ERROR,
                        HttpStatus.BAD_REQUEST,
                        "Book not found."
                ));

        if (bookDTO.getAvailableQuantity() <= 0) {
            throw new BusinessException(
                    BusinessCodeEnum.CLIENT_ERROR,
                    HttpStatus.CONFLICT,
                    "No available quantity for this book."
            );
        }

        Instant nowInstant = Instant.now();
        BorrowRecordEntity borrowRecordEntity = borrowRecordRepository.createBorrowRecord(
                postBorrowRecordsRequestDTO.getBookId(),
                postBorrowRecordsRequestDTO.getBorrowerName().trim(),
                nowInstant
        );

        BorrowRecordDTO borrowRecordDTO = mapBorrowRecordToDTO(borrowRecordEntity);
        BookDTO updatedBookDTO = bookRepository.findBookViewById(postBorrowRecordsRequestDTO.getBookId())
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.SYSTEM_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Updated book not found after borrow."
                ));

        PostBorrowRecordsResponseBodyDTO responseBodyDTO = new PostBorrowRecordsResponseBodyDTO();
        responseBodyDTO.setBorrowRecord(borrowRecordDTO);
        responseBodyDTO.setBook(updatedBookDTO);
        return responseBodyDTO;
    }

    @Transactional
    public PatchBorrowRecordsResponseBodyDTO returnBorrowRecord(Long borrowRecordId) {
        BorrowRecordEntity borrowRecordEntity = borrowRecordRepository.findBorrowRecordById(borrowRecordId)
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.CLIENT_ERROR,
                        HttpStatus.NOT_FOUND,
                        "Borrow record not found."
                ));

        if (BorrowRecordStatusEnum.RETURNED.equals(borrowRecordEntity.getStatus())) {
            throw new BusinessException(
                    BusinessCodeEnum.CLIENT_ERROR,
                    HttpStatus.CONFLICT,
                    "Borrow record already returned."
            );
        }

        Instant returnTimeInstant = Instant.now();
        borrowRecordRepository.markAsReturned(borrowRecordId, returnTimeInstant);

        BorrowRecordEntity updatedBorrowRecordEntity = borrowRecordRepository.findBorrowRecordById(borrowRecordId)
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.SYSTEM_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Updated borrow record not found."
                ));

        BookDTO updatedBookDTO = bookRepository.findBookViewById(updatedBorrowRecordEntity.getBookId())
                .orElseThrow(() -> new BusinessException(
                        BusinessCodeEnum.SYSTEM_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Updated book not found after return."
                ));

        PatchBorrowRecordsResponseBodyDTO patchBorrowRecordsResponseBodyDTO = new PatchBorrowRecordsResponseBodyDTO();
        patchBorrowRecordsResponseBodyDTO.setBorrowRecord(mapBorrowRecordToDTO(updatedBorrowRecordEntity));
        patchBorrowRecordsResponseBodyDTO.setBook(updatedBookDTO);
        return patchBorrowRecordsResponseBodyDTO;
    }

    private BorrowRecordDTO mapBorrowRecordToDTO(BorrowRecordEntity borrowRecordEntity) {
        BorrowRecordDTO borrowRecordDTO = new BorrowRecordDTO();
        borrowRecordDTO.setId(borrowRecordEntity.getId());
        borrowRecordDTO.setBookId(borrowRecordEntity.getBookId());
        borrowRecordDTO.setBorrowerName(borrowRecordEntity.getBorrowerName());
        borrowRecordDTO.setStatus(borrowRecordEntity.getStatus());
        borrowRecordDTO.setBorrowedAt(borrowRecordEntity.getBorrowedAt());
        borrowRecordDTO.setReturnedAt(borrowRecordEntity.getReturnedAt());
        borrowRecordDTO.setCreatedAt(borrowRecordEntity.getCreatedAt());
        borrowRecordDTO.setUpdatedAt(borrowRecordEntity.getUpdatedAt());
        return borrowRecordDTO;
    }
}
