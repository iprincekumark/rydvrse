package com.rydvrse.auth.application;

import com.rydvrse.auth.domain.OtpChallengeEntity;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.infrastructure.OtpChallengeRepository;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.util.HashingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class OtpChallengeService {

    private final OtpChallengeRepository otpChallengeRepository;
    private final UserAccountRepository userAccountRepository;
    private final MobileNumberNormalizer mobileNumberNormalizer;
    private final RydvrseProperties rydvrseProperties;

    public OtpChallengeService(
            OtpChallengeRepository otpChallengeRepository,
            UserAccountRepository userAccountRepository,
            MobileNumberNormalizer mobileNumberNormalizer,
            RydvrseProperties rydvrseProperties
    ) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.userAccountRepository = userAccountRepository;
        this.mobileNumberNormalizer = mobileNumberNormalizer;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Transactional
    public OtpChallengeResult requestOtp(String mobileNumber, String actorType, String purpose, String deviceId, String requestIp) {
        String normalized = mobileNumberNormalizer.normalize(mobileNumber);
        if (normalized.isBlank()) {
            throw ApiException.unprocessable(ErrorCode.VALIDATION_ERROR, "Mobile number is required");
        }
        userAccountRepository.findByMobileNumberE164(normalized)
                .filter(account -> "SUSPENDED".equals(account.getStatus()) || "BLOCKED".equals(account.getStatus()))
                .ifPresent(account -> {
                    throw ApiException.unprocessable(ErrorCode.AUTH_ACCOUNT_SUSPENDED, "Account is suspended");
                });
        otpChallengeRepository.findTopByMobileNumberE164OrderByCreatedAtDesc(normalized)
                .filter(existing -> existing.getCreatedAt().isAfter(OffsetDateTime.now().minusSeconds(60)))
                .ifPresent(existing -> {
                    throw ApiException.unprocessable(ErrorCode.AUTH_OTP_RATE_LIMITED, "OTP requested too recently");
                });
        UserAccountEntity account = userAccountRepository.findByMobileNumberE164(normalized).orElse(null);
        if (account != null && !account.getUserType().equals(actorType)) {
            throw ApiException.unprocessable(ErrorCode.AUTH_ACCOUNT_SUSPENDED, "Mobile number is not registered for this actor type");
        }
        OtpChallengeEntity challenge = new OtpChallengeEntity();
        challenge.setUserAccountId(account == null ? null : account.getId());
        challenge.setMobileNumberE164(normalized);
        challenge.setChannel("SMS");
        challenge.setPurpose(purpose);
        challenge.setOtpHash(HashingUtils.sha256(rydvrseProperties.getAuth().getDefaultOtpCode()));
        challenge.setStatus("ISSUED");
        challenge.setAttemptCount(0);
        challenge.setMaxAttempts(5);
        challenge.setExpiresAt(OffsetDateTime.now().plusMinutes(rydvrseProperties.getAuth().getOtpExpiryMinutes()));
        challenge.setRequestIp(requestIp);
        challenge.setDeviceFingerprint(deviceId);
        otpChallengeRepository.save(challenge);
        return new OtpChallengeResult(
                challenge.getId().toString(),
                mobileNumberNormalizer.mask(normalized),
                challenge.getExpiresAt(),
                60
        );
    }

    public record OtpChallengeResult(String challengeId, String maskedMobileNumber, OffsetDateTime expiresAt, int retryAfterSeconds) {
    }
}
