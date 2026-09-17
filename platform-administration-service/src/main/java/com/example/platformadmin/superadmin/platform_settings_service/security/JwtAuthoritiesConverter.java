package com.example.platformadmin.superadmin.platform_settings_service.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;

/**
 * This class is a custom converter that converts a JWT (JSON Web Token) into a
 * collection of GrantedAuthority objects. It extracts the "roles" claim from the
 * JWT and maps each role to a SimpleGrantedAuthority, ensuring that each role
 * starts with "ROLE_". If the "roles" claim is not present or is not a collection,
 * it returns an empty list of authorities.
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Object rolesClaim = jwt.getClaims().get("roles");

        if (!(rolesClaim instanceof Collection<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(Object::toString)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();
    }

}