package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.entity.Account;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-13T21:22:43+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountResponseDto toDto(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponseDto.AccountResponseDtoBuilder accountResponseDto = AccountResponseDto.builder();

        accountResponseDto.accountNumber( account.getAccountNumber() );
        accountResponseDto.active( account.isActive() );
        accountResponseDto.balance( account.getBalance() );
        accountResponseDto.currency( account.getCurrency() );
        accountResponseDto.id( account.getId() );

        return accountResponseDto.build();
    }
}
