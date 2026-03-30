package com.rydvrse.auth.service;

import com.rydvrse.auth.dto.*;
import com.rydvrse.auth.entity.RefreshToken;
import com.rydvrse.auth.repository.RefreshTokenRepository;
import com.rydvrse.shared.enums.UserType;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Core authentication service.
 * OTP stored in Redis (not JPA) per spec. Phone + OTP primary, email/password secondary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final EventPublisher eventPublisher;

    @Value("${rydvrse.otp.expiration-seconds:300}")
    private int otpExpirationSeconds;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";

    /**
     * Send OTP to phone number. Stores in Redis with TTL.
     */
    public void sendOtp(OtpSendRequest request) {
        String otp = generateOtp();
        String key = OTP_KEY_PREFIX + request.getPhone() + ":" + request.getUserType();

        redisTemplate.opsForValue().set(key, otp, Duration.ofSeconds(otpExpirationSeconds));

        // In production: send via SMS gateway (Twilio, MSG91)
        log.info("OTP for {} [{}]: {} (dev mode — in production sent via SMS)",
                request.getPhone(), request.getUserType(), otp);
    }

    /**
     * Verify OTP and return JWT tokens. Auto-creates user if new.
     */
    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String key = OTP_KEY_PREFIX + request.getPhone() + ":" + request.getUserType();
        String attemptsKey = OTP_ATTEMPTS_PREFIX + request.getPhone();

        // Check attempt limit
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= 3) {
            throw new BusinessRuleException("OTP_ATTEMPTS_EXCEEDED", "Maximum OTP verification attempts exceeded");
        }

        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp == null) {
            throw new BusinessRuleException("OTP_EXPIRED", "OTP has expired or was not sent");
        }

        if (!storedOtp.equals(request.getOtp())) {
            redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, Duration.ofSeconds(otpExpirationSeconds));
            throw new UnauthorizedException("Invalid OTP");
        }

        // OTP valid — delete it
        redisTemplate.delete(key);
        redisTemplate.delete(attemptsKey);

        // For MVP: generate a deterministic userId based on phone+userType
        // In production this would look up/create a user in the customer/driver module
        UUID userId = UUID.nameUUIDFromBytes(
                (request.getPhone() + ":" + request.getUserType()).getBytes());
        boolean isNewUser = true; // Simplified for MVP

        String role = request.getUserType() == UserType.DRIVER ? "DRIVER" : "CUSTOMER";
        String accessToken = jwtTokenProvider.generateAccessToken(userId, request.getUserType(), role);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        saveRefreshToken(userId, request.getUserType(), refreshToken);

        log.info("OTP verified for {} [{}], userId={}", request.getPhone(), request.getUserType(), userId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .userType(request.getUserType())
                .build();
    }

    /**
     * Refresh access token using a valid, un-revoked refresh token.
     * Implements token rotation — old token is revoked, new one issued.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenAndIsRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        // Rotate: revoke old, issue new
        token.setIsRevoked(true);
        refreshTokenRepository.save(token);

        String role = mapUserTypeToRole(token.getUserType());
        String accessToken = jwtTokenProvider.generateAccessToken(
                token.getUserId(), token.getUserType(), role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        saveRefreshToken(token.getUserId(), token.getUserType(), newRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .isNewUser(false)
                .userType(token.getUserType())
                .build();
    }

    /**
     * Logout — revoke all refresh tokens for the user.
     */
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All sessions revoked for user {}", userId);
    }

    // ---- Private Helpers ----

    private void saveRefreshToken(UUID userId, UserType userType, String tokenStr) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .userType(userType)
                .token(tokenStr)
                .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private String mapUserTypeToRole(UserType userType) {
        return switch (userType) {
            case DRIVER -> "DRIVER";
            case ADMIN -> "SUPER_ADMIN";
            default -> "CUSTOMER";
        };
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
