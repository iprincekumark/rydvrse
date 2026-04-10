package com.rydvrse.customer.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.customer.domain.SavedLocationEntity;
import com.rydvrse.customer.infrastructure.SavedLocationRepository;
import com.rydvrse.master.application.ServiceabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SavedLocationService {

    private final SavedLocationRepository savedLocationRepository;
    private final CustomerProfileService customerProfileService;
    private final ServiceabilityService serviceabilityService;

    public SavedLocationService(
            SavedLocationRepository savedLocationRepository,
            CustomerProfileService customerProfileService,
            ServiceabilityService serviceabilityService
    ) {
        this.savedLocationRepository = savedLocationRepository;
        this.customerProfileService = customerProfileService;
        this.serviceabilityService = serviceabilityService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        CustomerProfileEntity profile = customerProfileService.requireCurrentProfile();
        return savedLocationRepository.findByCustomerProfileIdAndDeletedFalseOrderByDefaultLocationDescCreatedAtAsc(profile.getId())
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional
    public Map<String, Object> create(SavedLocationCommand command) {
        CustomerProfileEntity profile = customerProfileService.requireCurrentProfile();
        SavedLocationEntity entity = new SavedLocationEntity();
        entity.setCustomerProfileId(profile.getId());
        apply(entity, command);
        return toMap(savedLocationRepository.save(entity));
    }

    @Transactional
    public Map<String, Object> update(UUID savedLocationId, SavedLocationCommand command) {
        CustomerProfileEntity profile = customerProfileService.requireCurrentProfile();
        SavedLocationEntity entity = savedLocationRepository.findByIdAndCustomerProfileIdAndDeletedFalse(savedLocationId, profile.getId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Saved location not found"));
        apply(entity, command);
        return toMap(savedLocationRepository.save(entity));
    }

    @Transactional
    public void delete(UUID savedLocationId) {
        CustomerProfileEntity profile = customerProfileService.requireCurrentProfile();
        SavedLocationEntity entity = savedLocationRepository.findByIdAndCustomerProfileIdAndDeletedFalse(savedLocationId, profile.getId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Saved location not found"));
        entity.setDeleted(true);
        entity.setDefaultLocation(false);
        savedLocationRepository.save(entity);
    }

    private void apply(SavedLocationEntity entity, SavedLocationCommand command) {
        ServiceabilityService.ZoneResolution resolution = serviceabilityService.resolveZone(command.cityId(), command.latitude(), command.longitude());
        entity.setLabel(command.label());
        entity.setAddressLine1(command.addressLine1());
        entity.setAddressLine2(command.addressLine2());
        entity.setLandmark(command.landmark());
        entity.setCityId(command.cityId());
        entity.setServiceZoneId(resolution.zoneId());
        entity.setLatitude(command.latitude());
        entity.setLongitude(command.longitude());
        entity.setDefaultLocation(command.isDefault());
        entity.setDeleted(false);
    }

    private Map<String, Object> toMap(SavedLocationEntity entity) {
        return Map.of(
                "saved_location_id", entity.getId(),
                "label", entity.getLabel(),
                "location", Map.of(
                        "label", entity.getLabel(),
                        "address_line_1", entity.getAddressLine1(),
                        "address_line_2", entity.getAddressLine2(),
                        "landmark", entity.getLandmark(),
                        "city_id", entity.getCityId(),
                        "zone_id", entity.getServiceZoneId(),
                        "latitude", entity.getLatitude(),
                        "longitude", entity.getLongitude()
                ),
                "is_default", entity.isDefaultLocation()
        );
    }

    public record SavedLocationCommand(
            String label,
            String addressLine1,
            String addressLine2,
            String landmark,
            UUID cityId,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude,
            boolean isDefault
    ) {
    }
}
