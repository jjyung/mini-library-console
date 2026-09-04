package com.example.library.dao;

import com.example.library.dao.model.BookRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookDao {

    List<BookRecord> listBooks();

    Optional<BookRecord> findByIsbn(String isbn);

    Optional<BookRecord> findByIsbnForUpdate(String isbn);

    BookRecord insert(BookRecord book);

    int decrementAvailableCount(UUID bookId);

    int incrementAvailableCount(UUID bookId);
}
