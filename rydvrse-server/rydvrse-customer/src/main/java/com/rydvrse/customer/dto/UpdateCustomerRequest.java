package com.rydvrse.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateCustomerRequest {
    @Size(max = 100) private String name;
    @Email private String email;
    @Size(max = 500) private String profileImageUrl;
}
