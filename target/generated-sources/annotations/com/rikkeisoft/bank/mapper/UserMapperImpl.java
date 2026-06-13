package com.rikkeisoft.bank.mapper;

import com.rikkeisoft.bank.dto.response.UserResponseDto;
import com.rikkeisoft.bank.entity.KycProfile;
import com.rikkeisoft.bank.entity.Role;
import com.rikkeisoft.bank.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-13T21:23:27+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDto.UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.fullName( userKycProfileFullName( user ) );
        userResponseDto.role( userRoleName( user ) );
        userResponseDto.id( user.getId() );
        userResponseDto.username( user.getUsername() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.phoneNumber( user.getPhoneNumber() );

        return userResponseDto.build();
    }

    private String userKycProfileFullName(User user) {
        KycProfile kycProfile = user.getKycProfile();
        if ( kycProfile == null ) {
            return null;
        }
        return kycProfile.getFullName();
    }

    private String userRoleName(User user) {
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        return role.getName();
    }
}
