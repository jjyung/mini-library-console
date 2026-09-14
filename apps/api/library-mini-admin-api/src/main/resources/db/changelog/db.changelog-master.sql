--liquibase formatted sql

--changeset be:books-001
CREATE TABLE books (
    book_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    author VARCHAR(100) NULL,
    category VARCHAR(20) NOT NULL,
    total_count INTEGER NOT NULL,
    available_count INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT ck_books_title_not_blank CHECK (TRIM(title) <> ''),
    CONSTRAINT ck_books_isbn_not_blank CHECK (TRIM(isbn) <> ''),
    CONSTRAINT ck_books_author_not_blank CHECK (author IS NULL OR TRIM(author) <> ''),
    CONSTRAINT ck_books_category CHECK (
        category IN (
            'literature', 'science', 'technology', 'history',
            'art', 'philosophy', 'business', 'education'
        )
    ),
    CONSTRAINT ck_books_total_positive CHECK (total_count > 0),
    CONSTRAINT ck_books_available_range CHECK (
        available_count >= 0 AND available_count <= total_count
    ),
    CONSTRAINT ck_books_status_mapping CHECK (
        (is_active = FALSE AND status = 'INACTIVE')
        OR (
            is_active = TRUE
            AND (
                (available_count > 0 AND status = 'AVAILABLE')
                OR (available_count = 0 AND status = 'BORROWED')
            )
        )
    ),
    CONSTRAINT ck_books_timestamp_order CHECK (updated_at >= created_at)
);

CREATE UNIQUE INDEX uk_books_isbn ON books (isbn);
CREATE INDEX idx_books_status_updated_at ON books (status, updated_at);
--rollback DROP TABLE books;

--changeset be:loans-001
CREATE TABLE loans (
    loan_id UUID NOT NULL,
    book_id UUID NOT NULL,
    reader_id VARCHAR(100) NOT NULL,
    borrowed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NULL,
    returned_at TIMESTAMP WITH TIME ZONE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_loans PRIMARY KEY (loan_id),
    CONSTRAINT fk_loans_book FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT ck_loans_reader_not_blank CHECK (TRIM(reader_id) <> ''),
    CONSTRAINT ck_loans_status CHECK (status IN ('ACTIVE', 'RETURNED')),
    CONSTRAINT ck_loans_return_mapping CHECK (
        (status = 'ACTIVE' AND returned_at IS NULL)
        OR (status = 'RETURNED' AND returned_at IS NOT NULL)
    ),
    CONSTRAINT ck_loans_time_order CHECK (
        returned_at IS NULL OR returned_at >= borrowed_at
    ),
    CONSTRAINT ck_loans_timestamp_order CHECK (updated_at >= created_at)
);

CREATE INDEX idx_loans_book_status ON loans (book_id, status);
CREATE INDEX idx_loans_reader_status ON loans (reader_id, status);
--rollback DROP TABLE loans;

--changeset be:books-002
ALTER TABLE books DROP CONSTRAINT ck_books_status_mapping;

ALTER TABLE books ADD CONSTRAINT ck_books_status_mapping CHECK (
    (is_active = FALSE AND status = 'INACTIVE')
    OR (
        is_active = TRUE
        AND status IN ('AVAILABLE', 'BORROWED')
    )
);

--rollback ALTER TABLE books DROP CONSTRAINT ck_books_status_mapping;
--rollback ALTER TABLE books ADD CONSTRAINT ck_books_status_mapping CHECK (
--rollback     (is_active = FALSE AND status = 'INACTIVE')
--rollback     OR (
--rollback         is_active = TRUE
--rollback         AND (
--rollback             (available_count > 0 AND status = 'AVAILABLE')
--rollback             OR (available_count = 0 AND status = 'BORROWED')
--rollback         )
--rollback     )
--rollback );
