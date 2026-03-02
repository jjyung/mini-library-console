package com.example.library.common.exception;

import com.example.library.common.BusinessCodeEnum;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final BusinessCodeEnum businessCodeEnum;
    private final HttpStatus httpStatus;

    public BusinessException(BusinessCodeEnum businessCodeEnum, HttpStatus httpStatus, String message) {
        super(message);
        this.businessCodeEnum = businessCodeEnum;
        this.httpStatus = httpStatus;
    }

    public BusinessCodeEnum getBusinessCodeEnum() {
        return businessCodeEnum;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
