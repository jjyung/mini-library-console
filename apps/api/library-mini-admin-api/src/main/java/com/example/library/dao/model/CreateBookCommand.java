package com.example.library.dao.model;

public class CreateBookCommand {

    private String title;
    private String isbn;
    private String author;
    private String category;
    private int quantity;
    private boolean active;

    public CreateBookCommand(String title, String isbn, String author, String category,
                             int quantity, boolean active) {
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.category = category;
        this.quantity = quantity;
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isActive() {
        return active;
    }
}
