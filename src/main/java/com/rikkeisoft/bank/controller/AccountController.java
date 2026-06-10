package com.rikkeisoft.bank.controller;

import com.rikkeisoft.bank.dto.request.AccountUpdateRequest;
import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.dto.response.ApiResponse;
import com.rikkeisoft.bank.dto.response.BalanceResponseDto;
import com.rikkeisoft.bank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountResponseDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Get all accounts successfully", accountService.getAll(pageable)));
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> create(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Create account successfully", accountService.createAccount(userId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Get accounts successfully", accountService.getAccountsByUser(userId)));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<BalanceResponseDto>> getBalance(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Get account balance successfully", accountService.getBalance(accountNumber)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Update account successfully", accountService.updateAccount(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Delete account successfully", null));
    }
}
