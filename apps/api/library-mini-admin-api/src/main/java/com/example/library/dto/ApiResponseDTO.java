package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDTO<TData> {

    private String code;
    private String message;
    private TData data;
    private ApiErrorDTO error;

    public static <TData> ApiResponseDTO<TData> success(TData responseData) {
        return new ApiResponseDTO<>("00000", "Success", responseData, null);
    }

    public static <TData> ApiResponseDTO<TData> failure(String code, String message, String errorSubCode) {
        return new ApiResponseDTO<>(code, message, null, new ApiErrorDTO(errorSubCode, message));
    }
}
