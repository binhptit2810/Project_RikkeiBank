package com.rikkeisoft.bank.controller;

import com.rikkeisoft.bank.dto.response.ApiResponse;
import com.rikkeisoft.bank.entity.KycProfile;
import com.rikkeisoft.bank.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {
    private final KycService kycService;

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycProfile>> submit(
            @RequestParam Long userId,
            @RequestParam String idNumber,
            @RequestParam String fullName,
            @RequestParam String dob,
            @RequestParam String sex,
            @RequestParam String address,
            @RequestParam MultipartFile frontImage
    ) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(
                "Submit KYC successfully",
                kycService.submit(userId, idNumber, fullName, dob, sex, address, frontImage)
        ));
    }
}
