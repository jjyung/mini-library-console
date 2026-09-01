package com.example.library.repository;

import com.example.library.domain.Loan;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLoanRepositoryImpl implements LoanRepository {

    private final ConcurrentMap<UUID, Loan> loans = new ConcurrentHashMap<>();

    @Override
    public Loan save(Loan loan) {
        loans.put(loan.getLoanId(), loan);
        return loan;
    }

    @Override
    public Optional<Loan> findOldestActiveByBookId(UUID bookId) {
        return loans.values().stream()
                .filter(loan -> loan.getBookId().equals(bookId) && loan.getReturnedAt() == null)
                .min(Comparator.comparing(Loan::getLoanedAt));
    }

    @Override
    public Optional<Loan> findOldestActiveByBookIdAndReaderId(UUID bookId, String readerId) {
        return loans.values().stream()
                .filter(loan -> loan.getBookId().equals(bookId)
                        && loan.getReaderId().equals(readerId)
                        && loan.getReturnedAt() == null)
                .min(Comparator.comparing(Loan::getLoanedAt));
    }
}
