package com.rydvrse.auth.application;

import com.rydvrse.auth.domain.UserSessionEntity;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.infrastructure.UserSessionRepository;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.JwtService;
import com.rydvrse.common.util.HashingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final UserSessionRepository userSessionRepository;
    private final JwtService jwtService;
    private final RydvrseProperties rydvrseProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(
            UserSessionRepository userSessionRepository,
            JwtService jwtService,
            RydvrseProperties rydvrseProperties
    ) {
        this.userSessionRepository = userSessionRepository;
        this.jwtService = jwtService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Transactional
    public SessionTokens createSession(UserAccountEntity account, UUID profileId, ActorType actorType, List<String> roles, String requestIp, String userAgent) {
        String refreshToken = randomRefreshToken();
        UserSessionEntity session = new UserSessionEntity();
        session.setUserAccountId(account.getId());
        session.setRefreshTokenHash(HashingUtils.sha256(refreshToken));
        session.setSessionStatus("ACTIVE");
        session.setIssuedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plusDays(rydvrseProperties.getSecurity().getRefreshTokenTtlDays()));
        session.setLastSeenAt(OffsetDateTime.now());
        session.setRequestIp(requestIp);
        session.setUserAgent(userAgent);
        userSessionRepository.save(session);

        String accessToken = jwtService.createAccessToken(account.getId(), profileId, session.getId(), actorType, roles);
        return new SessionTokens(accessToken, refreshToken, rydvrseProperties.getSecurity().getAccessTokenTtlMinutes() * 60);
    }

    @Transactional
    public SessionTokens refresh(String refreshToken, UUID profileId, ActorType actorType, UserAccountEntity account, List<String> roles) {
        UserSessionEntity session = userSessionRepository.findByRefreshTokenHash(HashingUtils.sha256(refreshToken))
                .orElseThrow(() -> ApiException.unauthorized(ErrorCode.SESSION_EXPIRED, "Refresh session not found"));
        if (!"ACTIVE".equals(session.getSessionStatus()) || session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw ApiException.unauthorized(ErrorCode.SESSION_EXPIRED, "Session expired");
        }
        session.setSessionStatus("REVOKED");
        session.setRevokedAt(OffsetDateTime.now());
        userSessionRepository.save(session);
        return createSession(account, profileId, actorType, roles, session.getRequestIp(), session.getUserAgent());
    }

    @Transactional
    public void revoke(UUID sessionId, UUID userId) {
        UserSessionEntity session = userSessionRepository.findByIdAndUserAccountId(sessionId, userId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Session not found"));
        session.setSessionStatus("REVOKED");
        session.setRevokedAt(OffsetDateTime.now());
        userSessionRepository.save(session);
    }

    private String randomRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
