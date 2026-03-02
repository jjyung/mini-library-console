package com.example.library.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BorrowRecordEntity {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String borrowerName;
    private String borrowedAt;
    private String returnedAt;
    private String status;
}
