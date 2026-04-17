package com.rydvrse.pricing.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.master.application.ServiceabilityService;
import com.rydvrse.master.domain.AirportZoneBandEntity;
import com.rydvrse.master.domain.OneWayBandEntity;
import com.rydvrse.master.infrastructure.AirportZoneBandRepository;
import com.rydvrse.master.infrastructure.OneWayBandRepository;
import com.rydvrse.pricing.domain.PricingRuleEntity;
import com.rydvrse.pricing.domain.QuoteComponentEntity;
import com.rydvrse.pricing.domain.QuoteEntity;
import com.rydvrse.pricing.infrastructure.PricingRuleRepository;
import com.rydvrse.pricing.infrastructure.QuoteComponentRepository;
import com.rydvrse.pricing.infrastructure.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuoteService {

    private final CustomerProfileService customerProfileService;
    private final CurrentActorService currentActorService;
    private final ServiceabilityService serviceabilityService;
    private final AirportZoneBandRepository airportZoneBandRepository;
    private final OneWayBandRepository oneWayBandRepository;
    private final PricingPlanResolver pricingPlanResolver;
    private final PricingRuleRepository pricingRuleRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteComponentRepository quoteComponentRepository;
    private final JsonNodeUtils jsonNodeUtils;
    private final OutboxService outboxService;
    private final RydvrseProperties rydvrseProperties;
    private final BengaluruHybridFareCalculator bengaluruHybridFareCalculator;

    public QuoteService(
            CustomerProfileService customerProfileService,
            CurrentActorService currentActorService,
            ServiceabilityService serviceabilityService,
            AirportZoneBandRepository airportZoneBandRepository,
            OneWayBandRepository oneWayBandRepository,
            PricingPlanResolver pricingPlanResolver,
            PricingRuleRepository pricingRuleRepository,
            QuoteRepository quoteRepository,
            QuoteComponentRepository quoteComponentRepository,
            JsonNodeUtils jsonNodeUtils,
            OutboxService outboxService,
            RydvrseProperties rydvrseProperties,
            BengaluruHybridFareCalculator bengaluruHybridFareCalculator
    ) {
        this.customerProfileService = customerProfileService;
        this.currentActorService = currentActorService;
        this.serviceabilityService = serviceabilityService;
        this.airportZoneBandRepository = airportZoneBandRepository;
        this.oneWayBandRepository = oneWayBandRepository;
        this.pricingPlanResolver = pricingPlanResolver;
        this.pricingRuleRepository = pricingRuleRepository;
        this.quoteRepository = quoteRepository;
        this.quoteComponentRepository = quoteComponentRepository;
        this.jsonNodeUtils = jsonNodeUtils;
        this.outboxService = outboxService;
        this.rydvrseProperties = rydvrseProperties;
        this.bengaluruHybridFareCalculator = bengaluruHybridFareCalculator;
    }

    @Transactional
    public Map<String, Object> createQuote(CreateQuoteCommand command) {
        currentActorService.requireActor(ActorType.CUSTOMER);
        String serviceType = normalizeServiceType(command.serviceType());
        CustomerProfileEntity customerProfile = customerProfileService.requireCurrentProfile();
        ServiceabilityService.ServiceabilityResult serviceability = serviceabilityService.check(
                command.pickup().cityId(),
                serviceType,
                command.pickup().latitude(),
                command.pickup().longitude(),
                command.scheduledPickupAt()
        );
        ServiceabilityService.ZoneResolution dropZone = null;
        if (command.drop() != null) {
            dropZone = serviceabilityService.resolveZone(command.drop().cityId(), command.drop().latitude(), command.drop().longitude());
        }
        PricingPlanResolver.PricingContext pricingContext = pricingPlanResolver.resolve(command.pickup().cityId(), command.scheduledPickupAt());
        String leadTimeBucket = leadTimeBucket(command.scheduledPickupAt());

        UUID airportBandId = null;
        UUID oneWayBandId = null;
        List<PricingRuleEntity> rules = pricingRuleRepository.findByPricingPlanIdAndServiceTypeAndActiveTrue(pricingContext.pricingPlan().getId(), serviceType);
        List<LineItem> lineItems = new ArrayList<>();
        Map<String, Object> pricingMetadata = new LinkedHashMap<>();
        pricingMetadata.put("lead_time_bucket", leadTimeBucket);
        pricingMetadata.put("customer_notes", command.customerNotes() == null ? "" : command.customerNotes());

        if (bengaluruHybridFareCalculator.supports(serviceType)) {
            BengaluruHybridFareCalculator.HybridQuote hybridQuote = bengaluruHybridFareCalculator.calculate(new BengaluruHybridFareCalculator.HybridQuoteRequest(
                    serviceType,
                    command.roundedDistanceKm(),
                    command.predictedDriveMinutes(),
                    command.expectedDurationMinutes(),
                    command.driverPickupDistanceKm(),
                    command.driverPickupEtaMinutes(),
                    command.estimatedPickupCostPaise(),
                    command.transmissionType(),
                    command.carType(),
                    command.carBrandModel(),
                    command.carNumber(),
                    command.safetyAddonOpted() == null || command.safetyAddonOpted(),
                    isPeakWindow(command.scheduledPickupAt()),
                    isNight(command.scheduledPickupAt())
            ));
            hybridQuote.lineItems().forEach(item -> lineItems.add(new LineItem(item.code(), item.label(), item.amountPaise(), false, item.metadata())));
            pricingMetadata.put("commercial_model", hybridQuote.commercialModel());
            pricingMetadata.put("pricing_assumptions", hybridQuote.pricingAssumptions());
            pricingMetadata.put("driver_payout_preview", hybridQuote.driverPayoutPreview());
            pricingMetadata.put("savings_summary", hybridQuote.savingsSummary());
        } else if ("AIRPORT".equals(serviceType)) {
            airportBandId = airportZoneBandRepository.findByCityIdAndServiceZoneId(command.pickup().cityId(), serviceability.pickupZoneId())
                    .map(AirportZoneBandEntity::getId)
                    .orElseThrow(() -> ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Airport band unavailable"));
            PricingRuleEntity airportRule = pickRule(rules, "AIRPORT_FIXED", null, airportBandId, null, null);
            lineItems.add(new LineItem("AIRPORT_FIXED", "Airport fixed fare", airportRule.getAmountPaise(), false, Map.of("band_id", airportBandId)));
        } else {
            PricingRuleEntity baseRule = pickRule(rules, "BASE_90", serviceability.pickupZoneId(), null, null, leadTimeBucket);
            lineItems.add(new LineItem("BASE_90", "90 minute base fare", baseRule.getAmountPaise(), false, Map.of("zone_id", serviceability.pickupZoneId())));
            if (command.expectedDurationMinutes() != null && command.expectedDurationMinutes() > 90) {
                int extraBlocks = (int) Math.ceil((command.expectedDurationMinutes() - 90) / 30.0);
                PricingRuleEntity extensionRule = pickRule(rules, "EXTENSION_30", null, null, null, null);
                lineItems.add(new LineItem("EXTENSION_30", extraBlocks + " x 30 minute extension", extensionRule.getAmountPaise() * extraBlocks, false, Map.of("blocks", extraBlocks)));
            }
            if ("PRIORITY".equals(leadTimeBucket)) {
                PricingRuleEntity surchargeRule = pickRule(rules, "SURCHARGE", null, null, null, "PRIORITY");
                lineItems.add(new LineItem("PRIORITY_SURCHARGE", "Priority booking surcharge", surchargeRule.getAmountPaise(), false, Map.of("lead_time_bucket", leadTimeBucket)));
            }
            if ("SCHEDULED_ONE_WAY".equals(serviceType) && dropZone != null) {
                oneWayBandId = oneWayBandRepository.findByCityIdAndSourceZoneIdAndDestinationZoneId(
                                command.pickup().cityId(),
                                serviceability.pickupZoneId(),
                                dropZone.zoneId()
                        )
                        .map(OneWayBandEntity::getId)
                        .orElseThrow(() -> ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "One-way band unavailable"));
                PricingRuleEntity allowanceRule = pickRule(rules, "ONE_WAY_ALLOWANCE", null, null, oneWayBandId, null);
                lineItems.add(new LineItem("ONE_WAY_ALLOWANCE", "One-way return allowance", allowanceRule.getAmountPaise(), false, Map.of("band_id", oneWayBandId)));
            }
            if ("LATE_NIGHT".equals(serviceType) || isNight(command.scheduledPickupAt())) {
                PricingRuleEntity nightRule = pickRule(rules, "NIGHT_SURCHARGE", null, null, null, null);
                lineItems.add(new LineItem("NIGHT_SURCHARGE", "Night surcharge", nightRule.getAmountPaise(), false, Map.of()));
            }
        }

        long subtotal = lineItems.stream().mapToLong(LineItem::amountPaise).sum();
        long tax = pricingContext.taxProfile().getTaxPercent()
                .multiply(BigDecimal.valueOf(subtotal))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValue();
        lineItems.add(new LineItem("GST", "Tax", tax, true, Map.of("rate_percent", pricingContext.taxProfile().getTaxPercent())));
        long total = subtotal + tax;

        QuoteEntity quote = new QuoteEntity();
        quote.setCustomerProfileId(customerProfile.getId());
        quote.setCityId(command.pickup().cityId());
        quote.setServiceType(serviceType);
        quote.setPickupZoneId(serviceability.pickupZoneId());
        quote.setDropZoneId(dropZone == null ? null : dropZone.zoneId());
        quote.setAirportZoneBandId(airportBandId);
        quote.setOneWayBandId(oneWayBandId);
        quote.setPricingPlanId(pricingContext.pricingPlan().getId());
        quote.setTaxProfileId(pricingContext.taxProfile().getId());
        quote.setScheduledPickupAt(command.scheduledPickupAt());
        quote.setQuotedDurationMinutes(command.expectedDurationMinutes());
        quote.setPickupPayload(toLocationPayload(command.pickup(), serviceability.pickupZoneId()));
        quote.setDropPayload(command.drop() == null ? null : toLocationPayload(command.drop(), dropZone == null ? null : dropZone.zoneId()));
        quote.setCurrencyCode("INR");
        quote.setSubtotalPaise(subtotal);
        quote.setTaxPaise(tax);
        quote.setTotalPaise(total);
        quote.setQuoteStatus("ACTIVE");
        quote.setExpiresAt(OffsetDateTime.now().plusMinutes(rydvrseProperties.getPricing().getQuoteExpiryMinutes()));
        quote.setRequestedAt(OffsetDateTime.now());
        quote.setMetadata(jsonNodeUtils.toJsonNode(pricingMetadata));
        quoteRepository.save(quote);

        int sortOrder = 1;
        for (LineItem lineItem : lineItems) {
            QuoteComponentEntity component = new QuoteComponentEntity();
            component.setQuoteId(quote.getId());
            component.setComponentType(lineItem.code());
            component.setDisplayLabel(lineItem.label());
            component.setAmountPaise(lineItem.amountPaise());
            component.setSortOrder(sortOrder++);
            component.setTax(lineItem.tax());
            component.setComponentPayload(jsonNodeUtils.toJsonNode(lineItem.metadata()));
            quoteComponentRepository.save(component);
        }

        outboxService.publish("quote", quote.getId(), "QuoteCreatedEvent", Map.of("quote_id", quote.getId()));
        return buildQuoteResponse(quote, lineItems, pricingContext.cancellationPolicy().getPolicyPayload(), leadTimeBucket);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuote(UUID quoteId) {
        CustomerProfileEntity customerProfile = customerProfileService.requireCurrentProfile();
        QuoteEntity quote = quoteRepository.findByIdAndCustomerProfileId(quoteId, customerProfile.getId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Quote not found"));
        if ("ACTIVE".equals(quote.getQuoteStatus()) && quote.getExpiresAt().isBefore(OffsetDateTime.now())) {
            quote.setQuoteStatus("EXPIRED");
        }
        List<LineItem> lineItems = quoteComponentRepository.findByQuoteIdOrderBySortOrderAsc(quote.getId()).stream()
                .map(component -> new LineItem(component.getComponentType(), component.getDisplayLabel(), component.getAmountPaise(), component.isTax(), Map.of()))
                .toList();
        return buildQuoteResponse(quote, lineItems, JsonNodeFactory.instance.objectNode(), quote.getMetadata().path("lead_time_bucket").asText("FLEX"));
    }

    private Map<String, Object> buildQuoteResponse(QuoteEntity quote, List<LineItem> lineItems, JsonNode cancellationPolicy, String leadTimeBucket) {
        JsonNode metadata = quote.getMetadata() == null ? JsonNodeFactory.instance.objectNode() : quote.getMetadata();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("quote_id", quote.getId());
        response.put("status", quote.getQuoteStatus());
        response.put("service_type", quote.getServiceType());
        response.put("pickup", quote.getPickupPayload());
        response.put("drop", quote.getDropPayload());
        response.put("scheduled_pickup_at", quote.getScheduledPickupAt());
        response.put("expected_duration_minutes", quote.getQuotedDurationMinutes());
        response.put("lead_time_bucket", leadTimeBucket);
        response.put("pricing_plan_version", quote.getPricingPlanId().toString());
        response.put("commercial_model", metadata.path("commercial_model").asText("CONFIG_RULE_V1"));
        response.put("pricing_assumptions", metadata.path("pricing_assumptions"));
        response.put("components", lineItems.stream().map(lineItem -> Map.of(
                "code", lineItem.code(),
                "label", lineItem.label(),
                "amount_paise", lineItem.amountPaise(),
                "is_tax", lineItem.tax(),
                "metadata", lineItem.metadata()
        )).toList());
        response.put("subtotal_paise", quote.getSubtotalPaise());
        response.put("tax_paise", quote.getTaxPaise());
        response.put("total_paise", quote.getTotalPaise());
        response.put("driver_payout_preview", metadata.path("driver_payout_preview"));
        response.put("savings_summary", metadata.path("savings_summary"));
        response.put("valid_until", quote.getExpiresAt());
        response.put("cancellation_policy_summary", Map.of(
                "free_until", quote.getScheduledPickupAt().minusMinutes(cancellationPolicy.path("free_until_minutes_before_pickup").asLong(60)),
                "late_cancel_fee_paise", cancellationPolicy.path("late_cancel_fee_paise").asLong(0)
        ));
        response.put("serviceability_status", "SERVICEABLE");
        return response;
    }

    private PricingRuleEntity pickRule(
            List<PricingRuleEntity> rules,
            String ruleType,
            UUID serviceZoneId,
            UUID airportBandId,
            UUID oneWayBandId,
            String leadTimeBucket
    ) {
        return rules.stream()
                .filter(rule -> ruleType.equals(rule.getRuleType()))
                .filter(rule -> serviceZoneId == null || serviceZoneId.equals(rule.getServiceZoneId()) || rule.getServiceZoneId() == null)
                .filter(rule -> airportBandId == null || airportBandId.equals(rule.getAirportZoneBandId()) || rule.getAirportZoneBandId() == null)
                .filter(rule -> oneWayBandId == null || oneWayBandId.equals(rule.getOneWayBandId()) || rule.getOneWayBandId() == null)
                .filter(rule -> leadTimeBucket == null || leadTimeBucket.equals(rule.getLeadTimeBucket()) || rule.getLeadTimeBucket() == null)
                .sorted(Comparator.comparing((PricingRuleEntity rule) -> rule.getServiceZoneId() == null ? 1 : 0)
                        .thenComparing(rule -> rule.getAirportZoneBandId() == null ? 1 : 0)
                        .thenComparing(rule -> rule.getOneWayBandId() == null ? 1 : 0)
                        .thenComparing(rule -> rule.getLeadTimeBucket() == null ? 1 : 0))
                .findFirst()
                .orElseThrow(() -> ApiException.unprocessable(ErrorCode.SERVICEABILITY_UNAVAILABLE, "Pricing rule missing for request"));
    }

    private JsonNode toLocationPayload(LocationInput input, UUID zoneId) {
        return jsonNodeUtils.toJsonNode(Map.of(
                "label", input.label() == null ? "" : input.label(),
                "address_line_1", input.addressLine1(),
                "address_line_2", input.addressLine2() == null ? "" : input.addressLine2(),
                "landmark", input.landmark() == null ? "" : input.landmark(),
                "city_id", input.cityId(),
                "zone_id", zoneId,
                "latitude", input.latitude(),
                "longitude", input.longitude()
        ));
    }

    private boolean isNight(OffsetDateTime scheduledPickupAt) {
        int hour = scheduledPickupAt.atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata")).getHour();
        return hour >= 22 || hour < 6;
    }

    private boolean isPeakWindow(OffsetDateTime scheduledPickupAt) {
        int hour = scheduledPickupAt.atZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata")).getHour();
        return (hour >= 8 && hour < 11) || (hour >= 17 && hour < 20);
    }

    private String leadTimeBucket(OffsetDateTime scheduledPickupAt) {
        long leadMinutes = Duration.between(OffsetDateTime.now(), scheduledPickupAt).toMinutes();
        if (leadMinutes >= 180) {
            return "FLEX";
        }
        if (leadMinutes >= 60) {
            return "PRIORITY";
        }
        return "EXPRESS";
    }

    public record CreateQuoteCommand(
            String serviceType,
            LocationInput pickup,
            LocationInput drop,
            OffsetDateTime scheduledPickupAt,
            Integer expectedDurationMinutes,
            Integer roundedDistanceKm,
            Integer predictedDriveMinutes,
            Integer driverPickupDistanceKm,
            Integer driverPickupEtaMinutes,
            Integer estimatedPickupCostPaise,
            String transmissionType,
            String carType,
            String carBrandModel,
            String carNumber,
            Integer roundTripWaitMinutes,
            Boolean safetyAddonOpted,
            String customerNotes
    ) {
    }

    public record LocationInput(
            String label,
            String addressLine1,
            String addressLine2,
            String landmark,
            UUID cityId,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }

    private record LineItem(String code, String label, long amountPaise, boolean tax, Map<String, Object> metadata) {
    }

    private String normalizeServiceType(String serviceType) {
        return switch (serviceType == null ? "" : serviceType) {
            case "ONE_WAY_DROP" -> "SCHEDULED_ONE_WAY";
            case "ROUND_TRIP" -> "SCHEDULED_ROUND_TRIP";
            case "LATE_NIGHT_SAFE_RETURN" -> "LATE_NIGHT";
            default -> serviceType;
        };
    }
}
