package com.rikkeisoft.bank.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponseDto {
    private Long id;
    private String transactionCode;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String externalAccountNumber;
    private String externalBankName;
    private BigDecimal amount;
    private String description;
    private String status;
    private String transactionType;
    private LocalDateTime createdAt;
}
