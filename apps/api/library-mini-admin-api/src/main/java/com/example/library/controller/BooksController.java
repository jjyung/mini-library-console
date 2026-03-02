package com.example.library.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.ApiResponseDTO;
import com.example.library.dto.GetBooksRequestDTO;
import com.example.library.dto.GetBooksResponseDataDTO;
import com.example.library.dto.PostBooksRequestDTO;
import com.example.library.dto.PostBooksResponseDataDTO;
import com.example.library.service.LibraryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/books")
@Validated
public class BooksController {

    private final LibraryService libraryService;

    public BooksController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping
    public ApiResponseDTO<PostBooksResponseDataDTO> postBooks(@Valid @RequestBody PostBooksRequestDTO requestDto) {
        return ApiResponseDTO.success(new PostBooksResponseDataDTO(libraryService.createBook(requestDto)));
    }

    @GetMapping
    public ApiResponseDTO<GetBooksResponseDataDTO> getBooks(@Valid @ModelAttribute GetBooksRequestDTO requestDto) {
        return ApiResponseDTO.success(new GetBooksResponseDataDTO(libraryService.listBooks(requestDto)));
    }
}
