package com.example.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostBorrowRecordsRequestDTO {

    @Min(1)
    private Long bookId;

    @NotBlank
    @Size(max = 80)
    private String borrowerName;
}
