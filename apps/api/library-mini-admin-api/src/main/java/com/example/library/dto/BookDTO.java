package com.example.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class BookDTO {
    private UUID bookId;
    private String title;
    private String isbn;
    private String author;
    private String category;
    private String status;
    private int availableCount;
    private int totalCount;
    private boolean active;

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAvailableCount() { return availableCount; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    @JsonProperty("isActive")
    public boolean isActive() { return active; }
    @JsonProperty("isActive")
    public void setActive(boolean active) { this.active = active; }
}
