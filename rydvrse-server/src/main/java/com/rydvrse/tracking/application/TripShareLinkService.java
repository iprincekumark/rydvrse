package com.rydvrse.tracking.application;

import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.util.HashingUtils;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.trip.domain.TripEntity;
import com.rydvrse.trip.domain.TripShareLinkEntity;
import com.rydvrse.trip.infrastructure.TripRepository;
import com.rydvrse.trip.infrastructure.TripShareLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class TripShareLinkService {

    private final TripRepository tripRepository;
    private final TripShareLinkRepository tripShareLinkRepository;
    private final CustomerProfileService customerProfileService;

    public TripShareLinkService(
            TripRepository tripRepository,
            TripShareLinkRepository tripShareLinkRepository,
            CustomerProfileService customerProfileService
    ) {
        this.tripRepository = tripRepository;
        this.tripShareLinkRepository = tripShareLinkRepository;
        this.customerProfileService = customerProfileService;
    }

    @Transactional
    public Map<String, Object> createShareLink(UUID tripId, Integer expiresInMinutes) {
        UUID customerId = customerProfileService.requireCurrentProfile().getId();
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Trip not found"));
        if (!customerId.equals(trip.getCustomerProfileId())) {
            throw ApiException.forbidden(ErrorCode.FORBIDDEN, "Trip not visible");
        }

        String rawToken = "shr_" + UUID.randomUUID();
        TripShareLinkEntity shareLink = new TripShareLinkEntity();
        shareLink.setTripId(tripId);
        shareLink.setCustomerProfileId(customerId);
        shareLink.setShareTokenHash(HashingUtils.sha256(rawToken));
        shareLink.setStatus("ACTIVE");
        shareLink.setExpiresAt(OffsetDateTime.now().plusMinutes(expiresInMinutes == null ? 60 : expiresInMinutes));
        tripShareLinkRepository.save(shareLink);

        return Map.of(
                "share_url", "https://share.rydvrse.local/trips/" + tripId + "?token=" + rawToken,
                "expires_at", shareLink.getExpiresAt()
        );
    }
}
