package com.rikkeisoft.bank.repository;

import com.rikkeisoft.bank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountAccountNumberOrToAccountAccountNumberOrderByCreatedAtDesc(String fromAccountNumber, String toAccountNumber);
}
