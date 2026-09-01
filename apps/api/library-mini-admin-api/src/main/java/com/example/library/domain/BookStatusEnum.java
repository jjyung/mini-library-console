package com.example.library.domain;

public enum BookStatusEnum {
    AVAILABLE("available"),
    BORROWED("borrowed"),
    INACTIVE("inactive");

    private final String value;

    BookStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
