package com.rikkeisoft.bank.repository;

import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT new com.rikkeisoft.bank.dto.response.UserResponseDto(u.id, k.fullName, u.username, u.email, u.phoneNumber, r.name) " +
           "FROM User u LEFT JOIN u.kycProfile k LEFT JOIN u.role r")
    Page<UserResponseDto> findAllProjected(Pageable pageable);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
