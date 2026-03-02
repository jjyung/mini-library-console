package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BorrowRecordDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String borrowerName;
    private String borrowedAt;
    private String returnedAt;
    private String status;
}
