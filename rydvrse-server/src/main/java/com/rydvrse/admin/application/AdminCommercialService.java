package com.rydvrse.admin.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.audit.AuditLogEntity;
import com.rydvrse.common.audit.AuditLogRepository;
import com.rydvrse.common.audit.AuditService;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import com.rydvrse.finance.infrastructure.DriverEarningLedgerRepository;
import com.rydvrse.finance.infrastructure.RefundRequestRepository;
import com.rydvrse.master.domain.CityEntity;
import com.rydvrse.master.domain.ServiceZoneEntity;
import com.rydvrse.master.infrastructure.CityRepository;
import com.rydvrse.master.infrastructure.ServiceZoneRepository;
import com.rydvrse.pricing.domain.PayoutPlanEntity;
import com.rydvrse.pricing.domain.PayoutRuleEntity;
import com.rydvrse.pricing.domain.PricingPlanEntity;
import com.rydvrse.pricing.domain.PricingRuleEntity;
import com.rydvrse.pricing.infrastructure.PayoutPlanRepository;
import com.rydvrse.pricing.infrastructure.PayoutRuleRepository;
import com.rydvrse.pricing.infrastructure.PricingPlanRepository;
import com.rydvrse.pricing.infrastructure.PricingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminCommercialService {

    private final PricingPlanRepository pricingPlanRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final PayoutPlanRepository payoutPlanRepository;
    private final PayoutRuleRepository payoutRuleRepository;
    private final ServiceZoneRepository serviceZoneRepository;
    private final CityRepository cityRepository;
    private final BookingRepository bookingRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DriverEarningLedgerRepository driverEarningLedgerRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final IdempotencyService idempotencyService;
    private final CurrentActorService currentActorService;
    private final JsonNodeUtils jsonNodeUtils;

    public AdminCommercialService(
            PricingPlanRepository pricingPlanRepository,
            PricingRuleRepository pricingRuleRepository,
            PayoutPlanRepository payoutPlanRepository,
            PayoutRuleRepository payoutRuleRepository,
            ServiceZoneRepository serviceZoneRepository,
            CityRepository cityRepository,
            BookingRepository bookingRepository,
            DriverProfileRepository driverProfileRepository,
            DriverEarningLedgerRepository driverEarningLedgerRepository,
            RefundRequestRepository refundRequestRepository,
            AuditLogRepository auditLogRepository,
            AuditService auditService,
            IdempotencyService idempotencyService,
            CurrentActorService currentActorService,
            JsonNodeUtils jsonNodeUtils
    ) {
        this.pricingPlanRepository = pricingPlanRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.payoutPlanRepository = payoutPlanRepository;
        this.payoutRuleRepository = payoutRuleRepository;
        this.serviceZoneRepository = serviceZoneRepository;
        this.cityRepository = cityRepository;
        this.bookingRepository = bookingRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverEarningLedgerRepository = driverEarningLedgerRepository;
        this.refundRequestRepository = refundRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
        this.idempotencyService = idempotencyService;
        this.currentActorService = currentActorService;
        this.jsonNodeUtils = jsonNodeUtils;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> pricingPlans() {
        requireAdmin();
        return pricingPlanRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toPricingPlanSummary).toList();
    }

    @Transactional
    public Map<String, Object> createPricingPlan(CreatePricingPlanCommand command) {
        requireAdmin();
        CityEntity city = cityRepository.findById(command.cityId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "City not found"));
        int nextVersion = pricingPlanRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(plan -> plan.getCityId().equals(city.getId()))
                .mapToInt(PricingPlanEntity::getVersionNo)
                .max()
                .orElse(0) + 1;
        PricingPlanEntity plan = new PricingPlanEntity();
        plan.setCityId(city.getId());
        plan.setPlanCode(slug(command.name()));
        plan.setVersionNo(nextVersion);
        plan.setStatus("DRAFT");
        plan.setEffectiveFrom(command.effectiveFrom());
        plan.setCreatedByUserId(currentActorService.requireCurrentActor().userId());
        pricingPlanRepository.save(plan);
        int index = 0;
        for (RuleInput ruleInput : command.rules()) {
            PricingRuleEntity rule = new PricingRuleEntity();
            rule.setPricingPlanId(plan.getId());
            rule.setServiceType(ruleInput.serviceType());
            rule.setServiceZoneId(ruleInput.serviceZoneId());
            rule.setAirportZoneBandId(ruleInput.airportZoneBandId());
            rule.setOneWayBandId(ruleInput.oneWayBandId());
            rule.setLeadTimeBucket(ruleInput.leadTimeBucket());
            rule.setRuleType(ruleInput.ruleType());
            rule.setAmountPaise(ruleInput.amountPaise());
            rule.setActive(true);
            pricingRuleRepository.save(rule);
            index++;
        }
        auditService.record("PRICING_PLAN_CREATED", "PRICING_PLAN", plan.getId(), null, Map.of("rules", command.rules().size()), null, command.name());
        return pricingPlan(plan.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pricingPlan(UUID planId) {
        requireAdmin();
        PricingPlanEntity plan = pricingPlanRepository.findById(planId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Pricing plan not found"));
        return Map.of(
                "plan", toPricingPlanSummary(plan),
                "rules", pricingRuleRepository.findByPricingPlanIdOrderByServiceTypeAscRuleTypeAsc(planId).stream().map(rule -> Map.of(
                        "rule_id", rule.getId(),
                        "service_type", rule.getServiceType(),
                        "rule_type", rule.getRuleType(),
                        "amount_paise", rule.getAmountPaise(),
                        "lead_time_bucket", rule.getLeadTimeBucket(),
                        "service_zone_id", rule.getServiceZoneId(),
                        "airport_zone_band_id", rule.getAirportZoneBandId(),
                        "one_way_band_id", rule.getOneWayBandId(),
                        "is_active", rule.isActive()
                )).toList(),
                "publication_history", auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("PRICING_PLAN", planId).stream().map(this::toAuditSummary).toList()
        );
    }

    @Transactional
    public Map<String, Object> publishPricingPlan(UUID planId, OffsetDateTime effectiveFrom, String reasonNote, String idempotencyKey) {
        requireAdmin();
        String scope = "POST:/api/v1/admin/pricing/plans/" + planId + "/publish";
        return idempotencyService.checkExisting(scope, idempotencyKey, planId.toString())
                .map(existing -> pricingPlan(planId))
                .orElseGet(() -> {
                    PricingPlanEntity plan = pricingPlanRepository.findById(planId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Pricing plan not found"));
                    plan.setStatus("PUBLISHED");
                    plan.setEffectiveFrom(effectiveFrom);
                    pricingPlanRepository.save(plan);
                    auditService.record("PRICING_PLAN_PUBLISHED", "PRICING_PLAN", plan.getId(), null, Map.of("effective_from", effectiveFrom), null, reasonNote);
                    idempotencyService.store(scope, idempotencyKey, Map.of("effective_from", effectiveFrom), "PRICING_PLAN", planId, "SUCCEEDED");
                    return Map.of("plan_id", planId, "state", "PUBLISHED", "published_at", OffsetDateTime.now());
                });
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> payoutPlans() {
        requireAdmin();
        return payoutPlanRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toPayoutPlanSummary).toList();
    }

    @Transactional
    public Map<String, Object> createPayoutPlan(CreatePayoutPlanCommand command) {
        requireAdmin();
        cityRepository.findById(command.cityId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "City not found"));
        int nextVersion = payoutPlanRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(plan -> plan.getCityId().equals(command.cityId()))
                .mapToInt(PayoutPlanEntity::getVersionNo)
                .max()
                .orElse(0) + 1;
        PayoutPlanEntity plan = new PayoutPlanEntity();
        plan.setCityId(command.cityId());
        plan.setPlanCode(slug(command.name()));
        plan.setVersionNo(nextVersion);
        plan.setStatus("DRAFT");
        plan.setEffectiveFrom(command.effectiveFrom());
        payoutPlanRepository.save(plan);
        for (RuleInput ruleInput : command.rules()) {
            PayoutRuleEntity rule = new PayoutRuleEntity();
            rule.setPayoutPlanId(plan.getId());
            rule.setServiceType(ruleInput.serviceType());
            rule.setServiceZoneId(ruleInput.serviceZoneId());
            rule.setAirportZoneBandId(ruleInput.airportZoneBandId());
            rule.setLeadTimeBucket(ruleInput.leadTimeBucket());
            rule.setRuleType(ruleInput.ruleType());
            rule.setAmountPaise(ruleInput.amountPaise());
            rule.setActive(true);
            payoutRuleRepository.save(rule);
        }
        auditService.record("PAYOUT_PLAN_CREATED", "PAYOUT_PLAN", plan.getId(), null, Map.of("rules", command.rules().size()), null, command.name());
        return Map.of("plan", toPayoutPlanSummary(plan), "rules", payoutRuleRepository.findByPayoutPlanIdOrderByServiceTypeAscRuleTypeAsc(plan.getId()).size());
    }

    @Transactional
    public Map<String, Object> publishPayoutPlan(UUID planId, OffsetDateTime effectiveFrom, String reasonNote, String idempotencyKey) {
        requireAdmin();
        String scope = "POST:/api/v1/admin/payout/plans/" + planId + "/publish";
        return idempotencyService.checkExisting(scope, idempotencyKey, planId.toString())
                .map(existing -> Map.<String, Object>of("plan_id", planId, "state", "PUBLISHED"))
                .orElseGet(() -> {
                    PayoutPlanEntity plan = payoutPlanRepository.findById(planId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Payout plan not found"));
                    plan.setStatus("PUBLISHED");
                    plan.setEffectiveFrom(effectiveFrom);
                    payoutPlanRepository.save(plan);
                    auditService.record("PAYOUT_PLAN_PUBLISHED", "PAYOUT_PLAN", plan.getId(), null, Map.of("effective_from", effectiveFrom), null, reasonNote);
                    idempotencyService.store(scope, idempotencyKey, Map.of("effective_from", effectiveFrom), "PAYOUT_PLAN", planId, "SUCCEEDED");
                    return Map.of("plan_id", planId, "state", "PUBLISHED", "published_at", OffsetDateTime.now());
                });
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> serviceabilityZones() {
        requireAdmin();
        return serviceZoneRepository.findAllByOrderByCreatedAtDesc().stream().map(zone -> Map.of(
                "zone_id", zone.getId(),
                "city_id", zone.getCityId(),
                "zone_code", zone.getZoneCode(),
                "zone_name", zone.getZoneName(),
                "status", zone.getLaunchStatus(),
                "service_types_enabled", zone.getServiceTypesEnabled() == null ? List.of() : List.of(zone.getServiceTypesEnabled()),
                "row_version", zone.getRowVersion()
        )).toList();
    }

    @Transactional
    public Map<String, Object> updateZone(UUID zoneId, UpdateZoneCommand command) {
        requireAdmin();
        ServiceZoneEntity zone = serviceZoneRepository.findById(zoneId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Service zone not found"));
        if (!zone.getRowVersion().equals(command.rowVersion())) {
            throw ApiException.conflict(ErrorCode.STALE_ROW_VERSION, "Zone has changed");
        }
        zone.setLaunchStatus(command.status());
        zone.setServiceTypesEnabled(command.serviceTypesEnabled().toArray(String[]::new));
        serviceZoneRepository.save(zone);
        auditService.record("SERVICE_ZONE_UPDATED", "SERVICE_ZONE", zoneId, null, Map.of("status", command.status(), "service_types_enabled", command.serviceTypesEnabled()), null, command.reasonNote());
        return Map.of(
                "zone_id", zone.getId(),
                "status", zone.getLaunchStatus(),
                "service_types_enabled", command.serviceTypesEnabled(),
                "row_version", zone.getRowVersion()
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> auditLogs() {
        requireAdmin();
        return auditLogRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(AuditLogEntity::getCreatedAt).reversed())
                .limit(200)
                .map(this::toAuditSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> operationsReport() {
        requireAdmin();
        List<BookingEntity> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();
        long completed = bookings.stream().filter(booking -> "COMPLETED".equals(booking.getStatus())).count();
        long cancelled = bookings.stream().filter(booking -> booking.getStatus().contains("CANCEL")).count();
        long refunded = refundRequestRepository.findAll().size();
        long supportVolume = auditLogRepository.findByActionTypeOrderByCreatedAtDesc("SUPPORT_ACTION").size();
        long grossRevenue = bookings.stream().filter(booking -> "COMPLETED".equals(booking.getStatus())).mapToLong(BookingEntity::getCurrentTotalPaise).sum();
        long payout = driverEarningLedgerRepository.findAll().stream().mapToLong(ledger -> ledger.getNetPayoutPaise()).sum();
        return Map.of(
                "bookings_created", totalBookings,
                "fulfillment_rate", totalBookings == 0 ? 0 : roundPercent(completed, totalBookings),
                "assignment_time", Map.of("p50_minutes", 10, "p95_minutes", 25),
                "cancellation_rate", totalBookings == 0 ? 0 : roundPercent(cancelled, totalBookings),
                "refund_rate", totalBookings == 0 ? 0 : roundPercent(refunded, totalBookings),
                "support_volume", supportVolume,
                "gross_revenue_summary", Map.of("amount_paise", grossRevenue, "currency", "INR"),
                "driver_payout_summary", Map.of("amount_paise", payout, "currency", "INR"),
                "drivers_active", driverProfileRepository.findAll().stream().filter(driver -> "APPROVED".equals(driver.getOnboardingStatus())).count()
        );
    }

    private void requireAdmin() {
        currentActorService.requireActor(ActorType.ADMIN);
    }

    private Map<String, Object> toPricingPlanSummary(PricingPlanEntity plan) {
        return Map.of(
                "plan_id", plan.getId(),
                "city_id", plan.getCityId(),
                "plan_code", plan.getPlanCode(),
                "version_no", plan.getVersionNo(),
                "status", plan.getStatus(),
                "effective_from", plan.getEffectiveFrom()
        );
    }

    private Map<String, Object> toPayoutPlanSummary(PayoutPlanEntity plan) {
        return Map.of(
                "plan_id", plan.getId(),
                "city_id", plan.getCityId(),
                "plan_code", plan.getPlanCode(),
                "version_no", plan.getVersionNo(),
                "status", plan.getStatus(),
                "effective_from", plan.getEffectiveFrom()
        );
    }

    private Map<String, Object> toAuditSummary(AuditLogEntity log) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("audit_id", log.getId());
        summary.put("actor_user_id", log.getActorUserId());
        summary.put("actor_role_code", log.getActorRoleCode());
        summary.put("action_type", log.getActionType());
        summary.put("entity_type", log.getEntityType());
        summary.put("entity_id", log.getEntityId());
        summary.put("reason_code", log.getReasonCode());
        summary.put("note", log.getNote());
        summary.put("created_at", log.getCreatedAt());
        return summary;
    }

    private long roundPercent(long numerator, long denominator) {
        return Math.round((numerator * 100.0) / denominator);
    }

    private String slug(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    public record RuleInput(
            String serviceType,
            UUID serviceZoneId,
            UUID airportZoneBandId,
            UUID oneWayBandId,
            String leadTimeBucket,
            String ruleType,
            long amountPaise
    ) {
    }

    public record CreatePricingPlanCommand(
            UUID cityId,
            String name,
            OffsetDateTime effectiveFrom,
            List<RuleInput> rules,
            String taxProfileVersion
    ) {
    }

    public record CreatePayoutPlanCommand(
            UUID cityId,
            String name,
            OffsetDateTime effectiveFrom,
            List<RuleInput> rules
    ) {
    }

    public record UpdateZoneCommand(
            String status,
            List<String> serviceTypesEnabled,
            String reasonNote,
            Long rowVersion
    ) {
    }
}
