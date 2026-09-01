package com.example.library.controller;

import com.example.library.dto.ApiResponseDTO;
import com.example.library.dto.BookDTO;
import com.example.library.dto.LoanDTO;
import com.example.library.dto.LoanReturnDTO;
import com.example.library.dto.PostBooksRequestDTO;
import com.example.library.dto.PostLoansRequestDTO;
import com.example.library.dto.PostLoansReturnsRequestDTO;
import com.example.library.error.BusinessCodeEnum;
import com.example.library.service.LibraryService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/books")
    public ResponseEntity<ApiResponseDTO<BookDTO>> createBook(@RequestBody PostBooksRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                BusinessCodeEnum.SUCCESS.getValue(), "書籍新增成功", traceId(), libraryService.createBook(request)));
    }

    @GetMapping("/books")
    public ResponseEntity<ApiResponseDTO<List<BookDTO>>> listBooks() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                BusinessCodeEnum.SUCCESS.getValue(), "館藏列表取得成功", traceId(), libraryService.listBooks()));
    }

    @PostMapping("/loans")
    public ResponseEntity<ApiResponseDTO<LoanDTO>> checkoutBook(@RequestBody PostLoansRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDTO.success(
                BusinessCodeEnum.SUCCESS.getValue(), "借書成功", traceId(), libraryService.checkoutBook(request)));
    }

    @PostMapping("/loans/returns")
    public ResponseEntity<ApiResponseDTO<LoanReturnDTO>> returnBook(@RequestBody PostLoansReturnsRequestDTO request) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                BusinessCodeEnum.SUCCESS.getValue(), "還書成功", traceId(), libraryService.returnBook(request)));
    }

    private String traceId() {
        return UUID.randomUUID().toString();
    }
}
