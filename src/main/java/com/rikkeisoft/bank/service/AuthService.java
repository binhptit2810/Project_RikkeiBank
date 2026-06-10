package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.LoginRequest;
import com.rikkeisoft.bank.dto.request.RegisterRequest;
import com.rikkeisoft.bank.dto.response.AuthResponse;
import com.rikkeisoft.bank.entity.Role;
import com.rikkeisoft.bank.entity.KycProfile;
import com.rikkeisoft.bank.entity.TokenBlackList;
import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.enums.Status;
import com.rikkeisoft.bank.repository.RoleRepository;
import com.rikkeisoft.bank.repository.TokenBlackListRepository;
import com.rikkeisoft.bank.repository.UserRepository;
import com.rikkeisoft.bank.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhone())) {
            throw new IllegalArgumentException("Phone already exists");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(customerRole)
                .isActive(true)
                .isKyc(false)
                .createdAt(LocalDateTime.now())
                .build();

        KycProfile kycProfile = KycProfile.builder()
                .fullName(request.getFullName())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        user.setKycProfile(kycProfile);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(userDetails))
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(userDetails))
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();
    }

    @Transactional
    public void logout(String token) {
        LocalDateTime expiryDate = jwtService.extractExpiration(token).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        tokenBlackListRepository.save(TokenBlackList.builder()
                .accessToken(token)
                .expiryAt(expiryDate)
                .blacklistedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
