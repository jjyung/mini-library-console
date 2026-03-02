package com.example.library.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String subCode;

    public BusinessException(String code, String subCode, String message) {
        super(message);
        this.code = code;
        this.subCode = subCode;
    }
}
