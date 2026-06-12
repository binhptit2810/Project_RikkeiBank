package com.rikkeisoft.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeisoft.bank.dto.request.TransferRequest;
import com.rikkeisoft.bank.dto.response.TransactionResponseDto;
import com.rikkeisoft.bank.security.JwtService;
import com.rikkeisoft.bank.security.UserDetailsServiceImpl;
import com.rikkeisoft.bank.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void transfer_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("RB1111");
        request.setToAccountNumber("RB2222");
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test transfer");

        TransactionResponseDto responseDto = TransactionResponseDto.builder()
                .id(1L)
                .transactionCode("TX123")
                .fromAccountNumber("RB1111")
                .toAccountNumber("RB2222")
                .amount(new BigDecimal("100.00"))
                .description("Test transfer")
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer successfully"))
                .andExpect(jsonPath("$.data.transactionCode").value("TX123"))
                .andExpect(jsonPath("$.data.amount").value(100.00));
    }
}
