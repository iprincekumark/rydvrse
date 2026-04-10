package com.rydvrse.booking.application;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingRatingService {

    private final BookingRepository bookingRepository;
    private final CustomerProfileService customerProfileService;
    private final AssignmentRepository assignmentRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final IdempotencyService idempotencyService;

    public BookingRatingService(
            BookingRepository bookingRepository,
            CustomerProfileService customerProfileService,
            AssignmentRepository assignmentRepository,
            DriverProfileRepository driverProfileRepository,
            IdempotencyService idempotencyService
    ) {
        this.bookingRepository = bookingRepository;
        this.customerProfileService = customerProfileService;
        this.assignmentRepository = assignmentRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public Map<String, Object> submit(UUID bookingId, int rating, List<String> tags, String comment, String idempotencyKey) {
        if (rating < 1 || rating > 5) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Rating must be between 1 and 5");
        }
        String scope = "POST:/api/v1/bookings/" + bookingId + "/ratings";
        return idempotencyService.checkExisting(scope, idempotencyKey, idempotencyService.hashPayload(Map.of("rating", rating, "tags", tags, "comment", comment)))
                .map(existing -> Map.<String, Object>of("booking_id", bookingId, "rating", rating))
                .orElseGet(() -> {
                    UUID customerId = customerProfileService.requireCurrentProfile().getId();
                    BookingEntity booking = bookingRepository.findByIdAndCustomerProfileId(bookingId, customerId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
                    if (!List.of("COMPLETED", "COMPLETED_PAYMENT_PENDING").contains(booking.getStatus())) {
                        throw ApiException.unprocessable(ErrorCode.STATE_CONFLICT, "Rating is only available after completion");
                    }
                    if (booking.getMetadata() instanceof ObjectNode metadata && metadata.has("customer_rating")) {
                        throw ApiException.conflict(ErrorCode.STATE_CONFLICT, "Booking is already rated");
                    }
                    UUID driverId = assignmentRepository.findById(booking.getCurrentAssignmentId())
                            .map(assignment -> assignment.getDriverProfileId())
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Assigned driver not found"));
                    DriverProfileEntity driver = driverProfileRepository.findById(driverId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Driver not found"));

                    ObjectNode ratingNode = (ObjectNode) booking.getMetadata();
                    ratingNode.put("customer_rating", rating);
                    ratingNode.put("rated_at", OffsetDateTime.now().toString());
                    ratingNode.put("comment", comment == null ? "" : comment);
                    ArrayNode tagsNode = ratingNode.putArray("tags");
                    (tags == null ? List.<String>of() : tags).forEach(tagsNode::add);
                    bookingRepository.save(booking);

                    int currentCount = driver.getRatingCount();
                    BigDecimal currentAverage = driver.getRatingAvg() == null ? BigDecimal.ZERO : driver.getRatingAvg();
                    BigDecimal nextAverage = currentAverage.multiply(BigDecimal.valueOf(currentCount))
                            .add(BigDecimal.valueOf(rating))
                            .divide(BigDecimal.valueOf(currentCount + 1L), 2, RoundingMode.HALF_UP);
                    driver.setRatingCount(currentCount + 1);
                    driver.setRatingAvg(nextAverage);
                    driverProfileRepository.save(driver);

                    idempotencyService.store(scope, idempotencyKey, Map.of("rating", rating, "tags", tags, "comment", comment), "BOOKING", bookingId, "SUCCEEDED");
                    return Map.of(
                            "booking_id", bookingId,
                            "rating", rating,
                            "tags", tags == null ? List.of() : tags,
                            "comment", comment,
                            "driver_rating_summary", Map.of(
                                    "driver_id", driverId,
                                    "rating_avg", driver.getRatingAvg(),
                                    "rating_count", driver.getRatingCount()
                            )
                    );
                });
    }
}
