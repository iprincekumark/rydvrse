package com.rydvrse.auth.dto;

import com.rydvrse.shared.enums.UserType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private UserType userType;
}
