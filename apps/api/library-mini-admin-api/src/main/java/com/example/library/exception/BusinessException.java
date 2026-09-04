package com.example.library.exception;

public class BusinessException extends RuntimeException {

    private final String field;
    private final String reason;

    public BusinessException(String message) {
        this(message, null, null);
    }

    public BusinessException(String message, String field, String reason) {
        super(message);
        this.field = field;
        this.reason = reason;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.reason = null;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}
