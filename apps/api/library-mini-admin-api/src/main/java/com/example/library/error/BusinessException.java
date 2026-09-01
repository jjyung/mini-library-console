package com.example.library.error;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final BusinessCodeEnum code;
    private final HttpStatus status;

    public BusinessException(BusinessCodeEnum code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public BusinessException(BusinessCodeEnum code, HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }

    public BusinessCodeEnum getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
