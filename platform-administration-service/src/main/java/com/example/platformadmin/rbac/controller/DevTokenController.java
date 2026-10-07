package com.example.platformadmin.rbac.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;


// Dev-only controller to issue a signed JWT for testing — disabled in production.
@Profile("dev")
@RestController
@RequestMapping("/api/v1/dev")
public class DevTokenController {

    // JWT secret key loaded from application config (app.jwt.secret).
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // GET /api/v1/dev/token — generates a signed JWT valid for 1 hour.
    @GetMapping("/token")
    public Map<String, String> issueDemoToken(
            @RequestParam(defaultValue = "user-hr-1") String userId,
            @RequestParam(defaultValue = "tenant-1") String tenantId) throws Exception {

        // Build token payload with userId, tenantId, issue time, and 1-hour expiry.
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId)
                .claim("userId", userId)
                .claim("tenantId", tenantId)
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3_600_000))
                .build();

        // Sign the JWT using HMAC-SHA256 with the configured secret key.
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJWT.sign(new MACSigner(jwtSecret.getBytes()));

        // Return the token string and a usage hint for attaching it to requests.
        return Map.of(
                "token", signedJWT.serialize(),
                "usage", "Authorization: Bearer <token>");
    }
}
