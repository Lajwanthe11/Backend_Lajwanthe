package com.example.microservice.auth.security.user;

import com.example.microservice.common.tenant.TenantContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Universal UserPrincipal implementing both UserDetails and OAuth2User with Multi-Tenancy support.
 */
public class UserPrincipal implements UserDetails, OAuth2User {

    private final String id;
    private final String username;
    private final String email;
    private final String password;
    private final String tenantId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public UserPrincipal(String id, String username, String email, String password, String tenantId,
                         Collection<? extends GrantedAuthority> authorities,
                         Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.tenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : TenantContext.DEFAULT_TENANT_ID;
        this.authorities = authorities;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    public static UserPrincipalBuilder builder() {
        return new UserPrincipalBuilder();
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getTenantId() {
        return tenantId;
    }

    public static UserPrincipal create(String username, String email, String password, List<String> roles) {
        return create(username, email, password, roles, TenantContext.getTenantId());
    }

    public static UserPrincipal create(String username, String email, String password, List<String> roles, String tenantId) {
        List<GrantedAuthority> authorities = roles == null || roles.isEmpty()
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : roles.stream().map(SimpleGrantedAuthority::new).map(GrantedAuthority.class::cast).toList();

        return UserPrincipal.builder()
                .id(username)
                .username(username)
                .email(email != null ? email : username)
                .password(password)
                .tenantId(tenantId != null ? tenantId : TenantContext.getTenantId())
                .authorities(authorities)
                .attributes(Collections.emptyMap())
                .build();
    }

    public static UserPrincipal create(OAuth2User oAuth2User, String registrationId) {
        return create(oAuth2User, registrationId, TenantContext.getTenantId());
    }

    public static UserPrincipal create(OAuth2User oAuth2User, String registrationId, String tenantId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String username;
        String email;

        if ("github".equalsIgnoreCase(registrationId)) {
            username = (String) attributes.getOrDefault("login", "github_user");
            email = (String) attributes.getOrDefault("email", username + "@github.com");
        } else {
            // Default (Google, standard OIDC)
            email = (String) attributes.getOrDefault("email", "oauth2_user");
            username = email;
        }

        return UserPrincipal.builder()
                .id(username)
                .username(username)
                .email(email)
                .password("")
                .tenantId(tenantId != null ? tenantId : TenantContext.getTenantId())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .attributes(attributes)
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static class UserPrincipalBuilder {
        private String id;
        private String username;
        private String email;
        private String password;
        private String tenantId;
        private Collection<? extends GrantedAuthority> authorities;
        private Map<String, Object> attributes;

        public UserPrincipalBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserPrincipalBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserPrincipalBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserPrincipalBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserPrincipalBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public UserPrincipalBuilder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }

        public UserPrincipalBuilder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public UserPrincipal build() {
            return new UserPrincipal(id, username, email, password, tenantId, authorities, attributes);
        }
    }
}
