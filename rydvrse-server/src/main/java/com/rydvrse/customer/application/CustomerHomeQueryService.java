package com.rydvrse.customer.application;

import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.master.application.ConfigReadService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomerHomeQueryService {

    private final CustomerProfileService customerProfileService;
    private final UserAccountRepository userAccountRepository;
    private final ConfigReadService configReadService;

    public CustomerHomeQueryService(
            CustomerProfileService customerProfileService,
            UserAccountRepository userAccountRepository,
            ConfigReadService configReadService
    ) {
        this.customerProfileService = customerProfileService;
        this.userAccountRepository = userAccountRepository;
        this.configReadService = configReadService;
    }

    public Map<String, Object> getHome() {
        CustomerProfileEntity profile = customerProfileService.requireCurrentProfile();
        String mobile = userAccountRepository.findById(profile.getUserAccountId()).map(account -> account.getMobileNumberE164()).orElse(null);
        return Map.of(
                "customer_profile", customerProfileService.toProfileMap(profile, mobile),
                "supported_service_types", configReadService.supportedServiceTypes(profile.getDefaultCityId()),
                "upcoming_bookings", List.of(),
                "recent_bookings", List.of(),
                "help_and_safety", Map.of(
                        "support_contact", configReadService.supportContact(),
                        "quick_actions", List.of("HELP", "SOS", "TRACK_TRIP")
                )
        );
    }
}
