package com.example.auth.dto;

/**
 * Response DTO for a password reset request.
 *
 * NOTE: In production the reset token should be sent via email rather than
 * returned directly. The {@code resetToken} field is exposed here for
 * developer convenience while email delivery is not yet wired up.
 * TODO: Remove resetToken from response once email sender is integrated.
 */
public class PasswordResetResponseDTO {

    private String resetToken;
    private String message;
    private int expiresInMinutes;

    public PasswordResetResponseDTO() {
    }

    public PasswordResetResponseDTO(String resetToken, String message, int expiresInMinutes) {
        this.resetToken = resetToken;
        this.message = message;
        this.expiresInMinutes = expiresInMinutes;
    }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getExpiresInMinutes() { return expiresInMinutes; }
    public void setExpiresInMinutes(int expiresInMinutes) { this.expiresInMinutes = expiresInMinutes; }
}
