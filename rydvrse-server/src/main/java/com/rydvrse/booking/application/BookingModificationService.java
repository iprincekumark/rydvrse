package com.rydvrse.booking.application;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.domain.BookingInstructionEntity;
import com.rydvrse.booking.infrastructure.BookingInstructionRepository;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.pricing.application.QuoteService;
import com.rydvrse.pricing.domain.BookingFareSnapshotEntity;
import com.rydvrse.pricing.domain.QuoteEntity;
import com.rydvrse.pricing.infrastructure.BookingFareSnapshotRepository;
import com.rydvrse.pricing.infrastructure.QuoteComponentRepository;
import com.rydvrse.pricing.infrastructure.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingModificationService {

    private final BookingRepository bookingRepository;
    private final BookingQueryService bookingQueryService;
    private final BookingInstructionRepository bookingInstructionRepository;
    private final BookingFareSnapshotRepository bookingFareSnapshotRepository;
    private final BookingStateService bookingStateService;
    private final CustomerProfileService customerProfileService;
    private final QuoteService quoteService;
    private final QuoteRepository quoteRepository;
    private final QuoteComponentRepository quoteComponentRepository;
    private final IdempotencyService idempotencyService;

    public BookingModificationService(
            BookingRepository bookingRepository,
            BookingQueryService bookingQueryService,
            BookingInstructionRepository bookingInstructionRepository,
            BookingFareSnapshotRepository bookingFareSnapshotRepository,
            BookingStateService bookingStateService,
            CustomerProfileService customerProfileService,
            QuoteService quoteService,
            QuoteRepository quoteRepository,
            QuoteComponentRepository quoteComponentRepository,
            IdempotencyService idempotencyService
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingQueryService = bookingQueryService;
        this.bookingInstructionRepository = bookingInstructionRepository;
        this.bookingFareSnapshotRepository = bookingFareSnapshotRepository;
        this.bookingStateService = bookingStateService;
        this.customerProfileService = customerProfileService;
        this.quoteService = quoteService;
        this.quoteRepository = quoteRepository;
        this.quoteComponentRepository = quoteComponentRepository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public Map<String, Object> preview(UUID bookingId, ModificationCommand command) {
        BookingEntity booking = requireModifiableBooking(bookingId);
        if (!isModificationAllowed(booking)) {
            throw ApiException.unprocessable(ErrorCode.BOOKING_MODIFICATION_NOT_ALLOWED, "Booking modification is not allowed");
        }
        Map<String, Object> proposedQuote = quoteService.createQuote(toCreateQuoteCommand(booking, command));
        long fareDelta = (long) proposedQuote.get("total_paise") - booking.getCurrentTotalPaise();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("current_booking", bookingQueryService.toDetail(booking));
        response.put("proposed_quote", proposedQuote);
        response.put("fare_delta_paise", fareDelta);
        response.put("policy_notes", List.of(
                "Booking modifications are only supported before driver lock.",
                "A fresh pricing snapshot is created for every accepted modification."
        ));
        response.put("modification_allowed", true);
        return response;
    }

    @Transactional
    public Map<String, Object> apply(UUID bookingId, ModificationCommand command, UUID previewQuoteId, String idempotencyKey) {
        BookingEntity booking = requireModifiableBooking(bookingId);
        if (!isModificationAllowed(booking)) {
            throw ApiException.unprocessable(ErrorCode.BOOKING_MODIFICATION_NOT_ALLOWED, "Booking modification is not allowed");
        }
        String scope = "POST:/api/v1/bookings/" + bookingId + "/modify";
        return idempotencyService.checkExisting(scope, idempotencyKey, idempotencyService.hashPayload(Map.of("booking_id", bookingId, "preview_quote_id", previewQuoteId, "command", command)))
                .map(existing -> bookingQueryService.getMine(bookingId))
                .orElseGet(() -> {
                    CustomerProfileEntity customer = customerProfileService.requireCurrentProfile();
                    QuoteEntity previewQuote = quoteRepository.findByIdAndCustomerProfileId(previewQuoteId, customer.getId())
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Preview quote not found"));
                    if (!"ACTIVE".equals(previewQuote.getQuoteStatus()) || previewQuote.getExpiresAt().isBefore(OffsetDateTime.now())) {
                        throw ApiException.unprocessable(ErrorCode.QUOTE_EXPIRED, "Preview quote is no longer usable");
                    }

                    booking.setPickupAddressText(previewQuote.getPickupPayload().path("address_line_1").asText(booking.getPickupAddressText()));
                    booking.setDropAddressText(previewQuote.getDropPayload() == null ? null : previewQuote.getDropPayload().path("address_line_1").asText(null));
                    booking.setPickupLatitude(previewQuote.getPickupPayload().path("latitude").decimalValue());
                    booking.setPickupLongitude(previewQuote.getPickupPayload().path("longitude").decimalValue());
                    booking.setDropLatitude(previewQuote.getDropPayload() == null ? null : previewQuote.getDropPayload().path("latitude").decimalValue());
                    booking.setDropLongitude(previewQuote.getDropPayload() == null ? null : previewQuote.getDropPayload().path("longitude").decimalValue());
                    booking.setPickupZoneId(previewQuote.getPickupZoneId());
                    booking.setDropZoneId(previewQuote.getDropZoneId());
                    booking.setScheduledPickupAt(previewQuote.getScheduledPickupAt());
                    booking.setQuotedDurationMinutes(previewQuote.getQuotedDurationMinutes());
                    booking.setCurrentTotalPaise(previewQuote.getTotalPaise());

                    BookingFareSnapshotEntity snapshot = new BookingFareSnapshotEntity();
                    snapshot.setBookingId(booking.getId());
                    snapshot.setQuoteId(previewQuote.getId());
                    snapshot.setPricingPlanId(previewQuote.getPricingPlanId());
                    snapshot.setTaxProfileId(previewQuote.getTaxProfileId());
                    snapshot.setServiceType(previewQuote.getServiceType());
                    snapshot.setScheduledPickupAt(previewQuote.getScheduledPickupAt());
                    snapshot.setQuotedDurationMinutes(previewQuote.getQuotedDurationMinutes());
                    snapshot.setSubtotalPaise(previewQuote.getSubtotalPaise());
                    snapshot.setTaxPaise(previewQuote.getTaxPaise());
                    snapshot.setTotalPaise(previewQuote.getTotalPaise());
                    snapshot.setSnapshotPayload(com.rydvrse.common.persistence.ObjectMapperFactory.instance().valueToTree(Map.of(
                            "components", quoteComponentRepository.findByQuoteIdOrderBySortOrderAsc(previewQuote.getId()).stream().map(component -> Map.of(
                                    "code", component.getComponentType(),
                                    "label", component.getDisplayLabel(),
                                    "amount_paise", component.getAmountPaise(),
                                    "is_tax", component.isTax()
                            )).toList()
                    )));
                    bookingFareSnapshotRepository.save(snapshot);

                    booking.setFareSnapshotId(snapshot.getId());
                    if (command.customerNotes() != null && !command.customerNotes().isBlank()) {
                        BookingInstructionEntity note = new BookingInstructionEntity();
                        note.setBookingId(booking.getId());
                        note.setInstructionType("CUSTOMER_NOTE");
                        note.setInstructionText(command.customerNotes());
                        note.setCreatedByUserId(customer.getUserAccountId());
                        bookingInstructionRepository.save(note);
                    }
                    if (booking.getMetadata() instanceof ObjectNode metadata) {
                        metadata.put("last_modification_quote_id", previewQuote.getId().toString());
                    }
                    bookingStateService.transition(booking, "PENDING_ASSIGNMENT", "BOOKING_MODIFIED", "Booking details updated from preview quote");
                    bookingRepository.save(booking);
                    previewQuote.setQuoteStatus("BOOKED");
                    quoteRepository.save(previewQuote);
                    idempotencyService.store(scope, idempotencyKey, Map.of("preview_quote_id", previewQuoteId), "BOOKING", booking.getId(), "SUCCEEDED");
                    return bookingQueryService.getMine(bookingId);
                });
    }

    private BookingEntity requireModifiableBooking(UUID bookingId) {
        UUID customerId = customerProfileService.requireCurrentProfile().getId();
        return bookingRepository.findByIdAndCustomerProfileId(bookingId, customerId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
    }

    private boolean isModificationAllowed(BookingEntity booking) {
        return "PENDING_ASSIGNMENT".equals(booking.getStatus())
                && booking.getScheduledPickupAt().isAfter(OffsetDateTime.now().plusMinutes(30));
    }

    private QuoteService.CreateQuoteCommand toCreateQuoteCommand(BookingEntity booking, ModificationCommand command) {
        QuoteService.LocationInput pickup = new QuoteService.LocationInput(
                command.pickupLabel(),
                command.pickupAddressLine1() == null ? booking.getPickupAddressText() : command.pickupAddressLine1(),
                command.pickupAddressLine2(),
                command.pickupLandmark(),
                booking.getCityId(),
                command.pickupLatitude() == null ? booking.getPickupLatitude() : command.pickupLatitude(),
                command.pickupLongitude() == null ? booking.getPickupLongitude() : command.pickupLongitude()
        );
        QuoteService.LocationInput drop = null;
        if (booking.getDropAddressText() != null || command.dropAddressLine1() != null) {
            drop = new QuoteService.LocationInput(
                    command.dropLabel(),
                    command.dropAddressLine1() == null ? booking.getDropAddressText() : command.dropAddressLine1(),
                    command.dropAddressLine2(),
                    command.dropLandmark(),
                    booking.getCityId(),
                    command.dropLatitude() == null ? booking.getDropLatitude() : command.dropLatitude(),
                    command.dropLongitude() == null ? booking.getDropLongitude() : command.dropLongitude()
            );
        }
        return new QuoteService.CreateQuoteCommand(
                booking.getServiceType(),
                pickup,
                drop,
                command.scheduledPickupAt() == null ? booking.getScheduledPickupAt() : command.scheduledPickupAt(),
                command.expectedDurationMinutes() == null ? booking.getQuotedDurationMinutes() : command.expectedDurationMinutes(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                command.customerNotes()
        );
    }

    public record ModificationCommand(
            OffsetDateTime scheduledPickupAt,
            Integer expectedDurationMinutes,
            String pickupLabel,
            String pickupAddressLine1,
            String pickupAddressLine2,
            String pickupLandmark,
            java.math.BigDecimal pickupLatitude,
            java.math.BigDecimal pickupLongitude,
            String dropLabel,
            String dropAddressLine1,
            String dropAddressLine2,
            String dropLandmark,
            java.math.BigDecimal dropLatitude,
            java.math.BigDecimal dropLongitude,
            String customerNotes
    ) {
    }
}
