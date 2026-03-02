package com.example.library.api.controller;

import com.example.library.api.dto.PostLoansRequestDTO;
import com.example.library.api.dto.PostLoansResponseDTO;
import com.example.library.api.dto.PostLoansReturnsRequestDTO;
import com.example.library.api.dto.PostLoansReturnsResponseDTO;
import com.example.library.common.ApiResponse;
import com.example.library.service.LoansService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/library/loans")
public class LoansController {
    private final LoansService loansService;

    public LoansController(LoansService loansService) {
        this.loansService = loansService;
    }

    @PostMapping
    public PostLoansResponseDTO postLoans(@Valid @RequestBody PostLoansRequestDTO requestDTO) {
        Map<String, Object> data = loansService.borrow(requestDTO);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
        return new PostLoansResponseDTO(response.getCode(), response.getMessage(), response.getData());
    }

    @PostMapping("/returns")
    public PostLoansReturnsResponseDTO postLoansReturns(@Valid @RequestBody PostLoansReturnsRequestDTO requestDTO) {
        LoansService.ReturnResult returnResult = loansService.returnBook(requestDTO);
        return new PostLoansReturnsResponseDTO(returnResult.getCode(), returnResult.getMessage(), returnResult.getData());
    }
}
