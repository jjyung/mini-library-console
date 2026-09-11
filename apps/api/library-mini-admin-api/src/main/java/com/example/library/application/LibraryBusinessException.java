package com.example.library.application;

import com.example.library.domain.ErrorDetail;
import org.springframework.http.HttpStatus;

import java.util.List;

public class LibraryBusinessException extends RuntimeException {
    private final String businessCode;
    private final HttpStatus httpStatus;
    private final List<ErrorDetail> details;

    public LibraryBusinessException(
            String businessCode,
            HttpStatus httpStatus,
            String message,
            List<ErrorDetail> details
    ) {
        super(message);
        this.businessCode = businessCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }
}
