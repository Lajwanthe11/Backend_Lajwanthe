package com.example.auth.security.oauth2;

import com.example.auth.security.user.UserPrincipal;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Handles OIDC providers (Google, Microsoft).
 *
 * Spring routes OIDC logins through OidcUserService, NOT OAuth2UserService.
 * Because Google's default scope includes "openid", it is always OIDC —
 * so CustomOAuth2UserService was never invoked for it, and Spring fell
 * back to DefaultOidcUserService, producing a plain DefaultOidcUser.
 */
@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final SsoUserProvisioner ssoUserProvisioner;

    public CustomOidcUserService(SsoUserProvisioner ssoUserProvisioner) {
        this.ssoUserProvisioner = ssoUserProvisioner;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // Reuse the same principal factory used by the OAuth2 path.
        // UserPrincipal.create(OAuth2User, String) accepts OidcUser because
        // OidcUser extends OAuth2User.
        UserPrincipal principal = UserPrincipal.create(oidcUser, registrationId);

        ssoUserProvisioner.provision(principal);

        // OidcUserService MUST return an OidcUser — wrap our principal so it
        // satisfies both OidcUser and UserPrincipal.
        return new SsoOidcUser(principal, oidcUser);
    }
}