package com.example.library.borrowrecord.dto;

import com.example.library.book.dto.BookDTO;

public class PatchBorrowRecordsResponseBodyDTO {

    private BorrowRecordDTO borrowRecord;
    private BookDTO book;

    public BorrowRecordDTO getBorrowRecord() {
        return borrowRecord;
    }

    public void setBorrowRecord(BorrowRecordDTO borrowRecord) {
        this.borrowRecord = borrowRecord;
    }

    public BookDTO getBook() {
        return book;
    }

    public void setBook(BookDTO book) {
        this.book = book;
    }
}
