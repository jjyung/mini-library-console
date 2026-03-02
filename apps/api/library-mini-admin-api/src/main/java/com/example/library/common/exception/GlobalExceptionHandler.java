package com.example.library.common.exception;

import com.example.library.common.BusinessCodeEnum;
import com.example.library.common.dto.ApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBusinessException(BusinessException businessException) {
        return ResponseEntity.status(businessException.getHttpStatus())
                .body(ApiResponseDTO.failure(businessException.getBusinessCodeEnum(), businessException.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException methodArgumentNotValidException) {
        String errorMessage = "Invalid request.";
        FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
        if (fieldError != null) {
            errorMessage = fieldError.getField() + " " + fieldError.getDefaultMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.failure(BusinessCodeEnum.CLIENT_ERROR, errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException httpMessageNotReadableException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.failure(BusinessCodeEnum.CLIENT_ERROR, "Invalid request payload."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleDataAccessException(DataAccessException dataAccessException) {
        LOGGER.error("Database error occurred.", dataAccessException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.failure(BusinessCodeEnum.SYSTEM_ERROR, "Database operation failed."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleRuntimeException(RuntimeException runtimeException) {
        LOGGER.error("Unhandled runtime exception occurred.", runtimeException);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.failure(BusinessCodeEnum.SYSTEM_ERROR, "Internal server error."));
    }
}
