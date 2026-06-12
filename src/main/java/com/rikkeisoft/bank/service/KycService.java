package com.rikkeisoft.bank.service;

import com.cloudinary.Cloudinary;
import com.rikkeisoft.bank.entity.KycProfile;
import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.enums.Status;
import com.rikkeisoft.bank.repository.KycProfileRepository;
import com.rikkeisoft.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycService {
    private final Cloudinary cloudinary;
    private final UserService userService;
    private final UserRepository userRepository;
    private final KycProfileRepository kycProfileRepository;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Transactional
    public KycProfile submit(Long userId, String idNumber, String fullName, String dob, String sex, String address, MultipartFile frontImage)
            throws IOException {
        User user = userService.findEntityById(userId);
        KycProfile profile = kycProfileRepository.findByUserId(userId).orElseGet(KycProfile::new);
        profile.setUser(user);
        profile.setIdNumber(idNumber);
        profile.setFullName(fullName);
        if (dob != null && !dob.isBlank()) {
            profile.setDob(LocalDate.parse(dob)); // format: YYYY-MM-DD
        }
        profile.setSex(sex);
        profile.setAddress(address);
        profile.setIdCardFrontUrl(upload(frontImage));
        profile.setStatus(Status.PENDING);
        profile.setCreatedAt(profile.getCreatedAt() == null ? LocalDateTime.now() : profile.getCreatedAt());
        profile.setVerifiedAt(null); // Clear verification timestamp on new submission
        
        return kycProfileRepository.save(profile);
    }

    @Transactional
    public KycProfile verifyKyc(Long id, Status status) {
        KycProfile profile = kycProfileRepository.findById(id)
                .orElseThrow(() -> new com.rikkeisoft.bank.exception.ResourceNotFoundException("KYC Profile not found with id: " + id));

        profile.setStatus(status);
        profile.setVerifiedAt(LocalDateTime.now());

        // Update the associated User's isKyc field
        User user = profile.getUser();
        if (user != null) {
            user.setKyc(status == Status.CONFIRM);
            userRepository.save(user);
        }

        return kycProfileRepository.save(profile);
    }

    private String upload(MultipartFile file) throws IOException {
        try {
            if ("your-cloud-name".equals(cloudName) || cloudName == null || cloudName.isBlank()) {
                return uploadLocally(file);
            }
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "rikkei-bank/ekyc"));
            return result.get("secure_url").toString();
        } catch (Exception e) {
            System.err.println("Cloudinary upload failed: " + e.getMessage() + ". Falling back to local upload.");
            return uploadLocally(file);
        }
    }

    private String uploadLocally(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File uploadDir = new File("uploads").getAbsoluteFile();
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);
        return "/uploads/" + fileName;
    }
}
