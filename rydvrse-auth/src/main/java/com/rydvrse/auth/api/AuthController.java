package com.rydvrse.auth.api;

import com.rydvrse.auth.dto.*;
import com.rydvrse.auth.service.AuthService;
import com.rydvrse.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authentication REST controller.
 * All endpoints under /v1/auth/** are public (see SecurityConfig).
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Send OTP to phone number */
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("OTP sent", authService.sendOtp(request)));
    }

    /** Verify OTP and login/register */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", authService.verifyOtpAndLogin(request)));
    }

    /** Refresh access token */
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request)));
    }

    /** Logout — revoke all sessions */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UUID userId) {
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
