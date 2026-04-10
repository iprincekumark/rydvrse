package com.rydvrse.auth.application;

import com.rydvrse.auth.domain.OtpChallengeEntity;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.infrastructure.OtpChallengeRepository;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.customer.infrastructure.CustomerProfileRepository;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import com.rydvrse.common.util.HashingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OtpVerificationService {

    private static final UUID DEFAULT_CITY_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    private final OtpChallengeRepository otpChallengeRepository;
    private final UserAccountRepository userAccountRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final SessionService sessionService;

    public OtpVerificationService(
            OtpChallengeRepository otpChallengeRepository,
            UserAccountRepository userAccountRepository,
            CustomerProfileRepository customerProfileRepository,
            DriverProfileRepository driverProfileRepository,
            SessionService sessionService
    ) {
        this.otpChallengeRepository = otpChallengeRepository;
        this.userAccountRepository = userAccountRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public AuthenticatedActor verify(String challengeId, String otpCode, String actorType, String requestIp, String userAgent) {
        OtpChallengeEntity challenge = otpChallengeRepository.findById(UUID.fromString(challengeId))
                .orElseThrow(() -> ApiException.unprocessable(ErrorCode.AUTH_OTP_INVALID, "OTP challenge not found"));
        if (challenge.getExpiresAt().isBefore(OffsetDateTime.now())) {
            challenge.setStatus("EXPIRED");
            otpChallengeRepository.save(challenge);
            throw ApiException.unprocessable(ErrorCode.AUTH_OTP_EXPIRED, "OTP expired");
        }
        if (!challenge.getOtpHash().equals(HashingUtils.sha256(otpCode))) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            otpChallengeRepository.save(challenge);
            throw ApiException.unprocessable(ErrorCode.AUTH_OTP_INVALID, "Invalid OTP");
        }
        challenge.setStatus("VERIFIED");
        challenge.setVerifiedAt(OffsetDateTime.now());
        otpChallengeRepository.save(challenge);

        UserAccountEntity account = userAccountRepository.findByMobileNumberE164(challenge.getMobileNumberE164())
                .orElseGet(() -> createAccount(challenge.getMobileNumberE164(), actorType));
        if (!account.getUserType().equals(actorType)) {
            throw ApiException.unprocessable(ErrorCode.AUTH_ACCOUNT_SUSPENDED, "Actor type mismatch for account");
        }
        account.setMobileVerified(true);
        account.setLastLoginAt(OffsetDateTime.now());
        userAccountRepository.save(account);

        ActorType resolvedActorType = ActorType.valueOf(actorType);
        if (resolvedActorType == ActorType.CUSTOMER) {
            CustomerProfileEntity profile = customerProfileRepository.findByUserAccountId(account.getId())
                    .orElseGet(() -> createCustomerProfile(account));
            SessionTokens tokens = sessionService.createSession(account, profile.getId(), resolvedActorType, List.of(), requestIp, userAgent);
            return AuthenticatedActor.forCustomer(account, profile, tokens);
        }

        DriverProfileEntity profile = driverProfileRepository.findByUserAccountId(account.getId())
                .orElseGet(() -> createDriverProfile(account));
        SessionTokens tokens = sessionService.createSession(account, profile.getId(), resolvedActorType, List.of(), requestIp, userAgent);
        return AuthenticatedActor.forDriver(account, profile, tokens);
    }

    private UserAccountEntity createAccount(String mobileNumber, String actorType) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.setUserType(actorType);
        entity.setMobileCountryCode("+91");
        entity.setMobileNumberE164(mobileNumber);
        entity.setStatus("ACTIVE");
        entity.setMobileVerified(true);
        return userAccountRepository.save(entity);
    }

    private CustomerProfileEntity createCustomerProfile(UserAccountEntity account) {
        CustomerProfileEntity profile = new CustomerProfileEntity();
        profile.setUserAccountId(account.getId());
        profile.setFirstName("Rydvrse");
        profile.setLastName("Customer");
        profile.setEmail(account.getEmail());
        profile.setDefaultCityId(DEFAULT_CITY_ID);
        profile.setStatus("ACTIVE");
        profile.setLastActiveAt(OffsetDateTime.now());
        return customerProfileRepository.save(profile);
    }

    private DriverProfileEntity createDriverProfile(UserAccountEntity account) {
        DriverProfileEntity profile = new DriverProfileEntity();
        profile.setUserAccountId(account.getId());
        profile.setFirstName("Rydvrse");
        profile.setLastName("Driver");
        profile.setEmail(account.getEmail());
        profile.setDefaultCityId(DEFAULT_CITY_ID);
        profile.setOnboardingStatus("DRAFT");
        profile.setComplianceStatus("CLEAR");
        profile.setCurrentStatus("OFFLINE");
        return driverProfileRepository.save(profile);
    }

    public record AuthenticatedActor(
            SessionTokens tokens,
            ActorType actorType,
            UserAccountEntity userAccount,
            UUID profileId,
            boolean newUser,
            String onboardingState
    ) {
        static AuthenticatedActor forCustomer(UserAccountEntity account, CustomerProfileEntity profile, SessionTokens tokens) {
            return new AuthenticatedActor(tokens, ActorType.CUSTOMER, account, profile.getId(), false, "COMPLETE");
        }

        static AuthenticatedActor forDriver(UserAccountEntity account, DriverProfileEntity profile, SessionTokens tokens) {
            boolean newUser = "DRAFT".equals(profile.getOnboardingStatus());
            String onboardingState = switch (profile.getOnboardingStatus()) {
                case "APPROVED" -> "APPROVED";
                case "CORRECTION_REQUIRED" -> "CORRECTION_REQUIRED";
                case "REJECTED" -> "REJECTED";
                case "SUSPENDED" -> "SUSPENDED";
                case "SUBMITTED", "IN_REVIEW" -> "SUBMITTED";
                default -> "INCOMPLETE";
            };
            return new AuthenticatedActor(tokens, ActorType.DRIVER, account, profile.getId(), newUser, onboardingState);
        }
    }
}
