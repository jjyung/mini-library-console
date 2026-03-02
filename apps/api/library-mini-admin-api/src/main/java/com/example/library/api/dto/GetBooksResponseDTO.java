package com.example.library.api.dto;

public class GetBooksResponseDTO extends BaseResponseDTO {
    public GetBooksResponseDTO() {
    }

    public GetBooksResponseDTO(String code, String message, Object data) {
        super(code, message, data);
    }
}
