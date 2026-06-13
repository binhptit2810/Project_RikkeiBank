package com.rikkeisoft.bank.service;

import com.rikkeisoft.bank.dto.request.UserUpdateRequest;
import com.rikkeisoft.bank.dto.response.UserResponseDto;
import com.rikkeisoft.bank.entity.User;
import com.rikkeisoft.bank.exception.ResourceNotFoundException;
import com.rikkeisoft.bank.repository.UserRepository;
import com.rikkeisoft.bank.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponseDto getById(Long id) {
        return toDto(findEntityById(id));
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponseDto getByUsername(String username) {
        return toDto(findEntityByUsername(username));
    }

    public User findEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public Page<UserResponseDto> getAll(Pageable pageable) {
        return userRepository.findAllProjected(pageable);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequest request) {
        User user = findEntityById(id);
        
        // Update user properties
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setActive(request.isActive());
        
        // Update associated KycProfile's fullName
        if (user.getKycProfile() != null) {
            user.getKycProfile().setFullName(request.getFullName());
        }
        
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = findEntityByUsername(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponseDto toDto(User user) {
        return userMapper.toDto(user);
    }
}
