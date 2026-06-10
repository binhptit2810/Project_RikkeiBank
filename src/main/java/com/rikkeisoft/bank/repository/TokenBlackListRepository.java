package com.rikkeisoft.bank.repository;

import com.rikkeisoft.bank.entity.TokenBlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlackListRepository extends JpaRepository<TokenBlackList, Long> {
    boolean existsByAccessToken(String accessToken);
}
