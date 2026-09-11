package com.example.library.controller;

import com.example.library.application.LibraryService;
import com.example.library.domain.Book;
import com.example.library.domain.BookPage;
import com.example.library.domain.BookTransactionResult;
import com.example.library.generated.api.BooksApi;
import com.example.library.generated.dto.BookDetailResponseDTO;
import com.example.library.generated.dto.GetBooksDataResponseDTO;
import com.example.library.generated.dto.GetBooksResponseDTO;
import com.example.library.generated.dto.PostBookBorrowsDataResponseDTO;
import com.example.library.generated.dto.PostBookBorrowsRequestDTO;
import com.example.library.generated.dto.PostBookBorrowsResponseDTO;
import com.example.library.generated.dto.PostBookReturnsDataResponseDTO;
import com.example.library.generated.dto.PostBookReturnsRequestDTO;
import com.example.library.generated.dto.PostBookReturnsResponseDTO;
import com.example.library.generated.dto.PostBooksRequestDTO;
import com.example.library.generated.dto.PostBooksResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class LibraryBookController implements BooksApi {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final LibraryService libraryService;
    private final LibraryResponseMapper responseMapper;

    public LibraryBookController(LibraryService libraryService, LibraryResponseMapper responseMapper) {
        this.libraryService = libraryService;
        this.responseMapper = responseMapper;
    }

    @Override
    public ResponseEntity<GetBooksResponseDTO> getApiBooks(
            String xCorrelationId,
            String query,
            Integer page,
            Integer pageSize
    ) {
        int resolvedPage = page == null ? DEFAULT_PAGE : page;
        int resolvedPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        BookPage bookPage = libraryService.listBooks(query, resolvedPage, resolvedPageSize);
        GetBooksDataResponseDTO data = responseMapper.toBookPageResponse(bookPage, resolvedPage, resolvedPageSize);
        return ResponseEntity.ok(new GetBooksResponseDTO(
                GetBooksResponseDTO.CodeEnum._00000,
                "館藏查詢成功",
                resolveTraceId(xCorrelationId),
                data
        ));
    }

    @Override
    public ResponseEntity<PostBooksResponseDTO> postApiBooks(
            String idempotencyKey,
            PostBooksRequestDTO postBooksRequestDTO,
            String xCorrelationId
    ) {
        Book book = libraryService.createBook(idempotencyKey, postBooksRequestDTO);
        BookDetailResponseDTO data = responseMapper.toBookDetail(book, null);
        return ResponseEntity.status(201).body(new PostBooksResponseDTO(
                PostBooksResponseDTO.CodeEnum._00000,
                "書籍建立成功",
                resolveTraceId(xCorrelationId),
                data
        ));
    }

    @Override
    public ResponseEntity<PostBookBorrowsResponseDTO> postApiBooksBookIdBorrow(
            UUID bookId,
            String idempotencyKey,
            PostBookBorrowsRequestDTO postBookBorrowsRequestDTO,
            String xCorrelationId
    ) {
        BookTransactionResult result = libraryService.borrowBook(
                bookId,
                idempotencyKey,
                postBookBorrowsRequestDTO
        );
        PostBookBorrowsDataResponseDTO data = new PostBookBorrowsDataResponseDTO(
                responseMapper.toBookDetail(result.getBook(), result.getLoan()),
                responseMapper.toLoanSummary(result.getLoan())
        );
        return ResponseEntity.ok(new PostBookBorrowsResponseDTO(
                PostBookBorrowsResponseDTO.CodeEnum._00000,
                "借出成功",
                resolveTraceId(xCorrelationId),
                data
        ));
    }

    @Override
    public ResponseEntity<PostBookReturnsResponseDTO> postApiBooksBookIdReturn(
            UUID bookId,
            String idempotencyKey,
            PostBookReturnsRequestDTO postBookReturnsRequestDTO,
            String xCorrelationId
    ) {
        BookTransactionResult result = libraryService.returnBook(
                bookId,
                idempotencyKey,
                postBookReturnsRequestDTO
        );
        PostBookReturnsDataResponseDTO data = new PostBookReturnsDataResponseDTO(
                responseMapper.toBookDetail(result.getBook(), null),
                responseMapper.toLoanSummary(result.getLoan())
        );
        return ResponseEntity.ok(new PostBookReturnsResponseDTO(
                PostBookReturnsResponseDTO.CodeEnum._00000,
                "歸還成功",
                resolveTraceId(xCorrelationId),
                data
        ));
    }

    private String resolveTraceId(String correlationId) {
        return correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString()
                : correlationId.trim();
    }
}
