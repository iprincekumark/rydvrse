package com.rydvrse.auth.api;

import com.rydvrse.auth.application.OtpChallengeService;
import com.rydvrse.auth.application.OtpVerificationService;
import com.rydvrse.auth.application.SessionTokens;
import com.rydvrse.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class OtpAuthController {

    private final OtpChallengeService otpChallengeService;
    private final OtpVerificationService otpVerificationService;

    public OtpAuthController(OtpChallengeService otpChallengeService, OtpVerificationService otpVerificationService) {
        this.otpChallengeService = otpChallengeService;
        this.otpVerificationService = otpVerificationService;
    }

    @PostMapping("/otp/request")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Object>> requestOtp(@Valid @RequestBody OtpRequest request, HttpServletRequest httpServletRequest) {
        OtpChallengeService.OtpChallengeResult result = otpChallengeService.requestOtp(
                request.mobileNumber(),
                request.actorType(),
                request.purpose(),
                request.deviceId(),
                httpServletRequest.getRemoteAddr()
        );
        return ApiResponse.of(Map.of(
                "challenge_id", result.challengeId(),
                "masked_mobile_number", result.maskedMobileNumber(),
                "expires_at", result.expiresAt(),
                "retry_after_seconds", result.retryAfterSeconds()
        ), null);
    }

    @PostMapping("/otp/verify")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request, HttpServletRequest httpServletRequest) {
        OtpVerificationService.AuthenticatedActor result = otpVerificationService.verify(
                request.challengeId(),
                request.otpCode(),
                request.actorType(),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        );
        SessionTokens tokens = result.tokens();
        return ApiResponse.of(Map.of(
                "access_token", tokens.accessToken(),
                "refresh_token", tokens.refreshToken(),
                "expires_in_seconds", tokens.expiresInSeconds(),
                "actor_type", result.actorType().name(),
                "user", Map.of(
                        "user_id", result.userAccount().getId(),
                        "mobile_number", result.userAccount().getMobileNumberE164()
                ),
                "profile", Map.of(
                        "profile_id", result.profileId(),
                        "is_new_user", result.newUser(),
                        "onboarding_state", result.onboardingState()
                )
        ), null);
    }

    public record OtpRequest(
            @NotBlank String mobileNumber,
            @NotBlank String actorType,
            @NotBlank String purpose,
            String deviceId
    ) {
    }

    public record OtpVerifyRequest(
            @NotBlank String challengeId,
            @NotBlank String otpCode,
            @NotBlank String actorType,
            String deviceId
    ) {
    }
}
