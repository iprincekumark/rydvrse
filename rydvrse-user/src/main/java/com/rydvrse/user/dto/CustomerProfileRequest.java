package com.rydvrse.user.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private LocalDate dateOfBirth;
    private String gender;
}
