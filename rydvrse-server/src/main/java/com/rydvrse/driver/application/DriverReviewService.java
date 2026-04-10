package com.rydvrse.driver.application;

import com.rydvrse.common.audit.AuditService;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverAvailabilityStatusRepository;
import com.rydvrse.driver.infrastructure.DriverDocumentRepository;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DriverReviewService {

    private final DriverProfileRepository driverProfileRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final DriverAvailabilityStatusRepository driverAvailabilityStatusRepository;
    private final AuditService auditService;
    private final CurrentActorService currentActorService;

    public DriverReviewService(
            DriverProfileRepository driverProfileRepository,
            DriverDocumentRepository driverDocumentRepository,
            DriverAvailabilityStatusRepository driverAvailabilityStatusRepository,
            AuditService auditService,
            CurrentActorService currentActorService
    ) {
        this.driverProfileRepository = driverProfileRepository;
        this.driverDocumentRepository = driverDocumentRepository;
        this.driverAvailabilityStatusRepository = driverAvailabilityStatusRepository;
        this.auditService = auditService;
        this.currentActorService = currentActorService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> onboardingQueue() {
        currentActorService.requireActor(ActorType.ADMIN);
        return driverProfileRepository.findAll().stream()
                .filter(profile -> List.of("SUBMITTED", "IN_REVIEW", "CORRECTION_REQUIRED").contains(profile.getOnboardingStatus()))
                .map(profile -> detail(profile.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(UUID driverId) {
        currentActorService.requireActor(ActorType.ADMIN);
        DriverProfileEntity profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Driver not found"));
        return Map.of(
                "driver_profile", Map.of(
                        "driver_id", profile.getId(),
                        "full_name", (profile.getFirstName() + " " + (profile.getLastName() == null ? "" : profile.getLastName())).trim(),
                        "email", profile.getEmail(),
                        "onboarding_state", profile.getOnboardingStatus(),
                        "compliance_status", profile.getComplianceStatus()
                ),
                "document_checklist", driverDocumentRepository.findByDriverProfileIdOrderBySubmittedAtDesc(profile.getId()).stream()
                        .map(document -> Map.of(
                                "document_id", document.getId(),
                                "document_type", document.getDocumentType(),
                                "status", document.getStatus(),
                                "expires_at", document.getExpiresAt()
                        )).toList(),
                "review_notes", profile.getReviewerNotes(),
                "approval_history", List.of(),
                "operational_summary", Map.of(
                        "current_status", profile.getCurrentStatus(),
                        "approved_at", profile.getApprovedAt()
                )
        );
    }

    @Transactional
    public Map<String, Object> approve(UUID driverId, String reasonNote, Long rowVersion) {
        DriverProfileEntity profile = getMutable(driverId, rowVersion);
        profile.setOnboardingStatus("APPROVED");
        profile.setApprovedAt(OffsetDateTime.now());
        profile.setReviewerNotes(reasonNote);
        driverProfileRepository.save(profile);
        DriverAvailabilityStatusEntity availability = driverAvailabilityStatusRepository.findById(profile.getId()).orElseGet(() -> {
            DriverAvailabilityStatusEntity entity = new DriverAvailabilityStatusEntity();
            entity.setDriverProfileId(profile.getId());
            return entity;
        });
        availability.setCurrentStatus("OFFLINE");
        driverAvailabilityStatusRepository.save(availability);
        auditService.record("DRIVER_APPROVED", "DRIVER_PROFILE", profile.getId(), null, Map.of("status", "APPROVED"), null, reasonNote);
        return Map.of(
                "driver_id", profile.getId(),
                "onboarding_state", profile.getOnboardingStatus(),
                "eligibility_state", true,
                "audit_reference", "DRIVER_APPROVED"
        );
    }

    @Transactional
    public Map<String, Object> reject(UUID driverId, String reasonCode, String reasonNote, Long rowVersion) {
        DriverProfileEntity profile = getMutable(driverId, rowVersion);
        profile.setOnboardingStatus("REJECTED");
        profile.setReviewerNotes(reasonNote);
        profile.setRejectionReasonCode(reasonCode);
        driverProfileRepository.save(profile);
        auditService.record("DRIVER_REJECTED", "DRIVER_PROFILE", profile.getId(), null, Map.of("status", "REJECTED"), reasonCode, reasonNote);
        return Map.of("driver_id", profile.getId(), "onboarding_state", "REJECTED");
    }

    @Transactional
    public Map<String, Object> requestCorrection(UUID driverId, List<String> requiredCorrections, String reasonNote, Long rowVersion) {
        DriverProfileEntity profile = getMutable(driverId, rowVersion);
        profile.setOnboardingStatus("CORRECTION_REQUIRED");
        profile.setReviewerNotes(reasonNote + " Corrections: " + requiredCorrections);
        driverProfileRepository.save(profile);
        auditService.record("DRIVER_CORRECTION_REQUESTED", "DRIVER_PROFILE", profile.getId(), null, Map.of("status", "CORRECTION_REQUIRED"), null, reasonNote);
        return Map.of(
                "driver_id", profile.getId(),
                "onboarding_state", "CORRECTION_REQUIRED",
                "reviewer_notes", profile.getReviewerNotes()
        );
    }

    @Transactional
    public Map<String, Object> suspend(UUID driverId, String reasonCode, OffsetDateTime effectiveUntil, String reasonNote, Long rowVersion) {
        DriverProfileEntity profile = getMutable(driverId, rowVersion);
        profile.setOnboardingStatus("SUSPENDED");
        profile.setComplianceStatus("BLOCKED");
        profile.setSuspendedAt(OffsetDateTime.now());
        profile.setReviewerNotes(reasonNote);
        driverProfileRepository.save(profile);
        auditService.record("DRIVER_SUSPENDED", "DRIVER_PROFILE", profile.getId(), null, Map.of("status", "SUSPENDED"), reasonCode, reasonNote);
        return Map.of(
                "driver_id", profile.getId(),
                "compliance_status", profile.getComplianceStatus(),
                "operational_status", "SUSPENDED",
                "effective_until", effectiveUntil
        );
    }

    private DriverProfileEntity getMutable(UUID driverId, Long rowVersion) {
        currentActorService.requireActor(ActorType.ADMIN);
        DriverProfileEntity profile = driverProfileRepository.findById(driverId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Driver not found"));
        if (rowVersion != null && !rowVersion.equals(profile.getRowVersion())) {
            throw ApiException.conflict(ErrorCode.STALE_ROW_VERSION, "Driver profile has changed");
        }
        return profile;
    }
}
