package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.AccountUpdateRequest;
import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.dto.response.BalanceResponseDto;
import com.rikkeisoft.bank.entity.Account;
import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.exception.ResourceNotFoundException;
import com.rikkeisoft.bank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional
    public AccountResponseDto createAccount(Long userId) {
        User user = userService.findEntityById(userId);
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .active(true)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toDto(accountRepository.save(account));
    }

    public Page<AccountResponseDto> getAll(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toDto);
    }

    public BalanceResponseDto getBalance(String accountNumber) {
        Account account = findByAccountNumber(accountNumber);
        return BalanceResponseDto.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .build();
    }

    @Transactional
    public AccountResponseDto updateAccount(Long id, AccountUpdateRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        
        account.setCurrency(request.getCurrency());
        account.setActive(request.isActive());
        if (request.getTransactionPin() != null && !request.getTransactionPin().isBlank()) {
            account.setTransactionPin(request.getTransactionPin());
        }
        account.setUpdatedAt(LocalDateTime.now());
        
        return toDto(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }

    public List<AccountResponseDto> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    public AccountResponseDto toDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .active(account.isActive())
                .build();
    }

    private String generateAccountNumber() {
        return "RB" + ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L);
    }
}
