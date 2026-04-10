package com.rydvrse.pricing.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.pricing.domain.CancellationPolicyEntity;
import com.rydvrse.pricing.domain.PricingPlanEntity;
import com.rydvrse.pricing.domain.RefundPolicyEntity;
import com.rydvrse.pricing.domain.TaxProfileEntity;
import com.rydvrse.pricing.infrastructure.CancellationPolicyRepository;
import com.rydvrse.pricing.infrastructure.PricingPlanRepository;
import com.rydvrse.pricing.infrastructure.RefundPolicyRepository;
import com.rydvrse.pricing.infrastructure.TaxProfileRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PricingPlanResolver {

    private final PricingPlanRepository pricingPlanRepository;
    private final TaxProfileRepository taxProfileRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;
    private final RefundPolicyRepository refundPolicyRepository;

    public PricingPlanResolver(
            PricingPlanRepository pricingPlanRepository,
            TaxProfileRepository taxProfileRepository,
            CancellationPolicyRepository cancellationPolicyRepository,
            RefundPolicyRepository refundPolicyRepository
    ) {
        this.pricingPlanRepository = pricingPlanRepository;
        this.taxProfileRepository = taxProfileRepository;
        this.cancellationPolicyRepository = cancellationPolicyRepository;
        this.refundPolicyRepository = refundPolicyRepository;
    }

    public PricingContext resolve(UUID cityId, OffsetDateTime scheduledPickupAt) {
        PricingPlanEntity pricingPlan = pricingPlanRepository.findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(cityId, "PUBLISHED", scheduledPickupAt)
                .stream().findFirst()
                .orElseThrow(() -> ApiException.unavailable(ErrorCode.DEPENDENCY_UNAVAILABLE, "No active pricing plan"));
        TaxProfileEntity taxProfile = taxProfileRepository.findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(cityId, "PUBLISHED", scheduledPickupAt)
                .stream().findFirst()
                .orElseThrow(() -> ApiException.unavailable(ErrorCode.DEPENDENCY_UNAVAILABLE, "No active tax profile"));
        CancellationPolicyEntity cancellationPolicy = cancellationPolicyRepository.findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(cityId, "PUBLISHED", scheduledPickupAt)
                .stream().findFirst()
                .orElseThrow(() -> ApiException.unavailable(ErrorCode.DEPENDENCY_UNAVAILABLE, "No active cancellation policy"));
        RefundPolicyEntity refundPolicy = refundPolicyRepository.findByCityIdAndStatusAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(cityId, "PUBLISHED", scheduledPickupAt)
                .stream().findFirst()
                .orElseThrow(() -> ApiException.unavailable(ErrorCode.DEPENDENCY_UNAVAILABLE, "No active refund policy"));
        return new PricingContext(pricingPlan, taxProfile, cancellationPolicy, refundPolicy);
    }

    public record PricingContext(
            PricingPlanEntity pricingPlan,
            TaxProfileEntity taxProfile,
            CancellationPolicyEntity cancellationPolicy,
            RefundPolicyEntity refundPolicy
    ) {
    }
}
