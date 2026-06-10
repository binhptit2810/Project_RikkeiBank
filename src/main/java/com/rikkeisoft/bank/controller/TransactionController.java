package com.rikkeisoft.bank.controller;

import com.rikkeisoft.bank.dto.request.TransferRequest;
import com.rikkeisoft.bank.dto.response.ApiResponse;
import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer successfully", transactionService.transfer(request)));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponseDto>>> history(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Get transaction history successfully", transactionService.history(accountNumber)));
    }
}
