package com.example.auth.service;

import com.example.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;


@Component
public class PasswordValidator {


    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;

    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR = Pattern.compile(".*[!@#$%].*");
    private static final Pattern WHITESPACE = Pattern.compile(".*\\s.*");

    // Only allows standard printable ASCII: letters, digits, and the
    // permitted special characters. Anything outside this set is rejected.
    private static final Pattern ALLOWED_CHARS_ONLY =
            Pattern.compile("^[A-Za-z0-9!@#$%]+$");

    public void validate(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BadRequestException("Password must not be empty");
        }
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new BadRequestException(
                    String.format("Password must be between %d and %d characters long", MIN_LENGTH, MAX_LENGTH));
        }
        if (WHITESPACE.matcher(password).matches()) {
            throw new BadRequestException("Password must not contain whitespace characters");
        }
        if (!ALLOWED_CHARS_ONLY.matcher(password).matches()) {
            throw new BadRequestException(
                    "Password must only contain ASCII letters, digits, and the allowed special characters (!@#$%)");
        }
        if (!UPPERCASE.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }
        if (!LOWERCASE.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }
        if (!DIGIT.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one numeric digit");
        }
        if (!SPECIAL_CHAR.matcher(password).matches()) {
            throw new BadRequestException("Password must contain at least one special character (!@#$%)");
        }
    }
}