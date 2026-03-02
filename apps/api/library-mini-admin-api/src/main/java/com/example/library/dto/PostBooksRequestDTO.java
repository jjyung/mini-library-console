package com.example.library.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostBooksRequestDTO {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 80)
    private String author;

    @Min(1)
    @Max(9999)
    private Integer totalCopies;
}
