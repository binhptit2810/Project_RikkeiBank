package com.rikkeisoft.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String otp;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
