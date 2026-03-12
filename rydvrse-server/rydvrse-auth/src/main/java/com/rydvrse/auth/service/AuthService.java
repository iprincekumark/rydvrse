package com.rydvrse.auth.service;

import com.rydvrse.auth.domain.AuthUser;
import com.rydvrse.auth.domain.OtpRecord;
import com.rydvrse.auth.domain.RefreshToken;
import com.rydvrse.auth.dto.*;
import com.rydvrse.auth.event.UserRegisteredEvent;
import com.rydvrse.auth.repository.AuthUserRepository;
import com.rydvrse.auth.repository.OtpRepository;
import com.rydvrse.auth.repository.RefreshTokenRepository;
import com.rydvrse.shared.enums.UserRole;
import com.rydvrse.shared.event.EventPublisher;
import com.rydvrse.shared.exception.BusinessRuleException;
import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Core authentication service handling registration, OTP, login, and token refresh.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final OtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    @Value("${rydvrse.otp.expiration-seconds:300}")
    private int otpExpirationSeconds;

    @Value("${rydvrse.otp.length:6}")
    private int otpLength;

    /**
     * Send OTP to phone number for login/registration.
     */
    @Transactional
    public OtpResponse sendOtp(OtpRequest request) {
        String otp = generateOtp();
        OtpRecord record = OtpRecord.builder()
                .phoneNumber(request.getPhoneNumber())
                .otpCode(otp)
                .expiresAt(Instant.now().plusSeconds(otpExpirationSeconds))
                .build();
        otpRepository.save(record);

        // In production: send via SMS gateway (Twilio, MSG91, etc.)
        log.info("OTP generated for {}: {} (dev mode — in production this is sent via SMS)",
                request.getPhoneNumber(), otp);

        return OtpResponse.builder()
                .phoneNumber(request.getPhoneNumber())
                .message("OTP sent successfully")
                .expiresInSeconds(otpExpirationSeconds)
                .build();
    }

    /**
     * Verify OTP and complete login or trigger registration.
     */
    @Transactional
    public AuthResponse verifyOtpAndLogin(VerifyOtpRequest request) {
        OtpRecord otpRecord = otpRepository
                .findTopByPhoneNumberAndIsUsedFalseOrderByCreatedAtDesc(request.getPhoneNumber())
                .orElseThrow(() -> new BusinessRuleException("OTP_NOT_FOUND", "No active OTP found"));

        if (!otpRecord.isValid(request.getOtpCode())) {
            otpRecord.setAttempts(otpRecord.getAttempts() + 1);
            otpRepository.save(otpRecord);
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        otpRecord.setIsUsed(true);
        otpRepository.save(otpRecord);

        // Find or create auth user
        AuthUser authUser = authUserRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> createNewUser(request.getPhoneNumber(), request.getRole()));

        authUser.setIsPhoneVerified(true);
        authUser.setLastLoginAt(Instant.now());
        authUser.setFailedLoginAttempts(0);
        authUserRepository.save(authUser);

        return generateAuthResponse(authUser);
    }

    /**
     * Refresh access token using a valid refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndIsRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        // Rotate: revoke old, issue new
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        AuthUser authUser = authUserRepository.findById(refreshToken.getAuthUserId())
                .orElseThrow(() -> new ResourceNotFoundException("AuthUser", refreshToken.getAuthUserId().toString()));

        return generateAuthResponse(authUser);
    }

    /**
     * Logout — revoke all refresh tokens for the user.
     */
    @Transactional
    public void logout(UUID authUserId) {
        refreshTokenRepository.revokeAllByAuthUserId(authUserId);
        log.info("All sessions revoked for user {}", authUserId);
    }

    // ---- Private Helpers ----

    private AuthUser createNewUser(String phoneNumber, UserRole role) {
        if (role == null) role = UserRole.CUSTOMER;

        AuthUser newUser = AuthUser.builder()
                .phoneNumber(phoneNumber)
                .role(role)
                .isActive(true)
                .build();
        AuthUser saved = authUserRepository.save(newUser);

        // Publish event so User/Driver module can create profile
        eventPublisher.publish(new UserRegisteredEvent(saved.getId(), phoneNumber, role));
        eventPublisher.publishAsync(new UserRegisteredEvent(saved.getId(), phoneNumber, role));

        log.info("New {} registered: {}", role, phoneNumber);
        return saved;
    }

    private AuthResponse generateAuthResponse(AuthUser authUser) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                authUser.getId(), authUser.getProfileId(), authUser.getRole());

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(authUser.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .authUserId(authUser.getId())
                .expiresAt(Instant.now().plusMillis(604800000)) // 7 days
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .userId(authUser.getId())
                .profileId(authUser.getProfileId())
                .role(authUser.getRole())
                .isNewUser(authUser.getProfileId() == null)
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
