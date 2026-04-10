package com.rydvrse.driver.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrentActorService currentActorService;

    public DriverProfileService(
            DriverProfileRepository driverProfileRepository,
            UserAccountRepository userAccountRepository,
            CurrentActorService currentActorService
    ) {
        this.driverProfileRepository = driverProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.currentActorService = currentActorService;
    }

    @Transactional(readOnly = true)
    public DriverProfileEntity requireCurrentProfile() {
        UUID userId = currentActorService.requireActor(ActorType.DRIVER).userId();
        return driverProfileRepository.findByUserAccountId(userId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Driver profile not found"));
    }

    @Transactional
    public Map<String, Object> updateProfile(DriverProfilePatch patch) {
        DriverProfileEntity profile = requireCurrentProfile();
        if (patch.fullName() != null && !patch.fullName().isBlank()) {
            String[] parts = patch.fullName().trim().split("\\s+", 2);
            profile.setFirstName(parts[0]);
            profile.setLastName(parts.length > 1 ? parts[1] : null);
        }
        if (patch.email() != null) {
            profile.setEmail(patch.email());
        }
        if (patch.languages() != null && !patch.languages().isEmpty()) {
            ((ObjectNode) profile.getMetadata()).putPOJO("languages", patch.languages());
        }
        if (patch.emergencyContact() != null) {
            profile.setEmergencyContactName(patch.emergencyContact().name());
            profile.setEmergencyContactMobileE164(patch.emergencyContact().mobileNumber());
        }
        driverProfileRepository.save(profile);
        return toResponse(profile);
    }

    public Map<String, Object> toResponse(DriverProfileEntity profile) {
        String mobile = userAccountRepository.findById(profile.getUserAccountId()).map(account -> account.getMobileNumberE164()).orElse(null);
        return Map.of(
                "driver_id", profile.getId(),
                "full_name", (profile.getFirstName() + " " + (profile.getLastName() == null ? "" : profile.getLastName())).trim(),
                "email", profile.getEmail(),
                "mobile_number", mobile,
                "city_id", profile.getDefaultCityId(),
                "onboarding_state", mapOnboardingState(profile.getOnboardingStatus()),
                "approval_state", profile.getOnboardingStatus(),
                "compliance_summary", profile.getComplianceStatus(),
                "zone_scope", Arrays.asList("BLR_CORE", "BLR_EXTENDED")
        );
    }

    private String mapOnboardingState(String onboardingStatus) {
        return switch (onboardingStatus) {
            case "APPROVED" -> "APPROVED";
            case "SUBMITTED", "IN_REVIEW" -> "UNDER_REVIEW";
            case "CORRECTION_REQUIRED" -> "CORRECTION_REQUIRED";
            case "REJECTED" -> "REJECTED";
            case "SUSPENDED" -> "SUSPENDED";
            default -> "INCOMPLETE";
        };
    }

    public record DriverProfilePatch(String fullName, String email, java.util.List<String> languages, EmergencyContact emergencyContact) {
    }

    public record EmergencyContact(String name, String mobileNumber) {
    }
}
