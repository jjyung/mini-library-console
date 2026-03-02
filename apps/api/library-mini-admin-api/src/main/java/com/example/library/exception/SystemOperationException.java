package com.example.library.exception;

public class SystemOperationException extends RuntimeException {

    public SystemOperationException(String message, RuntimeException cause) {
        super(message, cause);
    }

    public SystemOperationException(String message) {
        super(message);
    }
}
