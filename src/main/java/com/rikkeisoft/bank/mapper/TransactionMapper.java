package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "fromAccountNumber", source = "fromAccount.accountNumber")
    @Mapping(target = "toAccountNumber", source = "toAccount.accountNumber")
    @Mapping(target = "transactionType", ignore = true)
    TransactionResponseDto toDto(Transaction transaction);
}
