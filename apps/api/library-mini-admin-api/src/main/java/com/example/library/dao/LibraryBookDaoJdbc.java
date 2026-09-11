package com.example.library.dao;

import com.example.library.domain.Book;
import com.example.library.domain.BookPage;
import com.example.library.domain.BookStatusEnum;
import com.example.library.domain.Loan;
import com.example.library.domain.LoanStatusEnum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LibraryBookDaoJdbc implements LibraryBookDao {
    private static final String BOOK_COLUMNS = "book_id, title, isbn, author, category, total_count, available_count, status, is_active, created_at, updated_at";
    private static final String LOAN_COLUMNS = "loan_id, book_id, reader_id, borrowed_at, due_date, returned_at, status, created_at, updated_at";
    private static final String BOOK_TABLE = " FROM books";
    private static final String LOAN_TABLE = " FROM loans";

    private final JdbcTemplate jdbcTemplate;

    public LibraryBookDaoJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BookPage listBooks(String query, int page, int pageSize) {
        String normalizedQuery = normalizeQuery(query);
        String whereClause = normalizedQuery.isEmpty()
                ? ""
                : " WHERE LOWER(title) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(COALESCE(author, '')) LIKE ?";
        String queryPattern = "%" + normalizedQuery.toLowerCase() + "%";

        List<Object> countArguments = createQueryArguments(whereClause, queryPattern);
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*)" + BOOK_TABLE + whereClause,
                Integer.class,
                countArguments.toArray()
        );

        long offset = ((long) page - 1L) * pageSize;
        List<Object> pageArguments = createQueryArguments(whereClause, queryPattern);
        pageArguments.add(pageSize);
        pageArguments.add(offset);
        List<Book> books = jdbcTemplate.query(
                "SELECT " + BOOK_COLUMNS + BOOK_TABLE + whereClause
                        + " ORDER BY updated_at DESC, book_id ASC LIMIT ? OFFSET ?",
                bookRowMapper(),
                pageArguments.toArray()
        );
        books.forEach(book -> book.setActiveLoan(findActiveLoanByBookId(book.getBookId()).orElse(null)));
        return new BookPage(books, total == null ? 0 : total);
    }

    @Override
    public Optional<Book> findBookById(UUID bookId) {
        return findBook("SELECT " + BOOK_COLUMNS + BOOK_TABLE + " WHERE book_id = ?", bookId);
    }

    @Override
    public Optional<Book> findBookByIdForUpdate(UUID bookId) {
        return findBook("SELECT " + BOOK_COLUMNS + BOOK_TABLE + " WHERE book_id = ? FOR UPDATE", bookId);
    }

    @Override
    public Optional<Book> findBookByIsbn(String isbn) {
        return findBook("SELECT " + BOOK_COLUMNS + BOOK_TABLE + " WHERE isbn = ?", isbn);
    }

    @Override
    public Optional<Loan> findActiveLoanByBookId(UUID bookId) {
        List<Loan> loans = jdbcTemplate.query(
                "SELECT " + LOAN_COLUMNS + LOAN_TABLE
                        + " WHERE book_id = ? AND status = ? ORDER BY borrowed_at DESC LIMIT 1",
                loanRowMapper(),
                bookId,
                LoanStatusEnum.ACTIVE.name()
        );
        return loans.stream().findFirst();
    }

    @Override
    public Optional<Loan> findLoanByIdForUpdate(UUID bookId, UUID loanId) {
        List<Loan> loans = jdbcTemplate.query(
                "SELECT " + LOAN_COLUMNS + LOAN_TABLE
                        + " WHERE book_id = ? AND loan_id = ? FOR UPDATE",
                loanRowMapper(),
                bookId,
                loanId
        );
        return loans.stream().findFirst();
    }

    @Override
    public Book saveBook(Book book) {
        jdbcTemplate.update(
                "INSERT INTO books (book_id, title, isbn, author, category, total_count, available_count, status, is_active)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                book.getBookId(),
                book.getTitle(),
                book.getIsbn(),
                book.getAuthor(),
                book.getCategory(),
                book.getTotalCount(),
                book.getAvailableCount(),
                book.getStatus().getDatabaseValue(),
                book.getActive()
        );
        return findBookById(book.getBookId()).orElseThrow(
                () -> new IllegalStateException("Created book cannot be reloaded")
        );
    }

    @Override
    public Loan saveLoan(Loan loan) {
        jdbcTemplate.update(
                "INSERT INTO loans (loan_id, book_id, reader_id, borrowed_at, due_date, status)"
                        + " VALUES (?, ?, ?, ?, ?, ?)",
                loan.getLoanId(),
                loan.getBookId(),
                loan.getReaderId(),
                loan.getBorrowedAt(),
                loan.getDueDate(),
                loan.getStatus().name()
        );
        return findLoanByIdForUpdate(loan.getBookId(), loan.getLoanId()).orElseThrow(
                () -> new IllegalStateException("Created loan cannot be reloaded")
        );
    }

    @Override
    public void updateBookInventory(UUID bookId, int availableCount, String status) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE books SET available_count = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE book_id = ?",
                availableCount,
                status,
                bookId
        );
        if (updatedRows != 1) {
            throw new IllegalStateException("Book inventory update affected an unexpected number of rows");
        }
    }

    @Override
    public void markLoanReturned(UUID bookId, UUID loanId, OffsetDateTime returnedAt) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE loans SET returned_at = ?, status = ?, updated_at = CURRENT_TIMESTAMP"
                        + " WHERE book_id = ? AND loan_id = ? AND status = ?",
                returnedAt,
                LoanStatusEnum.RETURNED.name(),
                bookId,
                loanId,
                LoanStatusEnum.ACTIVE.name()
        );
        if (updatedRows != 1) {
            throw new IllegalStateException("Loan return update affected an unexpected number of rows");
        }
    }

    private Optional<Book> findBook(String sql, Object argument) {
        List<Book> books = jdbcTemplate.query(sql, bookRowMapper(), argument);
        return books.stream().findFirst();
    }

    private List<Object> createQueryArguments(String whereClause, String queryPattern) {
        List<Object> arguments = new ArrayList<>();
        if (!whereClause.isEmpty()) {
            arguments.add(queryPattern);
            arguments.add(queryPattern);
            arguments.add(queryPattern);
        }
        return arguments;
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private RowMapper<Book> bookRowMapper() {
        return (resultSet, rowNumber) -> mapBook(resultSet);
    }

    private RowMapper<Loan> loanRowMapper() {
        return (resultSet, rowNumber) -> mapLoan(resultSet);
    }

    private Book mapBook(ResultSet resultSet) throws SQLException {
        Book book = new Book();
        book.setBookId(resultSet.getObject("book_id", UUID.class));
        book.setTitle(resultSet.getString("title"));
        book.setIsbn(resultSet.getString("isbn"));
        book.setAuthor(resultSet.getString("author"));
        book.setCategory(resultSet.getString("category"));
        book.setTotalCount(resultSet.getInt("total_count"));
        book.setAvailableCount(resultSet.getInt("available_count"));
        book.setStatus(BookStatusEnum.fromDatabaseValue(resultSet.getString("status")));
        book.setActive(resultSet.getBoolean("is_active"));
        book.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
        book.setUpdatedAt(resultSet.getObject("updated_at", OffsetDateTime.class));
        return book;
    }

    private Loan mapLoan(ResultSet resultSet) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(resultSet.getObject("loan_id", UUID.class));
        loan.setBookId(resultSet.getObject("book_id", UUID.class));
        loan.setReaderId(resultSet.getString("reader_id"));
        loan.setBorrowedAt(resultSet.getObject("borrowed_at", OffsetDateTime.class));
        loan.setDueDate(resultSet.getObject("due_date", LocalDate.class));
        loan.setReturnedAt(resultSet.getObject("returned_at", OffsetDateTime.class));
        loan.setStatus(LoanStatusEnum.fromDatabaseValue(resultSet.getString("status")));
        loan.setCreatedAt(resultSet.getObject("created_at", OffsetDateTime.class));
        loan.setUpdatedAt(resultSet.getObject("updated_at", OffsetDateTime.class));
        return loan;
    }
}
