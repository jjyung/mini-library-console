package com.example.library.domain;

public class BookRecord {
    private String bookId;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int totalQuantity;
    private int availableQuantity;
    private String shelfStatus;
    private String circulationStatus;
    private String currentBorrowerReaderId;
    private String currentDueDate;
    private String createdAt;
    private String updatedAt;

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public String getShelfStatus() { return shelfStatus; }
    public void setShelfStatus(String shelfStatus) { this.shelfStatus = shelfStatus; }
    public String getCirculationStatus() { return circulationStatus; }
    public void setCirculationStatus(String circulationStatus) { this.circulationStatus = circulationStatus; }
    public String getCurrentBorrowerReaderId() { return currentBorrowerReaderId; }
    public void setCurrentBorrowerReaderId(String currentBorrowerReaderId) { this.currentBorrowerReaderId = currentBorrowerReaderId; }
    public String getCurrentDueDate() { return currentDueDate; }
    public void setCurrentDueDate(String currentDueDate) { this.currentDueDate = currentDueDate; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
