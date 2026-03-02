package com.example.library.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.ApiResponseDTO;
import com.example.library.dto.GetBorrowRecordsRequestDTO;
import com.example.library.dto.GetBorrowRecordsResponseDataDTO;
import com.example.library.dto.PatchBorrowRecordsRequestDTO;
import com.example.library.dto.PatchBorrowRecordsResponseDataDTO;
import com.example.library.dto.PostBorrowRecordsRequestDTO;
import com.example.library.dto.PostBorrowRecordsResponseDataDTO;
import com.example.library.service.LibraryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/borrow-records")
@Validated
public class BorrowRecordsController {

    private final LibraryService libraryService;

    public BorrowRecordsController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping
    public ApiResponseDTO<PostBorrowRecordsResponseDataDTO> postBorrowRecords(
        @Valid @RequestBody PostBorrowRecordsRequestDTO requestDto) {
        return ApiResponseDTO.success(new PostBorrowRecordsResponseDataDTO(libraryService.borrowBook(requestDto)));
    }

    @PatchMapping("/{borrowRecordId}/return")
    public ApiResponseDTO<PatchBorrowRecordsResponseDataDTO> patchBorrowRecordsReturn(
        @PathVariable @Min(1) long borrowRecordId,
        @Valid @RequestBody PatchBorrowRecordsRequestDTO requestDto) {
        return ApiResponseDTO.success(new PatchBorrowRecordsResponseDataDTO(libraryService.returnBook(borrowRecordId, requestDto)));
    }

    @GetMapping
    public ApiResponseDTO<GetBorrowRecordsResponseDataDTO> getBorrowRecords(
        @Valid @ModelAttribute GetBorrowRecordsRequestDTO requestDto) {
        return ApiResponseDTO.success(new GetBorrowRecordsResponseDataDTO(libraryService.listBorrowRecords(requestDto)));
    }
}
