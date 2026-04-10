package com.rydvrse.master.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.master.domain.ServiceZoneEntity;
import com.rydvrse.master.domain.ServiceabilityRuleEntity;
import com.rydvrse.master.infrastructure.ServiceZoneRepository;
import com.rydvrse.master.infrastructure.ServiceabilityRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Service
public class ServiceabilityService {

    private final ServiceZoneRepository serviceZoneRepository;
    private final ServiceabilityRuleRepository serviceabilityRuleRepository;

    public ServiceabilityService(ServiceZoneRepository serviceZoneRepository, ServiceabilityRuleRepository serviceabilityRuleRepository) {
        this.serviceZoneRepository = serviceZoneRepository;
        this.serviceabilityRuleRepository = serviceabilityRuleRepository;
    }

    public ZoneResolution resolveZone(UUID cityId, BigDecimal latitude, BigDecimal longitude) {
        ServiceZoneEntity zone = serviceZoneRepository.findContainingZone(cityId, latitude, longitude)
                .orElseThrow(() -> ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Location is outside supported zones"));
        return new ZoneResolution(zone.getId(), zone.getZoneCode(), zone.getZoneName());
    }

    public ServiceabilityResult check(UUID cityId, String serviceType, BigDecimal pickupLatitude, BigDecimal pickupLongitude, OffsetDateTime scheduledPickupAt) {
        ZoneResolution pickupZone = resolveZone(cityId, pickupLatitude, pickupLongitude);
        ServiceabilityRuleEntity rule = serviceabilityRuleRepository.findByCityIdAndServiceZoneIdAndServiceType(cityId, pickupZone.zoneId(), serviceType)
                .orElseThrow(() -> ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Service unavailable for pickup zone"));
        if (!rule.isEnabled()) {
            throw ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Service disabled for pickup zone");
        }
        long leadMinutes = java.time.Duration.between(OffsetDateTime.now(), scheduledPickupAt).toMinutes();
        if (leadMinutes < rule.getMinLeadMinutes()) {
            throw ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Lead time below minimum threshold");
        }
        if (rule.getOperatingStartLocal() != null && rule.getOperatingEndLocal() != null) {
            var localTime = scheduledPickupAt.atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalTime();
            if (localTime.isBefore(rule.getOperatingStartLocal()) || localTime.isAfter(rule.getOperatingEndLocal())) {
                throw ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Pickup time outside service window");
            }
        }
        return new ServiceabilityResult(true, null, pickupZone.zoneId(), null);
    }

    public Map<String, Object> toResponse(ServiceabilityResult result) {
        return Map.of(
                "serviceable", result.serviceable(),
                "reason_code", result.reasonCode(),
                "resolved_pickup_zone_id", result.pickupZoneId(),
                "resolved_drop_zone_id", result.dropZoneId()
        );
    }

    public record ZoneResolution(UUID zoneId, String zoneCode, String zoneName) {
    }

    public record ServiceabilityResult(boolean serviceable, String reasonCode, UUID pickupZoneId, UUID dropZoneId) {
    }
}
