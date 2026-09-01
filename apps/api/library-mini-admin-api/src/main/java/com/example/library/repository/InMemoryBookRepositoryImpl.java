package com.example.library.repository;

import com.example.library.domain.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBookRepositoryImpl implements BookRepository {

    private final ConcurrentMap<UUID, Book> books = new ConcurrentHashMap<>();

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return books.values().stream().filter(book -> book.getIsbn().equals(isbn)).findFirst();
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public Book save(Book book) {
        books.put(book.getBookId(), book);
        return book;
    }
}
