////package com.example.auth.security.oauth2;
////
////import com.example.auth.security.user.UserPrincipal;
////import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
////import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
////import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
////import org.springframework.security.oauth2.core.user.OAuth2User;
////import org.springframework.stereotype.Service;
////
/////**
//// * Custom OAuth2 user service converting provider OAuth2User into the application's unified UserPrincipal.
//// */
////@Service
////public class CustomOAuth2UserService extends DefaultOAuth2UserService {
////
////    @Override
////    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
////        OAuth2User oAuth2User = super.loadUser(userRequest);
////        String registrationId = userRequest.getClientRegistration().getRegistrationId();
////        return UserPrincipal.create(oAuth2User, registrationId);
////    }
////}
//package com.example.auth.security.oauth2;
//
//import com.example.auth.security.user.CustomUserDetailsService;
//import com.example.auth.security.user.UserPrincipal;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//
//import java.security.SecureRandom;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Custom OAuth2 user service converting a provider's OAuth2User into the
// * application's unified UserPrincipal.
// *
// * On top of the default mapping, this service:
// *  1. Persists first-time SSO users into the same user store used by
// *     username/password login (CustomUserDetailsService), so OAuth2 users
// *     exist as real, lookup-able identities — e.g. for createdBy/performedBy
// *     references in other services (department, audit-log, etc.).
// *  2. For GitHub, if the primary email is not public (GitHub's /user API
// *     omits it in that case), makes a follow-up call to GitHub's
// *     /user/emails endpoint using the OAuth2 access token to fetch the
// *     user's real verified primary email, instead of relying on a
// *     fabricated "username@github.com" placeholder.
// */
//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
//    private static final String GITHUB_EMAILS_URI = "https://api.github.com/user/emails";
//
//    private final CustomUserDetailsService customUserDetailsService;
//    private final RestTemplate restTemplate;
//
//    public CustomOAuth2UserService(CustomUserDetailsService customUserDetailsService) {
//        this.customUserDetailsService = customUserDetailsService;
//        this.restTemplate = new RestTemplate();
//    }
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        String registrationId = userRequest.getClientRegistration().getRegistrationId();
//
//        Map<String, Object> attributes = resolveAttributes(oAuth2User, registrationId, userRequest);
//
//        UserPrincipal principal = UserPrincipal.create(
//                new OAuth2UserWithAttributes(oAuth2User, attributes), registrationId);
//        persistIfNewUser(principal);
//
//        return principal;
//    }
//
//    /**
//     * For GitHub, backfills a missing "email" attribute by calling GitHub's
//     * /user/emails endpoint with the access token. Returns the original
//     * attributes unchanged for all other providers, or if the email is
//     * already present, or if the lookup fails for any reason.
//     */
//    private Map<String, Object> resolveAttributes(OAuth2User oAuth2User, String registrationId,
//                                                  OAuth2UserRequest userRequest) {
//        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
//
//        boolean isGithub = "github".equalsIgnoreCase(registrationId);
//        Object emailAttr = attributes.get("email");
//        boolean emailMissing = emailAttr == null || String.valueOf(emailAttr).isBlank();
//
//        if (isGithub && emailMissing) {
//            String realEmail = fetchGithubPrimaryEmail(userRequest.getAccessToken().getTokenValue());
//            if (realEmail != null) {
//                attributes.put("email", realEmail);
//            } else {
//                log.warn("Could not resolve a verified email from GitHub for user '{}'; " +
//                                "falling back to placeholder email in UserPrincipal.",
//                        attributes.getOrDefault("login", "unknown"));
//            }
//        }
//
//        return attributes;
//    }
//
//    private String fetchGithubPrimaryEmail(String accessToken) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(accessToken);
//            headers.set("Accept", "application/vnd.github+json");
//
//            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
//                    GITHUB_EMAILS_URI,
//                    HttpMethod.GET,
//                    new HttpEntity<>(headers),
//                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
//            );
//
//            List<Map<String, Object>> emails = response.getBody();
//            if (emails == null || emails.isEmpty()) {
//                return null;
//            }
//
//            return emails.stream()
//                    .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
//                    .map(e -> (String) e.get("email"))
//                    .findFirst()
//                    .orElseGet(() -> emails.stream()
//                            .filter(e -> Boolean.TRUE.equals(e.get("verified")))
//                            .map(e -> (String) e.get("email"))
//                            .findFirst()
//                            .orElse(null));
//
//        } catch (RestClientException ex) {
//            log.warn("GitHub /user/emails lookup failed: {}", ex.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * Registers the SSO user into the shared user store on first login so they
//     * exist as a real, lookup-able identity (consistent with username/password
//     * users). Subsequent logins are idempotent no-ops.
//     *
//     * NOTE: this reuses the existing in-memory CustomUserDetailsService store —
//     * there is currently no separate DB-backed User entity in auth-service.
//     * If/when one is introduced, replace this call with a real
//     * UserRepository.findOrCreate(...) equivalent.
//     */
//    private void persistIfNewUser(UserPrincipal principal) {
//        String tenantId = principal.getTenantId();
//        if (customUserDetailsService.existsByUsernameAndTenant(principal.getUsername(), tenantId)) {
//            return;
//        }
//
//        // SSO users authenticate via the identity provider, never via local password,
//        // so store a random, unguessable, unusable placeholder hash rather than a
//        // blank password (a blank/empty stored hash is a common source of auth
//        // bypass bugs if this store is ever compared against directly).
//        String unusablePassword = generateUnusablePasswordPlaceholder();
//
//        customUserDetailsService.registerUser(
//                principal.getUsername(),
//                principal.getEmail(),
//                unusablePassword,
//                List.of("ROLE_USER"),
//                tenantId
//        );
//
//        log.info("Provisioned new SSO user '{}' (tenant '{}') on first login",
//                principal.getUsername(), tenantId);
//    }
//
//    private String generateUnusablePasswordPlaceholder() {
//        byte[] randomBytes = new byte[32];
//        new SecureRandom().nextBytes(randomBytes);
//        // Not a valid BCrypt hash format, so it can never match any real login attempt
//        // even if password-based auth is accidentally wired against this record.
//        return "SSO_NO_PASSWORD_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
//    }
//
//    /**
//     * Thin wrapper so we can hand UserPrincipal.create(...) a version of the
//     * OAuth2User with the (possibly GitHub-email-backfilled) attributes map,
//     * without changing UserPrincipal's existing factory method signature.
//     */
//    private record OAuth2UserWithAttributes(OAuth2User delegate, Map<String, Object> attributes)
//            implements OAuth2User {
//
//        @Override
//        public Map<String, Object> getAttributes() {
//            return attributes;
//        }
//
//        @Override
//        public List<GrantedAuthority> getAuthorities() {
//            return List.copyOf(delegate.getAuthorities());
//        }
//
//        @Override
//        public String getName() {
//            return delegate.getName();
//        }
//    }
//}

