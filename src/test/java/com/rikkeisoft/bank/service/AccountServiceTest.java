package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.ChangePinRequest;
import com.rikkeisoft.bank.dto.response.BalanceResponseDto;
import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.entity.Account;
import com.rikkeisoft.bank.exception.ResourceNotFoundException;
import com.rikkeisoft.bank.repository.AccountRepository;
import com.rikkeisoft.bank.mapper.AccountMapper;
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
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountNumber("RB1234567890")
                .balance(new BigDecimal("100000.00"))
                .currency("VND")
                .active(true)
                .transactionPin("123456")
                .build();
    }

    @Test
    void getBalance_Success() {
        when(accountRepository.findByAccountNumber("RB1234567890")).thenReturn(Optional.of(account));

        BalanceResponseDto result = accountService.getBalance("RB1234567890");

        assertNotNull(result);
        assertEquals("RB1234567890", result.getAccountNumber());
        assertEquals(new BigDecimal("100000.00"), result.getBalance());
        assertEquals("VND", result.getCurrency());
    }

    @Test
    void changePin_Success() {
        ChangePinRequest request = new ChangePinRequest();
        request.setOldPin("123456");
        request.setNewPin("654321");

        when(accountRepository.findByAccountNumber("RB1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toDto(any(Account.class))).thenReturn(AccountResponseDto.builder().accountNumber("RB1234567890").build());

        AccountResponseDto result = accountService.changePin("RB1234567890", request);

        assertNotNull(result);
        assertEquals("654321", account.getTransactionPin());
        verify(accountRepository, times(1)).save(account);
    }
}
