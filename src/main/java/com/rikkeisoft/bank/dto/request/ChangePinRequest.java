package com.rikkeisoft.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePinRequest {
    private String oldPin;

    @NotBlank
    private String newPin;
}
