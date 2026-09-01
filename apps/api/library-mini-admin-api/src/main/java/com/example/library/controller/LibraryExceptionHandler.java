package com.example.library.controller;

import com.example.library.dto.ApiResponseDTO;
import com.example.library.error.BusinessCodeEnum;
import com.example.library.error.BusinessException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LibraryExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBusinessException(BusinessException exception) {
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                exception.getCode().getValue(), exception.getMessage(), UUID.randomUUID().toString());
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        ApiResponseDTO<Void> response = ApiResponseDTO.error(
                BusinessCodeEnum.INVALID_INPUT.getValue(), "Request body 格式錯誤", UUID.randomUUID().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
