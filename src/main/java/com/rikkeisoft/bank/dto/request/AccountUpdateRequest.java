package com.rikkeisoft.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdateRequest {
    @NotBlank
    private String currency;

    private String transactionPin;

    private boolean active;
}
