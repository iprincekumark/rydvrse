package com.rydvrse.auth.service;

import com.rydvrse.shared.enums.UserType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token provider — generates access and refresh tokens.
 * Access token: 15 min TTL. Refresh token: 30 day TTL.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${rydvrse.jwt.secret}")
    private String jwtSecret;

    @Value("${rydvrse.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs; // 15 minutes

    @Value("${rydvrse.jwt.refresh-token-expiration-ms:2592000000}")
    private long refreshTokenExpirationMs; // 30 days

    public String generateAccessToken(UUID userId, UserType userType, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userType", userType.name())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
