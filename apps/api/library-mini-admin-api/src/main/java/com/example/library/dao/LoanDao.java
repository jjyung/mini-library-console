package com.example.library.dao;

import com.example.library.dao.model.LoanRecord;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LoanDao {

    LoanRecord insert(LoanRecord loan);

    List<LoanRecord> findActiveByBookId(UUID bookId, String readerId);

    int markReturned(UUID loanId, OffsetDateTime returnedAt);
}
