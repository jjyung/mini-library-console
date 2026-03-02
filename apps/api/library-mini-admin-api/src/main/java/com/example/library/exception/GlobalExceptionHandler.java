package com.example.library.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.library.dto.ApiResponseDTO;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.ok(ApiResponseDTO.failure(exception.getCode(), exception.getMessage(), exception.getSubCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String validationMessage = exception.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? "Request validation failed." : error.getDefaultMessage())
            .orElse("Request validation failed.");
        return ResponseEntity.ok(ApiResponseDTO.failure("A0000", validationMessage, "A0001"));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBindException(BindException exception) {
        String validationMessage = exception.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? "Request validation failed." : error.getDefaultMessage())
            .orElse("Request validation failed.");
        return ResponseEntity.ok(ApiResponseDTO.failure("A0000", validationMessage, "A0001"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.ok(ApiResponseDTO.failure("A0000", exception.getMessage(), "A0001"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.ok(ApiResponseDTO.failure("A0000", exception.getMessage(), "A0002"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRuntimeException(RuntimeException exception) {
        LOGGER.error("Unhandled runtime exception.", exception);
        return ResponseEntity.ok(ApiResponseDTO.failure("B0000", "System error", "B0000"));
    }
}
