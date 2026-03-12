package com.rydvrse.user.dto;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerResponse {
    private UUID id;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private Integer totalTrips;
    private Double averageRating;
}
