package com.example.library.dto;

import java.util.List;

public class ApiResponseDTO<T> {
    private String code;
    private String message;
    private String traceId;
    private T data;
    private List<ErrorDetailDTO> details;

    public static <T> ApiResponseDTO<T> success(String code, String message, String traceId, T data) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.code = code;
        response.message = message;
        response.traceId = traceId;
        response.data = data;
        return response;
    }

    public static <T> ApiResponseDTO<T> error(String code, String message, String traceId) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.code = code;
        response.message = message;
        response.traceId = traceId;
        return response;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public List<ErrorDetailDTO> getDetails() { return details; }
    public void setDetails(List<ErrorDetailDTO> details) { this.details = details; }
}
