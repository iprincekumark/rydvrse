package com.rydvrse.master.api;

import com.rydvrse.common.api.ApiResponse;
import com.rydvrse.master.application.ConfigReadService;
import com.rydvrse.master.application.ServiceabilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final ConfigReadService configReadService;
    private final ServiceabilityService serviceabilityService;

    public ConfigController(ConfigReadService configReadService, ServiceabilityService serviceabilityService) {
        this.configReadService = configReadService;
        this.serviceabilityService = serviceabilityService;
    }

    @GetMapping("/bootstrap")
    public ApiResponse<Map<String, Object>> bootstrap(
            @RequestParam(required = false) String actorType,
            @RequestParam(required = false) UUID cityId
    ) {
        UUID effectiveCityId = cityId == null ? UUID.fromString("20000000-0000-0000-0000-000000000001") : cityId;
        return ApiResponse.of(configReadService.bootstrap(actorType, effectiveCityId), null);
    }

    @PostMapping("/serviceability/check")
    public ApiResponse<Map<String, Object>> check(@Valid @RequestBody ServiceabilityCheckRequest request) {
        ServiceabilityService.ServiceabilityResult result = serviceabilityService.check(
                request.pickup().cityId(),
                request.serviceType(),
                request.pickup().latitude(),
                request.pickup().longitude(),
                request.scheduledPickupAt()
        );
        return ApiResponse.of(serviceabilityService.toResponse(result), null);
    }

    public record ServiceabilityCheckRequest(
            @NotBlank String serviceType,
            @NotNull ServiceabilityLocation pickup,
            ServiceabilityLocation drop,
            @NotNull OffsetDateTime scheduledPickupAt
    ) {
    }

    public record ServiceabilityLocation(
            @NotNull UUID cityId,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude
    ) {
    }
}
