package com.example.library.controller;

import com.example.library.book.dto.GetBooksResponseBodyDTO;
import com.example.library.book.dto.PostBooksRequestDTO;
import com.example.library.book.dto.PostBooksResponseBodyDTO;
import com.example.library.common.dto.ApiResponseDTO;
import com.example.library.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    private final LibraryService libraryService;

    public BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/books")
    public ResponseEntity<ApiResponseDTO<PostBooksResponseBodyDTO>> createBook(
            @Valid @RequestBody PostBooksRequestDTO postBooksRequestDTO) {
        PostBooksResponseBodyDTO responseBodyDTO = libraryService.createBook(postBooksRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Book created successfully.", responseBodyDTO));
    }

    @GetMapping("/books")
    public ResponseEntity<ApiResponseDTO<GetBooksResponseBodyDTO>> getBooks() {
        GetBooksResponseBodyDTO responseBodyDTO = libraryService.getBooks();
        return ResponseEntity.ok(ApiResponseDTO.success("Books fetched successfully.", responseBodyDTO));
    }
}
