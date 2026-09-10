package com.example.platformadmin.organizations.user.service.impl;

import com.example.common.abstracts.AbstractService;
import com.example.platformadmin.organizations.user.dto.UserProfileUpdateDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationRequestDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationResponseDto;
import com.example.platformadmin.organizations.user.entity.User;
import com.example.platformadmin.organizations.user.enums.UserStatus;
import com.example.platformadmin.organizations.user.exception.UserAlreadyExistsException;
import com.example.platformadmin.organizations.user.exception.UserNotFoundException;
import com.example.platformadmin.organizations.user.repository.UserRepository;
import com.example.platformadmin.organizations.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl
        extends AbstractService<
                User,
                Long,
                UserRegistrationRequestDto,
                UserRegistrationResponseDto>
        implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        super(userRepository, "User");
        this.userRepository = userRepository;
    }

    @Override
    protected User toEntity(UserRegistrationRequestDto request) {

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setCompanyId(request.getCompanyId());
        user.setDepartmentId(request.getDepartmentId());
        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);

        return user;
    }

    @Override
    protected UserRegistrationResponseDto toDto(User user) {

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

    @Override
    protected void updateEntityFromDto(
            User user,
            UserRegistrationRequestDto request) {

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setCompanyId(request.getCompanyId());
        user.setDepartmentId(request.getDepartmentId());
    }

    @Override
    protected void beforeCreate(
            User user,
            UserRegistrationRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: "
                            + request.getEmail());
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setDeleted(false);
    }

    @Override
    public UserRegistrationResponseDto registerUser(
            UserRegistrationRequestDto requestDto) {

        User user = toEntity(requestDto);

        beforeCreate(user, requestDto);

        User savedUser = userRepository.save(user);

        return toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRegistrationResponseDto getUserById(Long id) {

        User user = userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id));

        return toDto(user);
    }

    @Override
    public UserRegistrationResponseDto updateProfile(
            Long id,
            UserProfileUpdateDto requestDto) {

        User user = userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(requestDto.getEmail())
                && userRepository.existsByEmail(requestDto.getEmail())) {

            throw new UserAlreadyExistsException(
                    "User already exists with email: "
                            + requestDto.getEmail());
        }

        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());

        User updatedUser = userRepository.save(user);

        return toDto(updatedUser);
    }

    @Override
    public UserRegistrationResponseDto activateUser(Long id) {

        User user = getActiveUser(id);

        user.setStatus(UserStatus.ACTIVE);

        User updatedUser = userRepository.save(user);

        return toDto(updatedUser);
    }

    @Override
    public UserRegistrationResponseDto deactivateUser(Long id) {

        User user = getActiveUser(id);

        user.setStatus(UserStatus.INACTIVE);

        User updatedUser = userRepository.save(user);

        return toDto(updatedUser);
    }

    private User getActiveUser(Long id) {

        return userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id));
    }
}