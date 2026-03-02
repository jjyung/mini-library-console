package com.example.library.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBooksRequestDTO {

    @Size(max = 120)
    private String titleKeyword;

    private String status;
}
