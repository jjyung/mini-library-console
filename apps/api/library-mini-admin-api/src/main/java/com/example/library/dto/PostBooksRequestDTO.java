package com.example.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostBooksRequestDTO {
    private String title;
    private String isbn;
    private String author;
    private String category;
    private Integer quantity;
    private Boolean active;

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
    @JsonProperty("isActive")
    public Boolean getActive() { return active; }
    @JsonProperty("isActive")
    public void setActive(Boolean active) { this.active = active; }
}
