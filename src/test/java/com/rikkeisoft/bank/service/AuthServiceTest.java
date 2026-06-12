package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.config.JwtConfig;
import com.rikkeisoft.bank.dto.request.LoginRequest;
import com.rikkeisoft.bank.dto.request.RegisterRequest;
import com.rikkeisoft.bank.dto.request.TokenRefreshRequest;
import com.rikkeisoft.bank.dto.response.AuthResponse;
import com.rikkeisoft.bank.dto.response.UserResponseDto;
import com.rikkeisoft.bank.entity.KycProfile;
import com.rikkeisoft.bank.entity.Role;
import com.rikkeisoft.bank.entity.RefreshToken;
import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.repository.RefreshTokenRepository;
import com.rikkeisoft.bank.repository.RoleRepository;
import com.rikkeisoft.bank.repository.UserRepository;
import com.rikkeisoft.bank.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role role;
    private KycProfile kycProfile;

    @BeforeEach
    void setUp() {
        role = Role.builder().id(1L).name("CUSTOMER").build();
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@gmail.com")
                .phoneNumber("0987654321")
                .role(role)
                .isActive(true)
                .build();
        kycProfile = KycProfile.builder()
                .id(1L)
                .fullName("Test User")
                .user(user)
                .build();
        user.setKycProfile(kycProfile);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@gmail.com");
        request.setPhone("0987654321");
        request.setPassword("password");
        request.setFullName("Test User");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhone())).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(mock(UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("accessToken123");
        when(jwtConfig.getRefreshExpirationMs()).thenReturn(2592000000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("accessToken123", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(user.getUsername())).thenReturn(mock(UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("accessToken123");
        when(jwtConfig.getRefreshExpirationMs()).thenReturn(2592000000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken123", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void refreshToken_Success() {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("oldRefreshToken");

        RefreshToken oldToken = RefreshToken.builder()
                .token("oldRefreshToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .kycProfile(kycProfile)
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("oldRefreshToken")).thenReturn(Optional.of(oldToken));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mock(UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("newAccessToken");
        when(jwtConfig.getRefreshExpirationMs()).thenReturn(2592000000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals("oldRefreshToken", response.getRefreshToken());
        verify(refreshTokenRepository, times(1)).delete(oldToken);
    }
}
