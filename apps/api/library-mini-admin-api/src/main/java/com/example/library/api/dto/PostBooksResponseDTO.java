package com.example.library.api.dto;

public class PostBooksResponseDTO extends BaseResponseDTO {
    public PostBooksResponseDTO() {
    }

    public PostBooksResponseDTO(String code, String message, Object data) {
        super(code, message, data);
    }
}
