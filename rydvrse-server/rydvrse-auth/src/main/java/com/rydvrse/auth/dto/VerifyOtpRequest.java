package com.rydvrse.auth.dto;

import com.rydvrse.shared.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyOtpRequest {
    @NotBlank private String phoneNumber;
    @NotBlank private String otpCode;
    private UserRole role; // CUSTOMER or DRIVER — determines profile type
}
