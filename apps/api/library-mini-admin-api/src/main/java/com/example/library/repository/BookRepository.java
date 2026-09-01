package com.example.library.repository;

import com.example.library.domain.Book;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findAll();
    Book save(Book book);
}
