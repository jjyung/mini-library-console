package com.example.library.dao.model;

import java.time.LocalDate;

public class BorrowCommand {

    private String readerId;
    private String isbn;
    private LocalDate dueDate;

    public BorrowCommand(String readerId, String isbn, LocalDate dueDate) {
        this.readerId = readerId;
        this.isbn = isbn;
        this.dueDate = dueDate;
    }

    public String getReaderId() {
        return readerId;
    }

    public String getIsbn() {
        return isbn;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
