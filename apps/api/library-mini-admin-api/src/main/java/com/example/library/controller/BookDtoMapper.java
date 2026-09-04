package com.example.library.controller;

import com.example.library.dao.model.BookRecord;
import com.example.library.generated.dto.BookDTO;

public final class BookDtoMapper {

    private BookDtoMapper() {
    }

    public static BookDTO toDto(BookRecord book) {
        BookDTO dto = new BookDTO(book.getBookId(), book.getTitle(), book.getIsbn(), book.getCategory(),
                BookDTO.StatusEnum.valueOf(book.getStatus().name()), book.getAvailableCount(), book.getTotalCount(),
                book.isActive());
        if (book.getAuthor() != null) {
            dto.author(book.getAuthor());
        }
        return dto;
    }
}
