package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchBorrowRecordsRequestDTO {

    @NotBlank
    @Size(max = 80)
    private String returnedBy;
}
