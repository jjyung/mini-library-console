package com.example.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public class PostLoansRequestDTO {
    @NotBlank
    private String readerId;
    @NotBlank
    private String isbn;
    private String dueDate;

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
