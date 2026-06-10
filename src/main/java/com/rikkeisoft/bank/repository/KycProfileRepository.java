package com.rikkeisoft.bank.repository;

import com.rikkeisoft.bank.entity.KycProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    Optional<KycProfile> findByUserId(Long userId);
}
