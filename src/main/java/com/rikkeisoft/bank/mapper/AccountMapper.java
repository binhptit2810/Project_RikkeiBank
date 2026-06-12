package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountResponseDto toDto(Account account);
}
