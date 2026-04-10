package com.rydvrse.master.application;

import com.rydvrse.master.domain.BusinessConfigEntity;
import com.rydvrse.master.domain.FeatureFlagEntity;
import com.rydvrse.master.domain.ServiceabilityRuleEntity;
import com.rydvrse.master.infrastructure.BusinessConfigRepository;
import com.rydvrse.master.infrastructure.FeatureFlagRepository;
import com.rydvrse.master.infrastructure.ServiceabilityRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfigReadService {

    private final BusinessConfigRepository businessConfigRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final ServiceabilityRuleRepository serviceabilityRuleRepository;

    public ConfigReadService(
            BusinessConfigRepository businessConfigRepository,
            FeatureFlagRepository featureFlagRepository,
            ServiceabilityRuleRepository serviceabilityRuleRepository
    ) {
        this.businessConfigRepository = businessConfigRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.serviceabilityRuleRepository = serviceabilityRuleRepository;
    }

    public Map<String, Object> bootstrap(String actorType, UUID cityId) {
        return Map.of(
                "minimum_supported_version", "1.0.0",
                "supported_service_types", supportedServiceTypes(cityId),
                "supported_payment_methods", List.of("UPI_INTENT", "UPI_COLLECT"),
                "support_contact", supportContact(),
                "active_feature_flags", featureFlags(),
                "policy_summaries", List.of(
                        Map.of("policy_key", "trip_start_confirmation", "summary", "Customer confirmation is required before billing starts"),
                        Map.of("policy_key", "pricing_transparency", "summary", "Quote amount is shown before booking confirmation")
                )
        );
    }

    public List<String> supportedServiceTypes(UUID cityId) {
        return serviceabilityRuleRepository.findByCityIdAndEnabledTrue(cityId).stream()
                .map(ServiceabilityRuleEntity::getServiceType)
                .distinct()
                .sorted()
                .toList();
    }

    public Map<String, Object> supportContact() {
        return businessConfigRepository.findByConfigKeyAndActiveTrue("support_contact").stream()
                .findFirst()
                .map(BusinessConfigEntity::getConfigValue)
                .map(config -> Map.<String, Object>of(
                        "phone", config.path("phone").asText(),
                        "email", config.path("email").asText()
                ))
                .orElse(Map.of("phone", "", "email", ""));
    }

    public List<String> featureFlags() {
        return featureFlagRepository.findByEnabledTrue().stream()
                .map(FeatureFlagEntity::getFlagKey)
                .sorted()
                .toList();
    }
}
