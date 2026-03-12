package com.rydvrse.auth.dto;

import com.rydvrse.shared.enums.UserRole;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private UUID profileId;
    private UserRole role;
    private boolean isNewUser;
}
