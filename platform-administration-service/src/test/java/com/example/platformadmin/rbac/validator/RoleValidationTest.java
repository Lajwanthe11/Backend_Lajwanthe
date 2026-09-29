package com.example.platformadmin.rbac.validator;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.platformadmin.rbac.dto.request.RoleRequestDto;
import com.example.platformadmin.rbac.enums.RoleType;

class RoleValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void roleName_shouldNotBeBlank() {

        RoleRequestDto dto =
                new RoleRequestDto(
                        "",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "HR role",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("roleName"))
        );
    }

    @Test
    void roleName_shouldHaveMinimumThreeCharacters() {

        RoleRequestDto dto =
                new RoleRequestDto(
                        "HR",
                        "HR",
                        RoleType.CUSTOM,
                        "HR role",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("roleName"))
        );
    }

    @Test
    void roleName_shouldNotExceed100Characters() {

        String longRoleName =
                "A".repeat(101);

        RoleRequestDto dto =
                new RoleRequestDto(
                        longRoleName,
                        "LONG_ROLE",
                        RoleType.CUSTOM,
                        "Test role",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("roleName"))
        );
    }

    @Test
    void roleType_shouldBeRequired() {

        RoleRequestDto dto =
                new RoleRequestDto(
                        "HR Manager",
                        "HR_MANAGER",
                        null,
                        "HR role",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("roleType"))
        );
    }

    @Test
    void validRoleRequest_shouldPassValidation() {

        RoleRequestDto dto =
                new RoleRequestDto(
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "HR role",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void roleName_withExactlyThreeCharacters_shouldBeValid() {

        RoleRequestDto dto =
                new RoleRequestDto(
                        "HRM",
                        "HRM",
                        RoleType.CUSTOM,
                        "Test",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(
                violations.stream()
                        .noneMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("roleName"))
        );
    }

    @Test
    void roleName_withExactly100Characters_shouldBeValid() {

        String roleName =
                "A".repeat(100);

        RoleRequestDto dto =
                new RoleRequestDto(
                        roleName,
                        "ROLE_100",
                        RoleType.CUSTOM,
                        "Test",
                        "ACTIVE"
                );

        Set<ConstraintViolation<RoleRequestDto>> violations =
                validator.validate(dto);

        assertTrue(
                violations.stream()
                        .noneMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("roleName"))
        );
    }
}