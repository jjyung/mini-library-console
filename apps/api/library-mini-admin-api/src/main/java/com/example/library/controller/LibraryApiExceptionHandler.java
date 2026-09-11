package com.example.library.controller;

import com.example.library.application.LibraryBusinessException;
import com.example.library.config.CorrelationIdFilter;
import com.example.library.domain.ErrorDetail;
import com.example.library.generated.dto.ErrorDetailResponseDTO;
import com.example.library.generated.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

import java.util.List;

@RestControllerAdvice
public class LibraryApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryApiExceptionHandler.class);
    private static final String CLIENT_ERROR_CODE = "A0000";
    private static final String SYSTEM_ERROR_CODE = "B0000";
    private static final String TRACE_ID_ATTRIBUTE = CorrelationIdFilter.TRACE_ID_ATTRIBUTE;

    @ExceptionHandler(LibraryBusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            LibraryBusinessException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                exception.getHttpStatus(),
                exception.getBusinessCode(),
                exception.getMessage(),
                exception.getDetails(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorDetail)
                .toList();
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                CLIENT_ERROR_CODE,
                "請修正輸入欄位",
                details,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = exception.getConstraintViolations().stream()
                .map(violation -> new ErrorDetail(
                        normalizeFieldName(violation.getPropertyPath().toString()),
                        violation.getMessage()
                ))
                .toList();
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                CLIENT_ERROR_CODE,
                "請修正輸入欄位",
                details,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                CLIENT_ERROR_CODE,
                "請提供符合格式的 request body",
                List.of(new ErrorDetail("body", "request body 格式不正確")),
                request
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Library persistence failure", exception);
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                SYSTEM_ERROR_CODE,
                "系統暫時無法完成操作",
                List.of(),
                request
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Unexpected library API failure", exception);
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                SYSTEM_ERROR_CODE,
                "系統暫時無法完成操作",
                List.of(),
                request
        );
    }

    private ErrorDetail toErrorDetail(FieldError fieldError) {
        return new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private String normalizeFieldName(String propertyPath) {
        int lastSeparator = propertyPath.lastIndexOf('.');
        return lastSeparator < 0 ? propertyPath : propertyPath.substring(lastSeparator + 1);
    }

    private ResponseEntity<ErrorResponseDTO> errorResponse(
            HttpStatus status,
            String businessCode,
            String message,
            List<ErrorDetail> details,
            HttpServletRequest request
    ) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                ErrorResponseDTO.CodeEnum.fromValue(businessCode),
                message,
                resolveTraceId(request)
        );
        response.setDetails(details.stream()
                .map(detail -> new ErrorDetailResponseDTO(detail.getField(), detail.getReason()))
                .toList());
        return ResponseEntity.status(status).body(response);
    }

    private String resolveTraceId(HttpServletRequest request) {
        Object traceId = request.getAttribute(TRACE_ID_ATTRIBUTE);
        if (traceId instanceof String value && !value.isBlank()) {
            return value;
        }
        String header = request.getHeader("X-Correlation-Id");
        return header == null || header.isBlank() ? java.util.UUID.randomUUID().toString() : header.trim();
    }
}
