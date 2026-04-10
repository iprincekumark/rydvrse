package com.rydvrse.customer.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.customer.infrastructure.CustomerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CurrentActorService currentActorService;

    public CustomerProfileService(CustomerProfileRepository customerProfileRepository, CurrentActorService currentActorService) {
        this.customerProfileRepository = customerProfileRepository;
        this.currentActorService = currentActorService;
    }

    @Transactional(readOnly = true)
    public CustomerProfileEntity requireCurrentProfile() {
        return customerProfileRepository.findByUserAccountId(currentActorService.requireActor(ActorType.CUSTOMER).userId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Customer profile not found"));
    }

    @Transactional
    public Map<String, Object> updateProfile(CustomerProfilePatch patch) {
        CustomerProfileEntity profile = requireCurrentProfile();
        if (patch.fullName() != null && !patch.fullName().isBlank()) {
            String[] parts = patch.fullName().trim().split("\\s+", 2);
            profile.setFirstName(parts[0]);
            profile.setLastName(parts.length > 1 ? parts[1] : null);
        }
        if (patch.email() != null) {
            profile.setEmail(patch.email());
        }
        if (patch.cityId() != null) {
            profile.setDefaultCityId(patch.cityId());
        }
        profile.setLastActiveAt(OffsetDateTime.now());
        customerProfileRepository.save(profile);
        return toProfileMap(profile, null);
    }

    public Map<String, Object> toProfileMap(CustomerProfileEntity profile, String mobileNumber) {
        return Map.of(
                "customer_id", profile.getId(),
                "full_name", (profile.getFirstName() + " " + (profile.getLastName() == null ? "" : profile.getLastName())).trim(),
                "email", profile.getEmail(),
                "mobile_number", mobileNumber,
                "city_id", profile.getDefaultCityId(),
                "profile_completion_state", profile.getEmail() == null ? "PARTIAL" : "COMPLETE",
                "default_saved_location_id", profile.getMetadata().path("default_saved_location_id").isMissingNode() ? null : profile.getMetadata().path("default_saved_location_id").asText(),
                "created_at", profile.getCreatedAt()
        );
    }

    public record CustomerProfilePatch(String fullName, String email, java.util.UUID cityId) {
    }
}
