package com.example.microservice.auth.service;

import com.example.microservice.auth.entity.MfaPolicy;
import com.example.microservice.auth.repository.MfaPolicyRepository;
import org.springframework.stereotype.Service;

@Service
public class MfaPolicyService {

    private final MfaPolicyRepository mfaPolicyRepository;

    public MfaPolicyService(
            MfaPolicyRepository mfaPolicyRepository) {

        this.mfaPolicyRepository =
                mfaPolicyRepository;
    }

    // ---------------------------------------------------------------
    // Save / Update MFA Policy
    // ---------------------------------------------------------------

    public MfaPolicy savePolicy(MfaPolicy policy) {

        validatePolicy(policy);

        MfaPolicy existingPolicy =
                mfaPolicyRepository
                        .findByOrganizationId(
                                policy.getOrganizationId()
                        )
                        .orElse(null);

        if (existingPolicy != null) {

            existingPolicy.setMfaEnabled(
                    policy.isMfaEnabled()
            );

            existingPolicy.setEnforcement(
                    policy.getEnforcement()
            );

            existingPolicy.setEmailOtpEnabled(
                    policy.isEmailOtpEnabled()
            );

            existingPolicy.setSmsOtpEnabled(
                    policy.isSmsOtpEnabled()
            );

            existingPolicy.setAuthenticatorAppEnabled(
                    policy.isAuthenticatorAppEnabled()
            );

            existingPolicy.setSecurityKeyEnabled(
                    policy.isSecurityKeyEnabled()
            );

            existingPolicy.setOtpValidityMinutes(
                    policy.getOtpValidityMinutes()
            );

            existingPolicy.setMaxVerificationAttempts(
                    policy.getMaxVerificationAttempts()
            );

            existingPolicy.setTrustedDeviceDurationDays(
                    policy.getTrustedDeviceDurationDays()
            );

            existingPolicy.setUserScope(
                    policy.getUserScope()
            );

            existingPolicy.setSelectedRoles(
                    policy.getSelectedRoles()
            );

            existingPolicy.setSelectedDepartments(
                    policy.getSelectedDepartments()
            );

            existingPolicy.setRecoveryCodesEnabled(
                    policy.isRecoveryCodesEnabled()
            );

            existingPolicy.setBackupEmail(
                    policy.getBackupEmail()
            );

            existingPolicy.setBackupMobileNumber(
                    policy.getBackupMobileNumber()
            );

            return mfaPolicyRepository.save(
                    existingPolicy
            );
        }

        return mfaPolicyRepository.save(policy);
    }

    // ---------------------------------------------------------------
    // Get MFA Policy
    // ---------------------------------------------------------------

    public MfaPolicy getPolicy(
            String organizationId) {

        return mfaPolicyRepository
                .findByOrganizationId(
                        organizationId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "MFA policy not found for organization: "
                                        + organizationId
                        )
                );
    }

    // ---------------------------------------------------------------
    // Check Policy Exists
    // ---------------------------------------------------------------

    public boolean policyExists(
            String organizationId) {

        return mfaPolicyRepository
                .findByOrganizationId(
                        organizationId
                )
                .isPresent();
    }

    // ---------------------------------------------------------------
    // Delete MFA Policy
    // ---------------------------------------------------------------

    public void deletePolicy(
            String organizationId) {

        MfaPolicy policy =
                getPolicy(organizationId);

        mfaPolicyRepository.delete(policy);
    }

    // ---------------------------------------------------------------
    // Validate MFA Policy
    // ---------------------------------------------------------------

    private void validatePolicy(
            MfaPolicy policy) {

        if (policy == null) {
            throw new IllegalArgumentException(
                    "MFA policy cannot be null"
            );
        }

        // Organization mandatory
        if (policy.getOrganizationId() == null ||
                policy.getOrganizationId().isBlank()) {

            throw new IllegalArgumentException(
                    "Organization ID is mandatory"
            );
        }

        // MFA status mandatory
        if (policy.isMfaEnabled()) {

            // At least one authentication method
            boolean methodSelected =
                    policy.isEmailOtpEnabled()
                            || policy.isSmsOtpEnabled()
                            || policy.isAuthenticatorAppEnabled()
                            || policy.isSecurityKeyEnabled();

            if (!methodSelected) {

                throw new IllegalArgumentException(
                        "At least one authentication method "
                                + "must be enabled when MFA is enabled"
                );
            }
        }

        // OTP validity: 1 - 15 minutes
        if (policy.getOtpValidityMinutes() < 1 ||
                policy.getOtpValidityMinutes() > 15) {

            throw new IllegalArgumentException(
                    "OTP validity must be between 1 and 15 minutes"
            );
        }

        // Maximum attempts: 3 - 10
        if (policy.getMaxVerificationAttempts() < 3 ||
                policy.getMaxVerificationAttempts() > 10) {

            throw new IllegalArgumentException(
                    "Maximum verification attempts "
                            + "must be between 3 and 10"
            );
        }

        // Trusted device duration > 0
        if (policy.getTrustedDeviceDurationDays() <= 0) {

            throw new IllegalArgumentException(
                    "Trusted device duration must be greater than 0"
            );
        }

        // User scope mandatory when MFA is enabled
        if (policy.isMfaEnabled()) {

            if (policy.getUserScope() == null ||
                    policy.getUserScope().isBlank()) {

                throw new IllegalArgumentException(
                        "User scope is mandatory when MFA is enabled"
                );
            }

            String userScope =
                    policy.getUserScope()
                            .trim()
                            .toUpperCase();

            if (!userScope.equals("ALL_USERS") &&
                    !userScope.equals("SELECTED_ROLES") &&
                    !userScope.equals("SELECTED_DEPARTMENTS")) {

                throw new IllegalArgumentException(
                        "User scope must be ALL_USERS, "
                                + "SELECTED_ROLES, or "
                                + "SELECTED_DEPARTMENTS"
                );
            }

            // Selected roles required
            if (userScope.equals("SELECTED_ROLES")) {

                if (policy.getSelectedRoles() == null ||
                        policy.getSelectedRoles().isBlank()) {

                    throw new IllegalArgumentException(
                            "Selected roles are required "
                                    + "when user scope is SELECTED_ROLES"
                    );
                }
            }

            // Selected departments required
            if (userScope.equals("SELECTED_DEPARTMENTS")) {

                if (policy.getSelectedDepartments() == null ||
                        policy.getSelectedDepartments().isBlank()) {

                    throw new IllegalArgumentException(
                            "Selected departments are required "
                                    + "when user scope is SELECTED_DEPARTMENTS"
                    );
                }
            }
        }

        // Enforcement validation
        if (policy.getEnforcement() == null ||
                policy.getEnforcement().isBlank()) {

            throw new IllegalArgumentException(
                    "Enforcement is mandatory"
            );
        }

        String enforcement =
                policy.getEnforcement()
                        .trim()
                        .toUpperCase();

        if (!enforcement.equals("MANDATORY") &&
                !enforcement.equals("OPTIONAL")) {

            throw new IllegalArgumentException(
                    "Enforcement must be MANDATORY or OPTIONAL"
            );
        }
    }
}


