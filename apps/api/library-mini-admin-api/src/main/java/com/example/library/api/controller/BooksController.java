package com.example.library.api.controller;

import com.example.library.api.dto.GetBooksResponseDTO;
import com.example.library.api.dto.GetBooksSearchResponseDTO;
import com.example.library.api.dto.PostBooksRequestDTO;
import com.example.library.api.dto.PostBooksResponseDTO;
import com.example.library.common.ApiResponse;
import com.example.library.service.BooksService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/library/books")
public class BooksController {
    private final BooksService booksService;

    public BooksController(BooksService booksService) {
        this.booksService = booksService;
    }

    @PostMapping
    public PostBooksResponseDTO postBooks(@Valid @RequestBody PostBooksRequestDTO requestDTO) {
        Map<String, Object> data = booksService.createBook(requestDTO);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return new PostBooksResponseDTO(response.getCode(), response.getMessage(), response.getData());
    }

    @GetMapping
    public GetBooksResponseDTO getBooks(@RequestParam(required = false) String shelfStatus) {
        Map<String, Object> data = booksService.listBooks(shelfStatus);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return new GetBooksResponseDTO(response.getCode(), response.getMessage(), response.getData());
    }

    @GetMapping("/search")
    public GetBooksSearchResponseDTO getBooksSearch(@RequestParam String keyword) {
        Map<String, Object> data = booksService.searchBooks(keyword);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return new GetBooksSearchResponseDTO(response.getCode(), response.getMessage(), response.getData());
    }
}
