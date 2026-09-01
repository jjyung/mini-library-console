package com.example.library.dto;

public class PostLoansReturnsRequestDTO {
    private String isbn;
    private String readerId;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
}
