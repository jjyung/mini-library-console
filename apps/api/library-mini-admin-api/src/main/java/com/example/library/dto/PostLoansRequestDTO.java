package com.example.library.dto;

public class PostLoansRequestDTO {
    private String readerId;
    private String isbn;
    private String dueDate;

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
