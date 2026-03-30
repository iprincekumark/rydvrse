package com.rydvrse.customer.dto;

import com.rydvrse.shared.enums.CustomerStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    private CustomerStatus status;
    private Instant createdAt;
}
