package com.rydvrse.auth.dto;

import com.rydvrse.shared.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpSendRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+91\\d{10}$", message = "Phone must be in +91XXXXXXXXXX format")
    private String phone;

    @NotNull(message = "User type is required")
    private UserType userType;
}
