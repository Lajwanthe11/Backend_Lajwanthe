package com.example.auth.service;

import com.example.auth.dto.PasswordResetConfirmDTO;
import com.example.auth.dto.PasswordResetRequestDTO;
import com.example.auth.dto.PasswordResetResponseDTO;
import com.example.auth.security.user.CustomUserDetailsService;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the two-step password reset flow:
 * <ol>
 *   <li>{@link #generateResetToken} — validates the user exists, mints a UUID reset token
 *       with a 15-minute TTL, and returns it. In production this token should be emailed;
 *       for now it is returned directly in the API response.</li>
 *   <li>{@link #resetPassword} — validates the reset token, re-encodes the new password,
 *       and delegates the update to {@link CustomUserDetailsService}.</li>
 * </ol>
 *
 * NOTE: This is an in-memory implementation. Tokens are lost on restart.
 * TODO: Persist reset tokens to the database for production use.
 * TODO: Send reset token via email instead of returning it in the response.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int RESET_TOKEN_TTL_MINUTES = 15;

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    /** In-memory store: resetToken → ResetEntry. */
    private final Map<String, ResetEntry> tokenStore = new ConcurrentHashMap<>();

    public PasswordResetService(CustomUserDetailsService customUserDetailsService,
                                PasswordEncoder passwordEncoder,
                                PasswordValidator passwordValidator) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    // ---------------------------------------------------------------
    // Step 1: Request reset token
    // ---------------------------------------------------------------

    /**
     * Generate a password reset token for the given user.
     *
     * @param request contains username (required) and optional tenantId.
     * @return PasswordResetResponseDTO with the reset token and its TTL.
     * @throws ResourceNotFoundException if no user exists with that username/tenant combination.
     */
    public PasswordResetResponseDTO generateResetToken(PasswordResetRequestDTO request) {
        String tenantId = StringUtils.hasText(request.getTenantId())
                ? request.getTenantId()
                : TenantContext.getTenantId();

        if (!customUserDetailsService.existsByUsernameAndTenant(request.getUsername(), tenantId)) {
            throw new ResourceNotFoundException("User", "username", request.getUsername());
        }

        // Evict any existing reset token for this user before issuing a new one
        tokenStore.values().removeIf(e ->
                e.username.equals(request.getUsername()) && e.tenantId.equals(tenantId));

        String resetToken = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(RESET_TOKEN_TTL_MINUTES, ChronoUnit.MINUTES);
        tokenStore.put(resetToken, new ResetEntry(request.getUsername(), tenantId, expiry));

        log.info("Password reset token generated for user '{}' in tenant '{}'",
                request.getUsername(), tenantId);

        // TODO: Send resetToken via email instead of returning it in the response.
        return new PasswordResetResponseDTO(
                resetToken,
                "Password reset token generated. Use it at POST /auth/password-reset/confirm within "
                        + RESET_TOKEN_TTL_MINUTES + " minutes.",
                RESET_TOKEN_TTL_MINUTES
        );
    }

    // ---------------------------------------------------------------
    // Step 2: Confirm reset with token + new password
    // ---------------------------------------------------------------

    /**
     * Validate the reset token and update the user's password.
     *
     * @param request contains resetToken and newPassword.
     * @throws BadRequestException if the token is missing, expired, or not found,
         *                             or if the new password violates the password policy.
     */
    public void resetPassword(PasswordResetConfirmDTO request) {
        ResetEntry entry = tokenStore.get(request.getResetToken());

        if (entry == null) {
            throw new BadRequestException("Invalid or unknown password reset token.");
        }
        if (Instant.now().isAfter(entry.expiry)) {
            tokenStore.remove(request.getResetToken());
            throw new BadRequestException("Password reset token has expired. Please request a new one.");
        }

        // Apply the same policy that registration enforces. Checked after the
        // token checks so a rejected password does not consume the token —
        // the caller can retry with the same one.
        passwordValidator.validate(request.getNewPassword());

        TenantContext.setTenantId(entry.tenantId);
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        customUserDetailsService.updatePassword(entry.username, entry.tenantId, encodedPassword);

        // Invalidate the reset token immediately after use
        tokenStore.remove(request.getResetToken());

        log.info("Password successfully reset for user '{}' in tenant '{}'",
                entry.username, entry.tenantId);
    }

    // ---------------------------------------------------------------
    // Internal record
    // ---------------------------------------------------------------

    private record ResetEntry(String username, String tenantId, Instant expiry) {
    }
}
