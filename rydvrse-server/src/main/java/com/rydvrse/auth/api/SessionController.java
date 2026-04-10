package com.rydvrse.auth.api;

import com.rydvrse.auth.application.AuthRefreshService;
import com.rydvrse.auth.application.SessionService;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.common.security.RydvrsePrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class SessionController {

    private final AuthRefreshService authRefreshService;
    private final SessionService sessionService;
    private final CurrentActorService currentActorService;

    public SessionController(AuthRefreshService authRefreshService, SessionService sessionService, CurrentActorService currentActorService) {
        this.authRefreshService = authRefreshService;
        this.sessionService = sessionService;
        this.currentActorService = currentActorService;
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Object>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthRefreshService.RefreshedSession refreshed = authRefreshService.refresh(request.refreshToken(), request.actorType());
        Map<String, Object> body = new HashMap<>();
        body.put("access_token", refreshed.sessionTokens().accessToken());
        body.put("refresh_token", refreshed.sessionTokens().refreshToken());
        body.put("expires_in_seconds", refreshed.sessionTokens().expiresInSeconds());
        body.put("actor_type", refreshed.actorType().name());
        body.put("user", Map.of(
                "user_id", refreshed.userAccount().getId(),
                "mobile_number", refreshed.userAccount().getMobileNumberE164()
        ));
        body.put("profile", Map.of(
                "profile_id", refreshed.profileId(),
                "is_new_user", false,
                "onboarding_state", refreshed.actorType().name().equals("DRIVER") ? "INCOMPLETE" : "COMPLETE"
        ));
        if (!refreshed.roles().isEmpty()) {
            body.put("roles", refreshed.roles());
        }
        return ApiResponse.of(body, null);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody(required = false) LogoutRequest request) {
        RydvrsePrincipal principal = currentActorService.requireCurrentActor();
        sessionService.revoke(principal.sessionId(), principal.userId());
    }

    public record RefreshRequest(@NotBlank String refreshToken, @NotBlank String actorType, String deviceId) {
    }

    public record LogoutRequest(String refreshToken) {
    }
}
