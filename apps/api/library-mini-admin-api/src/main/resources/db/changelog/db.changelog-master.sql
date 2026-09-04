--liquibase formatted sql

--changeset library:001-create-books
CREATE TABLE books (
    book_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    author VARCHAR(200),
    category VARCHAR(30) NOT NULL,
    total_count INTEGER NOT NULL,
    available_count INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT ck_books_total_count_positive CHECK (total_count >= 1),
    CONSTRAINT ck_books_available_count_range CHECK (available_count >= 0 AND available_count <= total_count),
    CONSTRAINT ck_books_author_length CHECK (author IS NULL OR CHAR_LENGTH(author) <= 200)
);
--rollback DROP TABLE books;

--changeset library:002-create-loans
CREATE TABLE loans (
    loan_id UUID NOT NULL,
    book_id UUID NOT NULL,
    reader_id VARCHAR(100) NOT NULL,
    borrowed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE,
    returned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_loans PRIMARY KEY (loan_id),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT ck_loans_reader_id_non_empty CHECK (CHAR_LENGTH(reader_id) >= 1),
    CONSTRAINT ck_loans_return_order CHECK (returned_at IS NULL OR returned_at >= borrowed_at)
);
--rollback DROP TABLE loans;
