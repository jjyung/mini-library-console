package com.example.library.book.dto;

import java.util.List;

public class GetBooksResponseBodyDTO {

    private List<BookDTO> books;

    public List<BookDTO> getBooks() {
        return books;
    }

    public void setBooks(List<BookDTO> books) {
        this.books = books;
    }
}
