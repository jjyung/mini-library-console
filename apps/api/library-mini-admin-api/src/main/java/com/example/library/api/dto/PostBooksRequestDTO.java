package com.example.library.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PostBooksRequestDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String isbn;
    private String author;
    @NotBlank
    private String category;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotBlank
    private String shelfStatus;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getShelfStatus() { return shelfStatus; }
    public void setShelfStatus(String shelfStatus) { this.shelfStatus = shelfStatus; }
}
