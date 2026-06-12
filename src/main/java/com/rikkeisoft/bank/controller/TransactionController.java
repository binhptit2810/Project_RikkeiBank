package com.rikkeisoft.bank.controller;

import com.rikkeisoft.bank.dto.request.TransferRequest;
import com.rikkeisoft.bank.dto.request.InterbankTransferRequest;
import com.rikkeisoft.bank.dto.response.ApiResponse;
import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer successfully", transactionService.transfer(request)));
    }

    @PostMapping("/transfer-interbank")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> transferInterbank(@Valid @RequestBody InterbankTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interbank transfer successfully", transactionService.interbankTransfer(request)));
    }

    @GetMapping("/accounts/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<TransactionResponseDto>>> history(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Get transaction history successfully", transactionService.history(accountNumber, pageable)));
    }
}
