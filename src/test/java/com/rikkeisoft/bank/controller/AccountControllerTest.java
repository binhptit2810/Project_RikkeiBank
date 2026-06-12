package com.rikkeisoft.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeisoft.bank.dto.request.ChangePinRequest;
import com.rikkeisoft.bank.dto.response.AccountResponseDto;
import com.rikkeisoft.bank.dto.response.BalanceResponseDto;
import com.rikkeisoft.bank.security.JwtService;
import com.rikkeisoft.bank.security.UserDetailsServiceImpl;
import com.rikkeisoft.bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void getBalance_Success() throws Exception {
        BalanceResponseDto balanceDto = BalanceResponseDto.builder()
                .accountNumber("RB123")
                .balance(new BigDecimal("50000.00"))
                .currency("VND")
                .build();

        when(accountService.getBalance("RB123")).thenReturn(balanceDto);

        mockMvc.perform(get("/api/accounts/RB123/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Get account balance successfully"))
                .andExpect(jsonPath("$.data.balance").value(50000.00))
                .andExpect(jsonPath("$.data.accountNumber").value("RB123"));
    }

    @Test
    void changePin_Success() throws Exception {
        ChangePinRequest request = new ChangePinRequest();
        request.setOldPin("123456");
        request.setNewPin("654321");

        AccountResponseDto accountResponse = AccountResponseDto.builder()
                .id(1L)
                .accountNumber("RB123")
                .balance(new BigDecimal("50000.00"))
                .currency("VND")
                .active(true)
                .build();

        when(accountService.changePin(eq("RB123"), any(ChangePinRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts/RB123/change-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Change transaction PIN successfully"))
                .andExpect(jsonPath("$.data.accountNumber").value("RB123"));
    }
}
