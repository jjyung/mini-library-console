package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorDTO {

    private String errorSubCode;
    private String errorMessage;
}
