package com.example.library.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetBorrowRecordsResponseDataDTO {

    private List<BorrowRecordDTO> borrowRecords;
}
