package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.LoginRequest;
import com.rikkeisoft.bank.dto.request.RegisterRequest;
import com.rikkeisoft.bank.dto.request.ForgotPasswordRequest;
import com.rikkeisoft.bank.dto.request.ResetPasswordRequest;
import com.rikkeisoft.bank.dto.response.AuthResponse;
import com.rikkeisoft.bank.dto.request.TokenRefreshRequest;
import com.rikkeisoft.bank.entity.Role;
import com.rikkeisoft.bank.entity.KycProfile;
import com.rikkeisoft.bank.entity.RefreshToken;
import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.enums.Status;
import com.rikkeisoft.bank.repository.RoleRepository;
import com.rikkeisoft.bank.repository.RefreshTokenRepository;
import com.rikkeisoft.bank.repository.UserRepository;
import com.rikkeisoft.bank.security.JwtService;
import com.rikkeisoft.bank.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtConfig jwtConfig;


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
        RefreshToken refreshToken = createRefreshToken(kycProfile);

        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        KycProfile kycProfile = getOrCreateKycProfile(user);
        RefreshToken refreshToken = createRefreshToken(kycProfile);

        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new com.rikkeisoft.bank.exception.ResourceNotFoundException("User not found: " + request.getUsername()));

        // Generate 6-digit random OTP
        String otp = String.format("%06d", java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 999999));
        user.setResetPasswordOtp(otp);
        user.setResetPasswordOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Print to log for local testing since we don't have mail service configured
        System.out.println("=================================================");
        System.out.println("OTP FOR FORGOT PASSWORD FOR USER " + user.getUsername() + ": " + otp);
        System.out.println("=================================================");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new com.rikkeisoft.bank.exception.ResourceNotFoundException("User not found: " + request.getUsername()));

        if (user.getResetPasswordOtp() == null || !user.getResetPasswordOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        if (user.getResetPasswordOtpExpiry() == null || user.getResetPasswordOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordOtp(null);
        user.setResetPasswordOtpExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void logout(String token) {
        try {
            LocalDateTime expiryDate = jwtService.extractExpiration(token).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            long secondsUntilExpiry = java.time.Duration.between(LocalDateTime.now(), expiryDate).toSeconds();
            if (secondsUntilExpiry > 0) {
                stringRedisTemplate.opsForValue().set("blacklist:" + token, "true", secondsUntilExpiry, TimeUnit.SECONDS);
            } else {
                stringRedisTemplate.opsForValue().set("blacklist:" + token, "true", 60, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            stringRedisTemplate.opsForValue().set("blacklist:" + token, "true", 3600, TimeUnit.SECONDS);
        }

        try {
            String username = jwtService.extractUsername(token);
            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.getKycProfile() != null && user.getKycProfile().getRefreshToken() != null) {
                    refreshTokenRepository.delete(user.getKycProfile().getRefreshToken());
                    user.getKycProfile().setRefreshToken(null);
                }
            });
        } catch (Exception e) {
            // Ignore
        }
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));

        if (token.isRevoked()) {
            throw new IllegalArgumentException("Refresh token was revoked!");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token was expired. Please make a new signin request");
        }

        User user = token.getKycProfile().getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        KycProfile kycProfile = token.getKycProfile();
        refreshTokenRepository.delete(token);
        refreshTokenRepository.flush();
        kycProfile.setRefreshToken(null);

        RefreshToken newRefreshToken = createRefreshToken(kycProfile);

        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(userDetails))
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();
    }

    @Transactional
    public RefreshToken createRefreshToken(KycProfile kycProfile) {
        if (kycProfile.getRefreshToken() != null) {
            refreshTokenRepository.delete(kycProfile.getRefreshToken());
            kycProfile.setRefreshToken(null);
            refreshTokenRepository.flush();
        }

        long expiryMs = jwtConfig.getRefreshExpirationMs() > 0 ? jwtConfig.getRefreshExpirationMs() : 2592000000L;
        RefreshToken refreshToken = RefreshToken.builder()
                .token(java.util.UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(expiryMs))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .kycProfile(kycProfile)
                .build();

        kycProfile.setRefreshToken(refreshToken);
        return refreshTokenRepository.save(refreshToken);
    }

    private KycProfile getOrCreateKycProfile(User user) {
        if (user.getKycProfile() == null) {
            KycProfile kycProfile = KycProfile.builder()
                    .fullName(user.getUsername())
                    .status(Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();
            user.setKycProfile(kycProfile);
            userRepository.saveAndFlush(user);
        }
        return user.getKycProfile();
    }
}
