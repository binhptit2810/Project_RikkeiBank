package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.TransferRequest;
import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.entity.Account;
import com.rikkeisoft.bank.entity.Transaction;
import com.rikkeisoft.bank.exception.InsufficientBalanceException;
import com.rikkeisoft.bank.repository.AccountRepository;
import com.rikkeisoft.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponseDto transfer(TransferRequest request) {
        Account fromAccount = accountService.findByAccountNumber(request.getFromAccountNumber());
        Account toAccount = accountService.findByAccountNumber(request.getToAccountNumber());

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        String transactionCode = "TX" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Transaction transaction = Transaction.builder()
                .transactionCode(transactionCode)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(transactionRepository.save(transaction));
    }

    public List<TransactionResponseDto> history(String accountNumber) {
        return transactionRepository
                .findByFromAccountAccountNumberOrToAccountAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private TransactionResponseDto toDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .transactionCode(transaction.getTransactionCode())
                .fromAccountNumber(transaction.getFromAccount().getAccountNumber())
                .toAccountNumber(transaction.getToAccount().getAccountNumber())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
