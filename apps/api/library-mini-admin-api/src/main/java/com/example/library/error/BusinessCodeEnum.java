package com.example.library.error;

public enum BusinessCodeEnum {
    SUCCESS("00000"),
    CLIENT_ERROR("A0000"),
    INVALID_INPUT("A0001"),
    BOOK_NOT_FOUND("A0002"),
    DUPLICATE_ISBN("A0003"),
    NO_AVAILABLE_COPY("A0004"),
    BOOK_INACTIVE("A0005"),
    NO_ACTIVE_LOAN("A0006"),
    SYSTEM_ERROR("B0000"),
    CONSISTENCY_ERROR("B0001");

    private final String value;

    BusinessCodeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
