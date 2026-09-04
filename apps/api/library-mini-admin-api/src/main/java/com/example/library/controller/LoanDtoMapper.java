package com.example.library.controller;

import com.example.library.dao.model.LoanRecord;
import com.example.library.generated.dto.LoanDTO;

public final class LoanDtoMapper {

    private LoanDtoMapper() {
    }

    public static LoanDTO toDto(LoanRecord loan) {
        LoanDTO dto = new LoanDTO(loan.getLoanId(), loan.getBookId(), loan.getReaderId(), loan.getBorrowedAt());
        if (loan.getDueDate() != null) {
            dto.dueDate(loan.getDueDate());
        }
        if (loan.getReturnedAt() != null) {
            dto.returnedAt(loan.getReturnedAt());
        }
        return dto;
    }
}
