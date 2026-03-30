package com.rydvrse.notification.controller;

import com.rydvrse.notification.entity.DeviceToken;
import com.rydvrse.notification.service.NotificationService;
import com.rydvrse.shared.dto.ApiResponse;
import com.rydvrse.shared.enums.DevicePlatform;
import com.rydvrse.shared.enums.UserType;
import com.rydvrse.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/v1/notifications") @RequiredArgsConstructor
@Tag(name = "Notification", description = "Device token management")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/device-token")
    @Operation(summary = "Register/update FCM token")
    public ResponseEntity<ApiResponse<DeviceToken>> registerToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        DeviceToken token = notificationService.registerToken(
                principal.getUserId(), UserType.valueOf(principal.getUserType()),
                DevicePlatform.valueOf(body.get("platform")), body.get("fcmToken"));
        return ResponseEntity.ok(ApiResponse.success(token));
    }
}
