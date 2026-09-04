package com.example.library.controller;

import com.example.library.common.TraceIdService;
import com.example.library.dao.model.BorrowCommand;
import com.example.library.dao.model.LoanRecord;
import com.example.library.dao.model.ReturnCommand;
import com.example.library.generated.api.LoansApi;
import com.example.library.generated.dto.LoanDTO;
import com.example.library.generated.dto.PostLoansBorrowRequestDTO;
import com.example.library.generated.dto.PostLoansBorrowResponseDTO;
import com.example.library.generated.dto.PostLoansReturnRequestDTO;
import com.example.library.generated.dto.PostLoansReturnResponseDTO;
import com.example.library.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryLoanController implements LoansApi {

    private static final String SUCCESS_MESSAGE = "Success";

    private final LibraryService libraryService;
    private final TraceIdService traceIdService;

    public LibraryLoanController(LibraryService libraryService, TraceIdService traceIdService) {
        this.libraryService = libraryService;
        this.traceIdService = traceIdService;
    }

    @Override
    public ResponseEntity<PostLoansBorrowResponseDTO> postLoansBorrow(PostLoansBorrowRequestDTO request) {
        BorrowCommand command = new BorrowCommand(request.getReaderId(), request.getIsbn(), request.getDueDate());
        LoanDTO loan = LoanDtoMapper.toDto(libraryService.borrow(command));
        PostLoansBorrowResponseDTO response = new PostLoansBorrowResponseDTO(
                PostLoansBorrowResponseDTO.CodeEnum._00000, SUCCESS_MESSAGE, traceIdService.getOrCreateTraceId(), loan);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PostLoansReturnResponseDTO> postLoansReturn(PostLoansReturnRequestDTO request) {
        ReturnCommand command = new ReturnCommand(request.getIsbn(), request.getReaderId());
        LoanDTO loan = LoanDtoMapper.toDto(libraryService.returnLoan(command));
        PostLoansReturnResponseDTO response = new PostLoansReturnResponseDTO(
                PostLoansReturnResponseDTO.CodeEnum._00000, SUCCESS_MESSAGE, traceIdService.getOrCreateTraceId(), loan);
        return ResponseEntity.ok(response);
    }
}
