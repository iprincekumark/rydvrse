package com.rydvrse.booking.application;

import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.domain.BookingInstructionEntity;
import com.rydvrse.booking.domain.BookingPassengerContextEntity;
import com.rydvrse.booking.infrastructure.BookingInstructionRepository;
import com.rydvrse.booking.infrastructure.BookingPassengerContextRepository;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.common.persistence.ObjectMapperFactory;
import com.rydvrse.pricing.domain.BookingFareSnapshotEntity;
import com.rydvrse.pricing.domain.QuoteComponentEntity;
import com.rydvrse.pricing.domain.QuoteEntity;
import com.rydvrse.pricing.infrastructure.BookingFareSnapshotRepository;
import com.rydvrse.pricing.infrastructure.QuoteComponentRepository;
import com.rydvrse.pricing.infrastructure.QuoteRepository;
import com.rydvrse.pricing.application.PricingPlanResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingCreationService {

    private final QuoteRepository quoteRepository;
    private final QuoteComponentRepository quoteComponentRepository;
    private final BookingRepository bookingRepository;
    private final BookingPassengerContextRepository bookingPassengerContextRepository;
    private final BookingInstructionRepository bookingInstructionRepository;
    private final BookingFareSnapshotRepository bookingFareSnapshotRepository;
    private final AssignmentRepository assignmentRepository;
    private final BookingStateService bookingStateService;
    private final CustomerProfileService customerProfileService;
    private final PricingPlanResolver pricingPlanResolver;
    private final CurrentActorService currentActorService;
    private final IdempotencyService idempotencyService;
    private final OutboxService outboxService;

    public BookingCreationService(
            QuoteRepository quoteRepository,
            QuoteComponentRepository quoteComponentRepository,
            BookingRepository bookingRepository,
            BookingPassengerContextRepository bookingPassengerContextRepository,
            BookingInstructionRepository bookingInstructionRepository,
            BookingFareSnapshotRepository bookingFareSnapshotRepository,
            AssignmentRepository assignmentRepository,
            BookingStateService bookingStateService,
            CustomerProfileService customerProfileService,
            PricingPlanResolver pricingPlanResolver,
            CurrentActorService currentActorService,
            IdempotencyService idempotencyService,
            OutboxService outboxService
    ) {
        this.quoteRepository = quoteRepository;
        this.quoteComponentRepository = quoteComponentRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPassengerContextRepository = bookingPassengerContextRepository;
        this.bookingInstructionRepository = bookingInstructionRepository;
        this.bookingFareSnapshotRepository = bookingFareSnapshotRepository;
        this.assignmentRepository = assignmentRepository;
        this.bookingStateService = bookingStateService;
        this.customerProfileService = customerProfileService;
        this.pricingPlanResolver = pricingPlanResolver;
        this.currentActorService = currentActorService;
        this.idempotencyService = idempotencyService;
        this.outboxService = outboxService;
    }

    @Transactional
    public UUID createBooking(CreateBookingCommand command, String idempotencyKey) {
        currentActorService.requireActor(ActorType.CUSTOMER);
        CustomerProfileEntity customerProfile = customerProfileService.requireCurrentProfile();
        String scope = "POST:/api/v1/bookings:" + customerProfile.getId();
        String payloadHash = idempotencyService.hashPayload(command);
        return idempotencyService.checkExisting(scope, idempotencyKey, payloadHash)
                .map(IdempotencyService.StoredResponseReference::responseReferenceId)
                .orElseGet(() -> createFreshBooking(command, customerProfile, scope, idempotencyKey));
    }

    private UUID createFreshBooking(CreateBookingCommand command, CustomerProfileEntity customerProfile, String scope, String idempotencyKey) {
        QuoteEntity quote = quoteRepository.findByIdAndCustomerProfileId(command.quoteId(), customerProfile.getId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Quote not found"));
        if (!"ACTIVE".equals(quote.getQuoteStatus()) || quote.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw ApiException.unprocessable(ErrorCode.QUOTE_EXPIRED, "Quote expired");
        }
        PricingPlanResolver.PricingContext pricingContext = pricingPlanResolver.resolve(quote.getCityId(), quote.getScheduledPickupAt());

        BookingEntity booking = new BookingEntity();
        booking.setBookingCode("RYD-" + System.currentTimeMillis());
        booking.setCustomerProfileId(customerProfile.getId());
        booking.setCityId(quote.getCityId());
        booking.setServiceType(quote.getServiceType());
        booking.setPickupZoneId(quote.getPickupZoneId());
        booking.setDropZoneId(quote.getDropZoneId());
        booking.setPickupAddressText(quote.getPickupPayload().path("address_line_1").asText());
        booking.setDropAddressText(quote.getDropPayload() == null ? null : quote.getDropPayload().path("address_line_1").asText());
        booking.setPickupLatitude(quote.getPickupPayload().path("latitude").decimalValue());
        booking.setPickupLongitude(quote.getPickupPayload().path("longitude").decimalValue());
        booking.setDropLatitude(quote.getDropPayload() == null ? null : quote.getDropPayload().path("latitude").decimalValue());
        booking.setDropLongitude(quote.getDropPayload() == null ? null : quote.getDropPayload().path("longitude").decimalValue());
        booking.setScheduledPickupAt(quote.getScheduledPickupAt());
        booking.setQuotedDurationMinutes(quote.getQuotedDurationMinutes());
        booking.setStatus("PENDING_ASSIGNMENT");
        booking.setCancellationPolicyId(pricingContext.cancellationPolicy().getId());
        booking.setRefundPolicyId(pricingContext.refundPolicy().getId());
        booking.setCurrentTotalPaise(quote.getTotalPaise());
        booking.setPaymentState("PENDING");
        booking.setActive(true);
        booking.setMetadata(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode());
        bookingRepository.save(booking);

        BookingFareSnapshotEntity snapshot = new BookingFareSnapshotEntity();
        snapshot.setBookingId(booking.getId());
        snapshot.setQuoteId(quote.getId());
        snapshot.setPricingPlanId(quote.getPricingPlanId());
        snapshot.setTaxProfileId(quote.getTaxProfileId());
        snapshot.setServiceType(quote.getServiceType());
        snapshot.setScheduledPickupAt(quote.getScheduledPickupAt());
        snapshot.setQuotedDurationMinutes(quote.getQuotedDurationMinutes());
        snapshot.setSubtotalPaise(quote.getSubtotalPaise());
        snapshot.setTaxPaise(quote.getTaxPaise());
        snapshot.setTotalPaise(quote.getTotalPaise());
        List<Map<String, Object>> components = quoteComponentRepository.findByQuoteIdOrderBySortOrderAsc(quote.getId()).stream()
                .map(component -> {
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("code", component.getComponentType());
                    row.put("label", component.getDisplayLabel());
                    row.put("amount_paise", component.getAmountPaise());
                    row.put("is_tax", component.isTax());
                    return row;
                })
                .toList();
        snapshot.setSnapshotPayload(ObjectMapperFactory.instance().valueToTree(Map.of("components", components)));
        bookingFareSnapshotRepository.save(snapshot);

        booking.setFareSnapshotId(snapshot.getId());
        bookingRepository.save(booking);

        BookingPassengerContextEntity passengerContext = new BookingPassengerContextEntity();
        passengerContext.setBookingId(booking.getId());
        passengerContext.setPassengerName(command.passengerName() == null ? command.contactName() : command.passengerName());
        passengerContext.setPassengerMobileE164(command.passengerMobileNumber());
        passengerContext.setSelfBooking(command.passengerName() == null);
        bookingPassengerContextRepository.save(passengerContext);

        if (command.customerNotes() != null && !command.customerNotes().isBlank()) {
            BookingInstructionEntity instruction = new BookingInstructionEntity();
            instruction.setBookingId(booking.getId());
            instruction.setInstructionType("CUSTOMER_NOTE");
            instruction.setInstructionText(command.customerNotes());
            instruction.setCreatedByUserId(currentActorService.requireCurrentActor().userId());
            bookingInstructionRepository.save(instruction);
        }
        if (command.handoverNote() != null && !command.handoverNote().isBlank()) {
            BookingInstructionEntity handover = new BookingInstructionEntity();
            handover.setBookingId(booking.getId());
            handover.setInstructionType("HANDOVER_NOTE");
            handover.setInstructionText(command.handoverNote());
            handover.setCreatedByUserId(currentActorService.requireCurrentActor().userId());
            bookingInstructionRepository.save(handover);
        }

        bookingStateService.transition(booking, "PENDING_ASSIGNMENT", "BOOKING_CONFIRMED", "Booking created from quote");
        bookingRepository.save(booking);

        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setBookingId(booking.getId());
        assignment.setAssignmentSequenceNo(1);
        assignment.setStatus("SEARCHING");
        assignment.setCurrent(true);
        assignment.setRiskStatus("NORMAL");
        assignment.setRescueRequired(false);
        assignmentRepository.save(assignment);

        booking.setCurrentAssignmentId(assignment.getId());
        bookingRepository.save(booking);

        quote.setQuoteStatus("BOOKED");
        quoteRepository.save(quote);

        outboxService.publish("booking", booking.getId(), "BookingConfirmedEvent", Map.of("booking_id", booking.getId()));
        idempotencyService.store(scope, idempotencyKey, command, "BOOKING", booking.getId(), "SUCCEEDED");
        return booking.getId();
    }

    public record CreateBookingCommand(
            UUID quoteId,
            String contactName,
            String contactMobileNumber,
            String customerNotes,
            String passengerName,
            String passengerMobileNumber,
            String handoverNote
    ) {
    }
}
