package com.example.library.api.dto;

public class GetBooksSearchResponseDTO extends BaseResponseDTO {
    public GetBooksSearchResponseDTO() {
    }

    public GetBooksSearchResponseDTO(String code, String message, Object data) {
        super(code, message, data);
    }
}
