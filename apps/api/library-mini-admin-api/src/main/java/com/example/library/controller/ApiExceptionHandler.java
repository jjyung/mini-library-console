package com.example.library.controller;

import com.example.library.common.TraceIdService;
import com.example.library.exception.BusinessException;
import com.example.library.generated.dto.ErrorDetailDTO;
import com.example.library.generated.dto.ErrorResponseDTO;
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
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String VALIDATION_MESSAGE = "Request validation failed.";
    private static final String SYSTEM_MESSAGE = "Internal server error.";

    private final TraceIdService traceIdService;

    public ApiExceptionHandler(TraceIdService traceIdService) {
        this.traceIdService = traceIdService;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException exception) {
        List<ErrorDetailDTO> details = exception.getField() == null
                ? List.of()
                : List.of(new ErrorDetailDTO(exception.getField(), exception.getReason()));
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorResponseDTO.CodeEnum.A0000, exception.getMessage(), details);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException exception) {
        List<ErrorDetailDTO> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorDetail)
                .toList();
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorResponseDTO.CodeEnum.A0000, VALIDATION_MESSAGE, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return errorResponse(HttpStatus.BAD_REQUEST, ErrorResponseDTO.CodeEnum.A0000, VALIDATION_MESSAGE, List.of());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseException(DataAccessException exception) {
        LOGGER.error("Database operation failed", exception);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponseDTO.CodeEnum.B0000,
                SYSTEM_MESSAGE, List.of());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedRuntimeException(RuntimeException exception) {
        LOGGER.error("Unexpected library API failure", exception);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponseDTO.CodeEnum.B0000,
                SYSTEM_MESSAGE, List.of());
    }

    private ErrorDetailDTO toErrorDetail(FieldError fieldError) {
        String reason = fieldError.getDefaultMessage() == null ? "invalid value" : fieldError.getDefaultMessage();
        return new ErrorDetailDTO(fieldError.getField(), reason);
    }

    private ResponseEntity<ErrorResponseDTO> errorResponse(HttpStatus status, ErrorResponseDTO.CodeEnum code,
                                                            String message, List<ErrorDetailDTO> details) {
        ErrorResponseDTO response = new ErrorResponseDTO(code, message, traceIdService.getOrCreateTraceId());
        response.setDetails(details);
        return ResponseEntity.status(status).body(response);
    }
}
