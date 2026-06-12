package com.rikkeisoft.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeisoft.bank.dto.request.LoginRequest;
import com.rikkeisoft.bank.dto.request.TokenRefreshRequest;
import com.rikkeisoft.bank.dto.response.AuthResponse;
import com.rikkeisoft.bank.dto.response.UserResponseDto;
import com.rikkeisoft.bank.security.JwtService;
import com.rikkeisoft.bank.security.UserDetailsServiceImpl;
import com.rikkeisoft.bank.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@gmail.com")
                .phoneNumber("0987654321")
                .role("CUSTOMER")
                .fullName("Test User")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("accessToken123")
                .refreshToken("refreshToken123")
                .tokenType("Bearer")
                .user(userDto)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken123"));
    }

    @Test
    void refresh_Success() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("refreshToken123");

        UserResponseDto userDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@gmail.com")
                .phoneNumber("0987654321")
                .role("CUSTOMER")
                .fullName("Test User")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("newAccessToken123")
                .refreshToken("newRefreshToken123")
                .tokenType("Bearer")
                .user(userDto)
                .build();

        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken123"))
                .andExpect(jsonPath("$.data.refreshToken").value("newRefreshToken123"));
    }
}
