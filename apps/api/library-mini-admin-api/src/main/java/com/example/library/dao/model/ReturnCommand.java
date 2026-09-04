package com.example.library.dao.model;

public class ReturnCommand {

    private String isbn;
    private String readerId;

    public ReturnCommand(String isbn, String readerId) {
        this.isbn = isbn;
        this.readerId = readerId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getReaderId() {
        return readerId;
    }
}
