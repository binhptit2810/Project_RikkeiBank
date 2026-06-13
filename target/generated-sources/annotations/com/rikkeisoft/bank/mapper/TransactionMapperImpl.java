package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.entity.Account;
import com.rikkeisoft.bank.entity.Transaction;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-13T21:23:36+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponseDto toDto(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponseDto.TransactionResponseDtoBuilder transactionResponseDto = TransactionResponseDto.builder();

        transactionResponseDto.fromAccountNumber( transactionFromAccountAccountNumber( transaction ) );
        transactionResponseDto.toAccountNumber( transactionToAccountAccountNumber( transaction ) );
        transactionResponseDto.id( transaction.getId() );
        transactionResponseDto.transactionCode( transaction.getTransactionCode() );
        transactionResponseDto.externalAccountNumber( transaction.getExternalAccountNumber() );
        transactionResponseDto.externalBankName( transaction.getExternalBankName() );
        transactionResponseDto.amount( transaction.getAmount() );
        transactionResponseDto.description( transaction.getDescription() );
        transactionResponseDto.status( transaction.getStatus() );
        transactionResponseDto.createdAt( transaction.getCreatedAt() );

        return transactionResponseDto.build();
    }

    private String transactionFromAccountAccountNumber(Transaction transaction) {
        Account fromAccount = transaction.getFromAccount();
        if ( fromAccount == null ) {
            return null;
        }
        return fromAccount.getAccountNumber();
    }

    private String transactionToAccountAccountNumber(Transaction transaction) {
        Account toAccount = transaction.getToAccount();
        if ( toAccount == null ) {
            return null;
        }
        return toAccount.getAccountNumber();
    }
}
