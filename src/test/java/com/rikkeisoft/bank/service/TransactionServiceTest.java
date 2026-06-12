package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.TransferRequest;
import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.entity.Account;
import com.rikkeisoft.bank.entity.Transaction;
import com.rikkeisoft.bank.exception.InsufficientBalanceException;
import com.rikkeisoft.bank.repository.AccountRepository;
import com.rikkeisoft.bank.repository.TransactionRepository;
import com.rikkeisoft.bank.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = Account.builder()
                .id(1L)
                .accountNumber("RB1111")
                .balance(new BigDecimal("500.00"))
                .active(true)
                .build();

        toAccount = Account.builder()
                .id(2L)
                .accountNumber("RB2222")
                .balance(new BigDecimal("100.00"))
                .active(true)
                .build();
    }

    @Test
    void transfer_Success() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("RB1111");
        request.setToAccountNumber("RB2222");
        request.setAmount(new BigDecimal("200.00"));
        request.setDescription("Test Transfer");

        when(accountService.findByAccountNumber("RB1111")).thenReturn(fromAccount);
        when(accountService.findByAccountNumber("RB2222")).thenReturn(toAccount);
        when(accountRepository.save(fromAccount)).thenReturn(fromAccount);
        when(accountRepository.save(toAccount)).thenReturn(toAccount);

        Transaction mockTransaction = Transaction.builder()
                .id(100L)
                .transactionCode("TX123")
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .build();

        TransactionResponseDto responseDto = TransactionResponseDto.builder()
                .id(100L)
                .transactionCode("TX123")
                .fromAccountNumber("RB1111")
                .toAccountNumber("RB2222")
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(transactionMapper.toDto(any(Transaction.class))).thenReturn(responseDto);

        TransactionResponseDto result = transactionService.transfer(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("300.00"), toAccount.getBalance());
        assertEquals("RB1111", result.getFromAccountNumber());
        assertEquals("RB2222", result.getToAccountNumber());
        assertEquals("SUCCESS", result.getStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_InsufficientBalance() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("RB1111");
        request.setToAccountNumber("RB2222");
        request.setAmount(new BigDecimal("1000.00")); // exceeds balance of 500
        request.setDescription("Fail Transfer");

        when(accountService.findByAccountNumber("RB1111")).thenReturn(fromAccount);
        when(accountService.findByAccountNumber("RB2222")).thenReturn(toAccount);

        assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.transfer(request);
        });

        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
