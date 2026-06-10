package com.rikkeisoft.bank.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class BalanceResponseDto {
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
}
