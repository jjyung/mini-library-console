package com.example.library.domain;

import java.util.List;

public class BookPage {
    private List<Book> books;
    private Integer total;

    public BookPage(List<Book> books, Integer total) {
        this.books = books;
        this.total = total;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
