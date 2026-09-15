package com.example.auth.security.oauth2;

import com.example.auth.security.user.UserPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Map;

/**
 * Wraps a UserPrincipal so it also implements OidcUser, which Spring
 * requires for OIDC logins. Delegates all OIDC-specific accessors to
 * the original DefaultOidcUser produced by OidcUserService.
 */
public class SsoOidcUser extends UserPrincipal implements OidcUser {

    private final OidcUser delegate;

    public SsoOidcUser(UserPrincipal principal, OidcUser delegate) {
        super(principal);   // copy-constructor on UserPrincipal
        this.delegate = delegate;
    }

    @Override public Map<String, Object> getClaims() { return delegate.getClaims(); }
    @Override public OidcUserInfo getUserInfo()      { return delegate.getUserInfo(); }
    @Override public OidcIdToken getIdToken()        { return delegate.getIdToken(); }
}