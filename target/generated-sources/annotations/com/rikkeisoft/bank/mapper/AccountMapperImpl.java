package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.entity.Account;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T12:11:41+0700",
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

        accountResponseDto.id( account.getId() );
        accountResponseDto.accountNumber( account.getAccountNumber() );
        accountResponseDto.balance( account.getBalance() );
        accountResponseDto.currency( account.getCurrency() );
        accountResponseDto.active( account.isActive() );

        return accountResponseDto.build();
    }
}
