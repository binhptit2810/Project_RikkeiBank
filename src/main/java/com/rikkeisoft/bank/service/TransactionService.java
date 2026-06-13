package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.TransferRequest;
import com.rikkeisoft.bank.dto.request.InterbankTransferRequest;
import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.entity.Account;
import com.rikkeisoft.bank.entity.Transaction;
import com.rikkeisoft.bank.exception.InsufficientBalanceException;
import com.rikkeisoft.bank.repository.AccountRepository;
import com.rikkeisoft.bank.repository.TransactionRepository;
import com.rikkeisoft.bank.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponseDto transfer(TransferRequest request) {
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        Account fromAccount = accountService.findByAccountNumber(request.getFromAccountNumber());
        Account toAccount = accountService.findByAccountNumber(request.getToAccountNumber());

        if (!fromAccount.isActive()) {
            throw new IllegalArgumentException("Source account is inactive");
        }
        if (!toAccount.isActive()) {
            throw new IllegalArgumentException("Destination account is inactive");
        }

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

    @Transactional
    public TransactionResponseDto interbankTransfer(InterbankTransferRequest request) {
        Account fromAccount = accountService.findByAccountNumber(request.getFromAccountNumber());

        // Verify transaction PIN
        if (fromAccount.getTransactionPin() == null || !fromAccount.getTransactionPin().equals(request.getTransactionPin())) {
            throw new IllegalArgumentException("Incorrect transaction PIN");
        }

        // Verify active state
        if (!fromAccount.isActive()) {
            throw new IllegalArgumentException("Source account is inactive");
        }

        // Verify balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(fromAccount);

        String transactionCode = "TX" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Transaction transaction = Transaction.builder()
                .transactionCode(transactionCode)
                .fromAccount(fromAccount)
                .toAccount(null)
                .externalAccountNumber(request.getToAccountNumber())
                .externalBankName(request.getBankName())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        return toDto(transactionRepository.save(transaction));
    }

    public Page<TransactionResponseDto> history(String accountNumber, Pageable pageable) {
        return transactionRepository
                .findByFromAccountAccountNumberOrToAccountAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber, pageable)
                .map(transaction -> {
                    TransactionResponseDto dto = toDto(transaction);
                    if (transaction.getFromAccount() != null && accountNumber.equals(transaction.getFromAccount().getAccountNumber())) {
                        dto.setTransactionType("DEBIT");
                    } else if (transaction.getToAccount() != null && accountNumber.equals(transaction.getToAccount().getAccountNumber())) {
                        dto.setTransactionType("CREDIT");
                    } else {
                        dto.setTransactionType("DEBIT");
                    }
                    return dto;
                });
    }

    public TransactionResponseDto toDto(Transaction transaction) {
        return transactionMapper.toDto(transaction);
    }
}
