package com.rikkeisoft.bank.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private boolean active;
}
