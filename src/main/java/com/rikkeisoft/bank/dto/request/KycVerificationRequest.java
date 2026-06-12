package com.rikkeisoft.bank.dto.request;

import com.rikkeisoft.bank.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycVerificationRequest {
    @NotNull
    private Status status;
}
