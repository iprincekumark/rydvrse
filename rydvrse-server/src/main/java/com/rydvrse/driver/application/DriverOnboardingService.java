package com.rydvrse.driver.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DriverOnboardingService {

    private final DriverProfileService driverProfileService;
    private final DriverProfileRepository driverProfileRepository;
    private final CurrentActorService currentActorService;

    public DriverOnboardingService(
            DriverProfileService driverProfileService,
            DriverProfileRepository driverProfileRepository,
            CurrentActorService currentActorService
    ) {
        this.driverProfileService = driverProfileService;
        this.driverProfileRepository = driverProfileRepository;
        this.currentActorService = currentActorService;
    }

    @Transactional
    public Map<String, Object> submit(OnboardingCommand command) {
        DriverProfileEntity profile = driverProfileService.requireCurrentProfile();
        profile.setFirstName(command.fullName());
        profile.setDateOfBirth(command.dateOfBirth());
        profile.setDefaultCityId(command.cityId());
        profile.setEmergencyContactName(command.emergencyContactName());
        profile.setEmergencyContactMobileE164(command.emergencyContactMobile());
        profile.setOnboardingStatus("SUBMITTED");
        ObjectNode metadata = (ObjectNode) profile.getMetadata();
        metadata.putPOJO("languages", command.languages());
        metadata.put("license_number", command.licenseNumber());
        metadata.put("aadhaar_number_masked_or_tokenized", command.aadhaarNumberMaskedOrTokenized());
        metadata.put("pan_number_masked_or_tokenized", command.panNumberMaskedOrTokenized());
        metadata.putPOJO("bank_account", Map.of(
                "account_holder_name", command.bankAccountHolderName(),
                "bank_name", command.bankName(),
                "account_number_masked", command.accountNumberMasked(),
                "ifsc_code", command.ifscCode()
        ));
        driverProfileRepository.save(profile);
        return status(profile);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatus() {
        return status(driverProfileService.requireCurrentProfile());
    }

    private Map<String, Object> status(DriverProfileEntity profile) {
        List<String> missingDocuments = profile.getOnboardingStatus().equals("APPROVED") ? List.of() : List.of("LICENSE", "AADHAAR", "PAN", "SELFIE", "BANK_PROOF");
        return Map.of(
                "driver_onboarding_state", profile.getOnboardingStatus(),
                "missing_fields", List.of(),
                "missing_documents", missingDocuments,
                "reviewer_notes", profile.getReviewerNotes(),
                "eligibility_to_receive_assignments", "APPROVED".equals(profile.getOnboardingStatus())
        );
    }

    public record OnboardingCommand(
            String fullName,
            LocalDate dateOfBirth,
            java.util.UUID cityId,
            List<String> languages,
            String licenseNumber,
            String aadhaarNumberMaskedOrTokenized,
            String panNumberMaskedOrTokenized,
            String bankAccountHolderName,
            String bankName,
            String accountNumberMasked,
            String ifscCode,
            String emergencyContactName,
            String emergencyContactMobile
    ) {
    }
}
