package com.rydvrse.customer.api;

import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers/me")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;
    private final UserAccountRepository userAccountRepository;

    public CustomerProfileController(CustomerProfileService customerProfileService, UserAccountRepository userAccountRepository) {
        this.customerProfileService = customerProfileService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getProfile() {
        CustomerProfileEntity profile = customerProfileService.requireCurrentProfile();
        String mobile = userAccountRepository.findById(profile.getUserAccountId()).map(account -> account.getMobileNumberE164()).orElse(null);
        return ApiResponse.of(customerProfileService.toProfileMap(profile, mobile), null);
    }

    @PatchMapping
    public ApiResponse<Map<String, Object>> updateProfile(@Valid @RequestBody ProfilePatchRequest request) {
        return ApiResponse.of(customerProfileService.updateProfile(new CustomerProfileService.CustomerProfilePatch(
                request.fullName(),
                request.email(),
                request.cityId()
        )), null);
    }

    public record ProfilePatchRequest(
            @NotBlank String fullName,
            @Email String email,
            java.util.UUID cityId
    ) {
    }
}
