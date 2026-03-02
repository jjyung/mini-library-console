package com.example.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBorrowRecordsRequestDTO {

    private String status;

    @Min(1)
    private Long bookId;

    @Size(max = 80)
    private String borrowerName;
}
