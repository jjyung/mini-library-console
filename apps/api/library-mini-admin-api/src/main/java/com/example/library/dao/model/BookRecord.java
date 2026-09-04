package com.example.library.dao.model;

import java.util.UUID;

public class BookRecord {

    public enum Status {
        AVAILABLE,
        BORROWED,
        INACTIVE
    }

    private UUID bookId;
    private String title;
    private String isbn;
    private String author;
    private String category;
    private int totalCount;
    private int availableCount;
    private boolean active;

    public BookRecord() {
    }

    public BookRecord(UUID bookId, String title, String isbn, String author, String category,
                      int totalCount, int availableCount, boolean active) {
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.category = category;
        this.totalCount = totalCount;
        this.availableCount = availableCount;
        this.active = active;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Status getStatus() {
        if (!active) {
            return Status.INACTIVE;
        }
        return availableCount == 0 ? Status.BORROWED : Status.AVAILABLE;
    }
}
