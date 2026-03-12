package com.rydvrse.auth.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpResponse {
    private String phoneNumber;
    private String message;
    private int expiresInSeconds;
}
