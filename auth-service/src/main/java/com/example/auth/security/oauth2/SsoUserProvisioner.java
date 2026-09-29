package com.example.auth.security.oauth2;

import com.example.auth.security.user.CustomUserDetailsService;
import com.example.auth.security.user.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Shared "make sure this SSO principal exists in the local user store"
 * logic — used by BOTH:
 *   - CustomOAuth2UserService (GitHub, plain OAuth2)
 *   - CustomOidcUserService  (Google / Microsoft, OIDC)
 */
@Service
public class SsoUserProvisioner {

    private static final Logger log = LoggerFactory.getLogger(SsoUserProvisioner.class);

    private final CustomUserDetailsService customUserDetailsService;

    public SsoUserProvisioner(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Registers the SSO user into the shared user store on first login so they
     * exist as a real, lookup-able identity. Subsequent logins are idempotent.
     */
    public void provision(UserPrincipal principal) {
        String tenantId = principal.getTenantId();
        if (customUserDetailsService.existsByUsernameAndTenant(principal.getUsername(), tenantId)) {
            return;
        }

        String unusablePassword = generateUnusablePasswordPlaceholder();

        customUserDetailsService.registerUser(
                principal.getUsername(),
                principal.getEmail(),
                unusablePassword,
                List.of("ROLE_USER"),
                tenantId
        );

        log.info("Provisioned new SSO user '{}' (tenant '{}') on first login",
                principal.getUsername(), tenantId);
    }

    private String generateUnusablePasswordPlaceholder() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return "SSO_NO_PASSWORD_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}