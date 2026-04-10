package com.rydvrse.driver.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.driver.domain.DriverAvailabilityStatusEntity;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverAvailabilityStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class DriverAvailabilityService {

    private final DriverProfileService driverProfileService;
    private final DriverAvailabilityStatusRepository availabilityStatusRepository;
    private final CurrentActorService currentActorService;

    public DriverAvailabilityService(
            DriverProfileService driverProfileService,
            DriverAvailabilityStatusRepository availabilityStatusRepository,
            CurrentActorService currentActorService
    ) {
        this.driverProfileService = driverProfileService;
        this.availabilityStatusRepository = availabilityStatusRepository;
        this.currentActorService = currentActorService;
    }

    @Transactional
    public Map<String, Object> update(String status, String reasonCode) {
        DriverProfileEntity profile = driverProfileService.requireCurrentProfile();
        if (!"APPROVED".equals(profile.getOnboardingStatus())) {
            throw ApiException.unprocessable(ErrorCode.DRIVER_NOT_APPROVED, "Driver is not approved");
        }
        DriverAvailabilityStatusEntity entity = availabilityStatusRepository.findById(profile.getId()).orElseGet(() -> {
            DriverAvailabilityStatusEntity created = new DriverAvailabilityStatusEntity();
            created.setDriverProfileId(profile.getId());
            return created;
        });
        if (entity.getCurrentTripId() != null && "OFFLINE".equals(status)) {
            throw ApiException.unprocessable(ErrorCode.AVAILABILITY_CHANGE_NOT_ALLOWED, "Cannot go offline during an active trip");
        }
        entity.setCurrentStatus("ONLINE".equals(status) ? "AVAILABLE" : "OFFLINE");
        entity.setUpdatedByUserId(currentActorService.requireCurrentActor().userId());
        availabilityStatusRepository.save(entity);
        profile.setCurrentStatus(entity.getCurrentStatus());
        return Map.of(
                "driver_id", profile.getId(),
                "status", status,
                "effective_status", entity.getCurrentStatus(),
                "reason_code", reasonCode
        );
    }
}
