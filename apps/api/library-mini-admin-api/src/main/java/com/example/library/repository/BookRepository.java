package com.example.library.repository;

import com.example.library.book.dto.BookDTO;
import com.example.library.book.model.BookAvailabilityStatusEnum;
import com.example.library.book.model.BookEntity;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {

    private static final RowMapper<BookEntity> BOOK_ENTITY_ROW_MAPPER = (resultSet, rowNumber) -> {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(resultSet.getLong("id"));
        bookEntity.setTitle(resultSet.getString("title"));
        bookEntity.setAuthor(resultSet.getString("author"));
        bookEntity.setTotalQuantity(resultSet.getInt("total_quantity"));
        bookEntity.setCreatedAt(Instant.parse(resultSet.getString("created_at")));
        bookEntity.setUpdatedAt(Instant.parse(resultSet.getString("updated_at")));
        return bookEntity;
    };

    private static final RowMapper<BookDTO> BOOK_VIEW_ROW_MAPPER = (resultSet, rowNumber) -> {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(resultSet.getLong("id"));
        bookDTO.setTitle(resultSet.getString("title"));
        bookDTO.setAuthor(resultSet.getString("author"));
        bookDTO.setTotalQuantity(resultSet.getInt("total_quantity"));
        bookDTO.setAvailableQuantity(resultSet.getInt("available_quantity"));
        bookDTO.setStatus(BookAvailabilityStatusEnum.valueOf(resultSet.getString("status")));
        bookDTO.setCreatedAt(Instant.parse(resultSet.getString("created_at")));
        bookDTO.setUpdatedAt(Instant.parse(resultSet.getString("updated_at")));
        return bookDTO;
    };

    private final JdbcTemplate jdbcTemplate;

    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BookEntity createBook(String title, String author, Integer totalQuantity, Instant nowInstant) {
        String insertSql = """
                INSERT INTO books(title, author, total_quantity, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(insertSql, new String[]{"id"});
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setInt(3, totalQuantity);
            preparedStatement.setString(4, nowInstant.toString());
            preparedStatement.setString(5, nowInstant.toString());
            return preparedStatement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Failed to create book id.");
        }
        return findBookById(generatedId.longValue()).orElseThrow(() -> new IllegalStateException("Created book not found."));
    }

    public Optional<BookEntity> findBookById(Long bookId) {
        String querySql = "SELECT id, title, author, total_quantity, created_at, updated_at FROM books WHERE id = ?";
        List<BookEntity> rows = jdbcTemplate.query(querySql, BOOK_ENTITY_ROW_MAPPER, bookId);
        return rows.stream().findFirst();
    }

    public List<BookDTO> listBooksWithAvailability() {
        String querySql = """
                SELECT b.id,
                       b.title,
                       b.author,
                       b.total_quantity,
                       b.created_at,
                       b.updated_at,
                       (b.total_quantity - COALESCE(SUM(CASE WHEN br.status = 'BORROWED' THEN 1 ELSE 0 END), 0)) AS available_quantity,
                       CASE
                           WHEN (b.total_quantity - COALESCE(SUM(CASE WHEN br.status = 'BORROWED' THEN 1 ELSE 0 END), 0)) > 0
                               THEN 'AVAILABLE'
                           ELSE 'BORROWED'
                           END AS status
                FROM books b
                         LEFT JOIN borrow_records br ON b.id = br.book_id
                GROUP BY b.id, b.title, b.author, b.total_quantity, b.created_at, b.updated_at
                ORDER BY b.id ASC
                """;
        return jdbcTemplate.query(querySql, BOOK_VIEW_ROW_MAPPER);
    }

    public Optional<BookDTO> findBookViewById(Long bookId) {
        String querySql = """
                SELECT b.id,
                       b.title,
                       b.author,
                       b.total_quantity,
                       b.created_at,
                       b.updated_at,
                       (b.total_quantity - COALESCE(SUM(CASE WHEN br.status = 'BORROWED' THEN 1 ELSE 0 END), 0)) AS available_quantity,
                       CASE
                           WHEN (b.total_quantity - COALESCE(SUM(CASE WHEN br.status = 'BORROWED' THEN 1 ELSE 0 END), 0)) > 0
                               THEN 'AVAILABLE'
                           ELSE 'BORROWED'
                           END AS status
                FROM books b
                         LEFT JOIN borrow_records br ON b.id = br.book_id
                WHERE b.id = ?
                GROUP BY b.id, b.title, b.author, b.total_quantity, b.created_at, b.updated_at
                """;
        List<BookDTO> rows = jdbcTemplate.query(querySql, BOOK_VIEW_ROW_MAPPER, bookId);
        return rows.stream().findFirst();
    }
}
