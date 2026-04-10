package com.rydvrse.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenTtlMinutes;

    public JwtService(@Value("${rydvrse.security.jwt-secret}") String secret,
                      @Value("${rydvrse.security.access-token-ttl-minutes}") long accessTokenTtlMinutes) {
        byte[] bytes = secret.length() >= 32 ? secret.getBytes(StandardCharsets.UTF_8) : Decoders.BASE64.decode("Y2hhbmdlLW1lLWNoYW5nZS1tZS1jaGFuZ2UtbWUtY2hhbmdlLW1l");
        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public String createAccessToken(UUID userId, UUID profileId, UUID sessionId, ActorType actorType, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("profile_id", profileId == null ? null : profileId.toString())
                .claim("session_id", sessionId.toString())
                .claim("actor_type", actorType.name())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlMinutes * 60)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
