package com.example.library.domain;

public enum BookStatusEnum {
    AVAILABLE("AVAILABLE", "available"),
    BORROWED("BORROWED", "borrowed"),
    INACTIVE("INACTIVE", "inactive");

    private final String databaseValue;
    private final String apiValue;

    BookStatusEnum(String databaseValue, String apiValue) {
        this.databaseValue = databaseValue;
        this.apiValue = apiValue;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static BookStatusEnum fromDatabaseValue(String value) {
        for (BookStatusEnum status : values()) {
            if (status.databaseValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalStateException("Unknown book status: " + value);
    }
}