package com.example.auth.security.oauth2;

import com.example.auth.security.user.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom OAuth2 user service converting a provider's OAuth2User into the
 * application's unified UserPrincipal.
 *
 * Used for plain-OAuth2 providers (GitHub). OIDC providers (Google,
 * Microsoft) go through CustomOidcUserService instead.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private static final String GITHUB_EMAILS_URI = "https://api.github.com/user/emails";

    private final SsoUserProvisioner ssoUserProvisioner;
    private final RestTemplate restTemplate;

    public CustomOAuth2UserService(SsoUserProvisioner ssoUserProvisioner) {
        this.ssoUserProvisioner = ssoUserProvisioner;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = resolveAttributes(oAuth2User, registrationId, userRequest);

        UserPrincipal principal = UserPrincipal.create(
                new OAuth2UserWithAttributes(oAuth2User, attributes), registrationId);

        ssoUserProvisioner.provision(principal);

        return principal;
    }

    /**
     * For GitHub, backfills a missing "email" attribute by calling GitHub's
     * /user/emails endpoint with the access token.
     */
    private Map<String, Object> resolveAttributes(OAuth2User oAuth2User, String registrationId,
                                                  OAuth2UserRequest userRequest) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        boolean isGithub = "github".equalsIgnoreCase(registrationId);
        Object emailAttr = attributes.get("email");
        boolean emailMissing = emailAttr == null || String.valueOf(emailAttr).isBlank();

        if (isGithub && emailMissing) {
            String realEmail = fetchGithubPrimaryEmail(userRequest.getAccessToken().getTokenValue());
            if (realEmail != null) {
                attributes.put("email", realEmail);
            } else {
                log.warn("Could not resolve a verified email from GitHub for user '{}'; " +
                                "falling back to placeholder email in UserPrincipal.",
                        attributes.getOrDefault("login", "unknown"));
            }
        }

        return attributes;
    }

    private String fetchGithubPrimaryEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github+json");

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    GITHUB_EMAILS_URI,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> emails = response.getBody();
            if (emails == null || emails.isEmpty()) {
                return null;
            }

            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .filter(e -> Boolean.TRUE.equals(e.get("verified")))
                            .map(e -> (String) e.get("email"))
                            .findFirst()
                            .orElse(null));

        } catch (RestClientException ex) {
            log.warn("GitHub /user/emails lookup failed: {}", ex.getMessage());
            return null;
        }
    }

    private record OAuth2UserWithAttributes(OAuth2User delegate, Map<String, Object> attributes)
            implements OAuth2User {

        @Override public Map<String, Object> getAttributes() { return attributes; }
        @Override public List<GrantedAuthority> getAuthorities() { return List.copyOf(delegate.getAuthorities()); }
        @Override public String getName() { return delegate.getName(); }
    }
}