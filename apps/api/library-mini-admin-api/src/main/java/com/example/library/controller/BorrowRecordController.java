package com.example.library.controller;

import com.example.library.borrowrecord.dto.PatchBorrowRecordsRequestDTO;
import com.example.library.borrowrecord.dto.PatchBorrowRecordsResponseBodyDTO;
import com.example.library.borrowrecord.dto.PostBorrowRecordsRequestDTO;
import com.example.library.borrowrecord.dto.PostBorrowRecordsResponseBodyDTO;
import com.example.library.common.dto.ApiResponseDTO;
import com.example.library.service.LibraryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class BorrowRecordController {

    private final LibraryService libraryService;

    public BorrowRecordController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/borrow-records")
    public ResponseEntity<ApiResponseDTO<PostBorrowRecordsResponseBodyDTO>> borrowBook(
            @Valid @RequestBody PostBorrowRecordsRequestDTO postBorrowRecordsRequestDTO) {
        PostBorrowRecordsResponseBodyDTO responseBodyDTO = libraryService.borrowBook(postBorrowRecordsRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Book borrowed successfully.", responseBodyDTO));
    }

    @PatchMapping("/borrow-records/{borrowRecordId}/return")
    public ResponseEntity<ApiResponseDTO<PatchBorrowRecordsResponseBodyDTO>> returnBook(
            @PathVariable @Min(1) Long borrowRecordId,
            @RequestBody(required = false) PatchBorrowRecordsRequestDTO patchBorrowRecordsRequestDTO) {
        PatchBorrowRecordsResponseBodyDTO responseBodyDTO = libraryService.returnBorrowRecord(borrowRecordId);
        return ResponseEntity.ok(ApiResponseDTO.success("Book returned successfully.", responseBodyDTO));
    }
}
