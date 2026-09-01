package com.example.library.domain;

import java.util.UUID;

public class Book {

    private UUID bookId;
    private String title;
    private String isbn;
    private String author;
    private String category;
    private BookStatusEnum status;
    private boolean active;
    private int availableCount;
    private int totalCount;

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
    public BookStatusEnum getStatus() { return status; }
    public void setStatus(BookStatusEnum status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getAvailableCount() { return availableCount; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
