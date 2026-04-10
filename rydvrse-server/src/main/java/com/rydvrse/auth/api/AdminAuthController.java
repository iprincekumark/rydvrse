package com.rydvrse.auth.api;

import com.rydvrse.auth.application.AdminLoginService;
import com.rydvrse.auth.application.AuthRefreshService;
import com.rydvrse.auth.application.SessionService;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.common.security.RydvrsePrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminLoginService adminLoginService;
    private final AuthRefreshService authRefreshService;
    private final SessionService sessionService;
    private final CurrentActorService currentActorService;

    public AdminAuthController(
            AdminLoginService adminLoginService,
            AuthRefreshService authRefreshService,
            SessionService sessionService,
            CurrentActorService currentActorService
    ) {
        this.adminLoginService = adminLoginService;
        this.authRefreshService = authRefreshService;
        this.sessionService = sessionService;
        this.currentActorService = currentActorService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginService.AdminAuthenticatedActor authenticated = adminLoginService.login(request.email(), request.password(), null, null);
        return ApiResponse.of(Map.of(
                "access_token", authenticated.tokens().accessToken(),
                "refresh_token", authenticated.tokens().refreshToken(),
                "expires_in_seconds", authenticated.tokens().expiresInSeconds(),
                "admin", Map.of(
                        "user_id", authenticated.userAccount().getId(),
                        "email", authenticated.userAccount().getEmail()
                ),
                "roles", authenticated.roles()
        ), null);
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@Valid @RequestBody AdminRefreshRequest request) {
        AuthRefreshService.RefreshedSession refreshed = authRefreshService.refresh(request.refreshToken(), "ADMIN");
        return ApiResponse.of(Map.of(
                "access_token", refreshed.sessionTokens().accessToken(),
                "refresh_token", refreshed.sessionTokens().refreshToken(),
                "expires_in_seconds", refreshed.sessionTokens().expiresInSeconds(),
                "admin", Map.of(
                        "user_id", refreshed.userAccount().getId(),
                        "email", refreshed.userAccount().getEmail()
                ),
                "roles", refreshed.roles()
        ), null);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        RydvrsePrincipal principal = currentActorService.requireCurrentActor();
        sessionService.revoke(principal.sessionId(), principal.userId());
    }

    public record AdminLoginRequest(@Email @NotBlank String email, @NotBlank String password, String deviceId) {
    }

    public record AdminRefreshRequest(@NotBlank String refreshToken, String deviceId) {
    }
}
