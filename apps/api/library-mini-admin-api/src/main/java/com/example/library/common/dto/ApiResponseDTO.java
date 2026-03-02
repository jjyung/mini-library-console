package com.example.library.common.dto;

import com.example.library.common.BusinessCodeEnum;

public class ApiResponseDTO<TData> {

    private String code;
    private String message;
    private TData data;

    public ApiResponseDTO() {
    }

    public ApiResponseDTO(String code, String message, TData data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <TData> ApiResponseDTO<TData> success(String message, TData data) {
        return new ApiResponseDTO<>(BusinessCodeEnum.SUCCESS.getCode(), message, data);
    }

    public static <TData> ApiResponseDTO<TData> failure(BusinessCodeEnum businessCodeEnum, String message) {
        return new ApiResponseDTO<>(businessCodeEnum.getCode(), message, null);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TData getData() {
        return data;
    }

    public void setData(TData data) {
        this.data = data;
    }
}
