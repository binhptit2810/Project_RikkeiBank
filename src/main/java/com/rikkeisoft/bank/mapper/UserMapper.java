package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.UserResponseDto;
import com.rikkeisoft.bank.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "fullName", source = "kycProfile.fullName")
    @Mapping(target = "role", source = "role.name")
    UserResponseDto toDto(User user);
}
