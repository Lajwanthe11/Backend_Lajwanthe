package com.example.platformadmin.organizations.user.mapper;

import com.example.platformadmin.organizations.user.dto.UserRegistrationRequestDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationResponseDto;
import com.example.platformadmin.organizations.user.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User mapToUser(
            UserRegistrationRequestDto requestDto) {

        User user = new User();

        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        user.setCompanyId(requestDto.getCompanyId());
        user.setDepartmentId(requestDto.getDepartmentId());

        return user;
    }

    public static UserRegistrationResponseDto mapToResponseDto(
            User user) {

        UserRegistrationResponseDto response =
                new UserRegistrationResponseDto();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setCompanyId(user.getCompanyId());
        response.setDepartmentId(user.getDepartmentId());
        response.setStatus(user.getStatus());

        return response;
    }
}