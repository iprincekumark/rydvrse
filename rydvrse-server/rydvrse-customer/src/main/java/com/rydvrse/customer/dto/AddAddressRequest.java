package com.rydvrse.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddAddressRequest {
    @NotBlank private String label;
    @NotBlank private String address;
    @NotNull private Double lat;
    @NotNull private Double lng;
}
