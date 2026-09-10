package com.example.platformadmin.organizations.user.controller;

import com.example.platformadmin.organizations.user.dto.UserProfileUpdateDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationRequestDto;
import com.example.platformadmin.organizations.user.dto.UserRegistrationResponseDto;
import com.example.platformadmin.organizations.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> registerUser(
            @Valid @RequestBody UserRegistrationRequestDto requestDto) {

        UserRegistrationResponseDto responseDto =
                userService.registerUser(requestDto);

        return new ResponseEntity<>(
                responseDto,
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRegistrationResponseDto> getUserById(
            @PathVariable Long id) {

        UserRegistrationResponseDto responseDto =
                userService.getUserById(id);

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserRegistrationResponseDto> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileUpdateDto requestDto) {

        UserRegistrationResponseDto responseDto =
                userService.updateProfile(id, requestDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/activate/{id}")
    public ResponseEntity<UserRegistrationResponseDto> activateUser(
            @PathVariable Long id) {

        UserRegistrationResponseDto responseDto =
                userService.activateUser(id);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/de-activate/{id}")
    public ResponseEntity<UserRegistrationResponseDto> deactivateUser(
            @PathVariable Long id) {

        UserRegistrationResponseDto responseDto =
                userService.deactivateUser(id);

        return ResponseEntity.ok(responseDto);
    }
}