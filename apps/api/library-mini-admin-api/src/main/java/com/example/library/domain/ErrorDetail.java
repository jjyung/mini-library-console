package com.example.library.domain;

public class ErrorDetail {
    private String field;
    private String reason;

    public ErrorDetail(String field, String reason) {
        this.field = field;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}
