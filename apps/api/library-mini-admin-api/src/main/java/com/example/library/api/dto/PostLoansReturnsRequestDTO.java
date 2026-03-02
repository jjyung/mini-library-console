package com.example.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public class PostLoansReturnsRequestDTO {
    @NotBlank
    private String isbn;
    private String readerId;
    private String returnedAt;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }
    public String getReturnedAt() { return returnedAt; }
    public void setReturnedAt(String returnedAt) { this.returnedAt = returnedAt; }
}
