package com.example.library.controller;

import com.example.library.common.TraceIdService;
import com.example.library.dao.model.BookRecord;
import com.example.library.dao.model.CreateBookCommand;
import com.example.library.generated.api.BooksApi;
import com.example.library.generated.dto.BookDTO;
import com.example.library.generated.dto.GetBooksResponseDTO;
import com.example.library.generated.dto.PostBooksRequestDTO;
import com.example.library.generated.dto.PostBooksResponseDTO;
import com.example.library.service.LibraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LibraryBookController implements BooksApi {

    private static final String SUCCESS_MESSAGE = "Success";

    private final LibraryService libraryService;
    private final TraceIdService traceIdService;

    public LibraryBookController(LibraryService libraryService, TraceIdService traceIdService) {
        this.libraryService = libraryService;
        this.traceIdService = traceIdService;
    }

    @Override
    public ResponseEntity<GetBooksResponseDTO> getBooks() {
        List<BookDTO> books = libraryService.listBooks().stream()
                .map(BookDtoMapper::toDto)
                .toList();
        GetBooksResponseDTO response = new GetBooksResponseDTO(GetBooksResponseDTO.CodeEnum._00000,
                SUCCESS_MESSAGE, traceIdService.getOrCreateTraceId(), books);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PostBooksResponseDTO> postBooks(PostBooksRequestDTO request) {
        CreateBookCommand command = new CreateBookCommand(request.getTitle(), request.getIsbn(), request.getAuthor(),
                request.getCategory(), request.getQuantity(), request.getIsActive() == null || request.getIsActive());
        BookDTO book = BookDtoMapper.toDto(libraryService.createBook(command));
        PostBooksResponseDTO response = new PostBooksResponseDTO(PostBooksResponseDTO.CodeEnum._00000,
                SUCCESS_MESSAGE, traceIdService.getOrCreateTraceId(), book);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
