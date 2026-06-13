package com.rikkeisoft.bank.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
}
