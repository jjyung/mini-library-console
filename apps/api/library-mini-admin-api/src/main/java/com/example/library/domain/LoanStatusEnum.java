package com.example.library.domain;

public enum LoanStatusEnum {
    ACTIVE,
    RETURNED;

    public static LoanStatusEnum fromDatabaseValue(String value) {
        return LoanStatusEnum.valueOf(value);
    }
}
