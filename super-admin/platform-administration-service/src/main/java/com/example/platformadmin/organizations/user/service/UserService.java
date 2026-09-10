package com.example.platformadmin.organizations.user.service;

import com.example.platformadmin.organizations.user.dto.UserProfileUpdateDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationRequestDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationResponseDto;

public interface UserService {

    UserRegistrationResponseDto registerUser(
            UserRegistrationRequestDto requestDto);

    UserRegistrationResponseDto getUserById(Long id);

    UserRegistrationResponseDto updateProfile(
            Long id,
            UserProfileUpdateDto requestDto);

    UserRegistrationResponseDto activateUser(Long id);

    UserRegistrationResponseDto deactivateUser(Long id);
}