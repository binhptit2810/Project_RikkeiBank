package com.rikkeisoft.bank.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterbankTransferRequest {
    @NotBlank
    private String fromAccountNumber;

    @NotBlank
    private String toAccountNumber;

    @NotBlank
    private String bankName;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String description;

    @NotBlank
    private String transactionPin;
}
