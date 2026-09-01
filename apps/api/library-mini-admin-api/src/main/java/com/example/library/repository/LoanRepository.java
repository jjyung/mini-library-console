package com.example.library.repository;

import com.example.library.domain.Loan;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findOldestActiveByBookId(UUID bookId);
    Optional<Loan> findOldestActiveByBookIdAndReaderId(UUID bookId, String readerId);
}
