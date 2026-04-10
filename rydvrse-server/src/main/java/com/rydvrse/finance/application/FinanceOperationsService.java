package com.rydvrse.finance.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.audit.AuditService;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.error.ApiException;
import com.rydvrse.common.error.ErrorCode;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.finance.domain.InvoiceEntity;
import com.rydvrse.finance.domain.PaymentOrderEntity;
import com.rydvrse.finance.domain.PaymentTransactionEntity;
import com.rydvrse.finance.domain.RefundDecisionEntity;
import com.rydvrse.finance.domain.RefundRequestEntity;
import com.rydvrse.finance.domain.RefundTransactionEntity;
import com.rydvrse.finance.infrastructure.InvoiceRepository;
import com.rydvrse.finance.infrastructure.PaymentOrderRepository;
import com.rydvrse.finance.infrastructure.PaymentTransactionRepository;
import com.rydvrse.finance.infrastructure.RefundDecisionRepository;
import com.rydvrse.finance.infrastructure.RefundRequestRepository;
import com.rydvrse.finance.infrastructure.RefundTransactionRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FinanceOperationsService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final RefundDecisionRepository refundDecisionRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final CurrentActorService currentActorService;
    private final IdempotencyService idempotencyService;
    private final JsonNodeUtils jsonNodeUtils;
    private final OutboxService outboxService;
    private final AuditService auditService;
    private final RydvrseProperties rydvrseProperties;

    public FinanceOperationsService(
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            PaymentOrderRepository paymentOrderRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            InvoiceRepository invoiceRepository,
            RefundRequestRepository refundRequestRepository,
            RefundDecisionRepository refundDecisionRepository,
            RefundTransactionRepository refundTransactionRepository,
            CurrentActorService currentActorService,
            IdempotencyService idempotencyService,
            JsonNodeUtils jsonNodeUtils,
            OutboxService outboxService,
            AuditService auditService,
            RydvrseProperties rydvrseProperties
    ) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.refundRequestRepository = refundRequestRepository;
        this.refundDecisionRepository = refundDecisionRepository;
        this.refundTransactionRepository = refundTransactionRepository;
        this.currentActorService = currentActorService;
        this.idempotencyService = idempotencyService;
        this.jsonNodeUtils = jsonNodeUtils;
        this.outboxService = outboxService;
        this.auditService = auditService;
        this.rydvrseProperties = rydvrseProperties;
    }

    @Transactional
    public Map<String, Object> createPaymentOrder(UUID bookingId, String paymentMethod, String idempotencyKey) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        if (!List.of("COMPLETED_PAYMENT_PENDING", "COMPLETED").contains(booking.getStatus())) {
            throw ApiException.conflict(ErrorCode.STATE_CONFLICT, "Booking is not ready for payment");
        }
        String scope = "POST:/api/v1/bookings/" + bookingId + "/payment-orders";
        return idempotencyService.checkExisting(scope, idempotencyKey, bookingId.toString())
                .map(existing -> payment(existing.responseReferenceId()))
                .orElseGet(() -> {
                    InvoiceEntity invoice = invoiceRepository.findByBookingId(bookingId)
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Invoice not found"));
                    PaymentOrderEntity order = paymentOrderRepository.findByBookingId(bookingId).orElseGet(PaymentOrderEntity::new);
                    order.setBookingId(bookingId);
                    order.setTripId(booking.getCurrentTripId());
                    order.setInvoiceId(invoice.getId());
                    order.setProviderName("SANDBOX");
                    order.setProviderOrderId("pay_" + System.currentTimeMillis());
                    order.setCurrencyCode("INR");
                    order.setAmountPaise(invoice.getTotalPaise());
                    order.setStatus(rydvrseProperties.getPayments().isSandboxAutoCapture() ? "CAPTURED" : "CREATED");
                    order.setPaymentMethodType(paymentMethod);
                    order.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
                    order.setMetadata(jsonNodeUtils.toJsonNode(Map.of(
                            "payment_method", paymentMethod,
                            "sandbox_auto_capture", rydvrseProperties.getPayments().isSandboxAutoCapture()
                    )));
                    paymentOrderRepository.save(order);

                    PaymentTransactionEntity transaction = new PaymentTransactionEntity();
                    transaction.setPaymentOrderId(order.getId());
                    transaction.setProviderTransactionId(order.getProviderOrderId());
                    transaction.setTransactionType("PAYMENT");
                    transaction.setStatus(order.getStatus());
                    transaction.setAmountPaise(order.getAmountPaise());
                    transaction.setProviderEventAt(OffsetDateTime.now());
                    transaction.setProviderPayload(jsonNodeUtils.toJsonNode(Map.of("provider_order_id", order.getProviderOrderId(), "state", order.getStatus())));
                    transaction.setRecordedAt(OffsetDateTime.now());
                    paymentTransactionRepository.save(transaction);

                    if ("CAPTURED".equals(order.getStatus())) {
                        booking.setPaymentState("CAPTURED");
                        booking.setStatus("COMPLETED");
                        bookingRepository.save(booking);
                    }
                    outboxService.publish("payment_order", order.getId(), "PaymentOrderCreatedEvent", Map.of("payment_id", order.getId()));
                    idempotencyService.store(scope, idempotencyKey, paymentMethod, "PAYMENT_ORDER", order.getId(), "SUCCEEDED");
                    return payment(order.getId());
                });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> payment(UUID paymentId) {
        PaymentOrderEntity order = paymentOrderRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Payment not found"));
        PaymentTransactionEntity latestTransaction = paymentTransactionRepository.findTopByPaymentOrderIdOrderByRecordedAtDesc(order.getId()).orElse(null);
        Map<String, Object> paymentOrder = new LinkedHashMap<>();
        paymentOrder.put("payment_id", order.getId());
        paymentOrder.put("booking_id", order.getBookingId());
        paymentOrder.put("trip_id", order.getTripId());
        paymentOrder.put("invoice_id", order.getInvoiceId());
        paymentOrder.put("state", order.getStatus());
        paymentOrder.put("provider", order.getProviderName());
        paymentOrder.put("provider_order_id", order.getProviderOrderId());
        paymentOrder.put("amount_paise", order.getAmountPaise());
        paymentOrder.put("payment_method", order.getPaymentMethodType());
        paymentOrder.put("expires_at", order.getExpiresAt());
        paymentOrder.put("updated_at", order.getUpdatedAt());
        return Map.of(
                "payment_order", paymentOrder,
                "provider_payment_reference", latestTransaction == null ? order.getProviderOrderId() : latestTransaction.getProviderTransactionId(),
                "failure_reason", "FAILED".equals(order.getStatus()) ? "Provider reported payment failure" : ""
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> invoice(UUID bookingId) {
        InvoiceEntity invoice = invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Invoice not found"));
        PaymentOrderEntity order = paymentOrderRepository.findByBookingId(bookingId).orElse(null);
        List<RefundRequestEntity> refunds = refundRequestRepository.findByBookingIdOrderByRequestedAtDesc(bookingId);
        Map<String, Object> paymentSummary = order == null ? Map.of("state", "PENDING") : Map.of(
                "payment_id", order.getId(),
                "state", order.getStatus(),
                "amount_paise", order.getAmountPaise()
        );
        Map<String, Object> refundSummary = refunds.isEmpty() ? Map.of("state", "NONE", "count", 0) : Map.of(
                "state", refunds.getFirst().getStatus(),
                "count", refunds.size(),
                "latest_refund_id", refunds.getFirst().getId()
        );
        return Map.of(
                "invoice_id", invoice.getId(),
                "invoice_number", invoice.getInvoiceNumber(),
                "fare_breakdown", invoice.getSnapshotPayload(),
                "tax_breakdown", Map.of("tax_paise", invoice.getTaxPaise()),
                "payment_summary", paymentSummary,
                "refund_summary", refundSummary
        );
    }

    @Transactional
    public Map<String, Object> createRefund(AdminRefundCommand command, String idempotencyKey) {
        currentActorService.requireActor(ActorType.ADMIN);
        String scope = "POST:/api/v1/admin/refunds";
        return idempotencyService.checkExisting(scope, idempotencyKey, idempotencyService.hashPayload(command))
                .map(existing -> refundDetail(existing.responseReferenceId()))
                .orElseGet(() -> {
                    BookingEntity booking = bookingRepository.findById(command.bookingId())
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
                    if (!booking.getRowVersion().equals(command.rowVersion())) {
                        throw ApiException.conflict(ErrorCode.STALE_ROW_VERSION, "Booking has changed");
                    }
                    PaymentOrderEntity order = paymentOrderRepository.findById(command.paymentId())
                            .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Payment not found"));
                    if (!"CAPTURED".equals(order.getStatus()) && !"PARTIALLY_REFUNDED".equals(order.getStatus()) && !"REFUNDED".equals(order.getStatus())) {
                        throw ApiException.unprocessable(ErrorCode.REFUND_NOT_ALLOWED, "Payment is not refundable");
                    }
                    if (command.amountPaise() > order.getAmountPaise()) {
                        throw ApiException.unprocessable(ErrorCode.OVERRIDE_LIMIT_EXCEEDED, "Refund exceeds paid amount");
                    }
                    RefundRequestEntity refund = new RefundRequestEntity();
                    refund.setPaymentOrderId(order.getId());
                    refund.setBookingId(booking.getId());
                    refund.setTripId(booking.getCurrentTripId());
                    refund.setRequestedByUserId(currentActorService.requireCurrentActor().userId());
                    refund.setReasonCode(command.reasonCode());
                    refund.setReasonNote(command.reasonNote());
                    refund.setRequestedAmountPaise(command.amountPaise());
                    refund.setStatus("APPROVED");
                    refund.setRequestedAt(OffsetDateTime.now());
                    refundRequestRepository.save(refund);

                    RefundDecisionEntity decision = new RefundDecisionEntity();
                    decision.setRefundRequestId(refund.getId());
                    decision.setDecisionStatus("APPROVED");
                    decision.setApprovedAmountPaise(command.amountPaise());
                    decision.setDecidedByUserId(currentActorService.requireCurrentActor().userId());
                    decision.setDecisionReasonCode(command.reasonCode());
                    decision.setDecisionNote(command.reasonNote());
                    decision.setDecidedAt(OffsetDateTime.now());
                    refundDecisionRepository.save(decision);

                    RefundTransactionEntity transaction = new RefundTransactionEntity();
                    transaction.setRefundRequestId(refund.getId());
                    transaction.setProviderRefundId("rfnd_" + System.currentTimeMillis());
                    transaction.setAmountPaise(command.amountPaise());
                    transaction.setStatus("PROCESSED");
                    transaction.setProviderPayload(jsonNodeUtils.toJsonNode(Map.of("booking_id", booking.getId(), "payment_id", order.getId())));
                    transaction.setProcessedAt(OffsetDateTime.now());
                    refundTransactionRepository.save(transaction);

                    order.setStatus(command.amountPaise() == order.getAmountPaise() ? "REFUNDED" : "PARTIALLY_REFUNDED");
                    paymentOrderRepository.save(order);
                    booking.setPaymentState(order.getStatus());
                    bookingRepository.save(booking);
                    auditService.record("REFUND_CREATED", "REFUND_REQUEST", refund.getId(), null, Map.of("amount_paise", command.amountPaise()), command.reasonCode(), command.reasonNote());
                    outboxService.publish("refund_request", refund.getId(), "RefundRequestedEvent", Map.of("refund_id", refund.getId()));
                    idempotencyService.store(scope, idempotencyKey, command, "REFUND_REQUEST", refund.getId(), "SUCCEEDED");
        return Map.of(
                "refund_detail", refundDetail(refund.getId()),
                "audit_reference", "REFUND_CREATED"
        );
                });
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listRefunds() {
        return refundRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(this::toRefundSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> refundDetail(UUID refundId) {
        RefundRequestEntity refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Refund not found"));
        RefundDecisionEntity decision = refundDecisionRepository.findByRefundRequestId(refund.getId()).orElse(null);
        List<RefundTransactionEntity> transactions = refundTransactionRepository.findByRefundRequestIdOrderByCreatedAtAsc(refund.getId());
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("refund_id", refund.getId());
        detail.put("booking_id", refund.getBookingId());
        detail.put("payment_id", refund.getPaymentOrderId());
        detail.put("trip_id", refund.getTripId());
        detail.put("state", refund.getStatus());
        detail.put("requested_amount_paise", refund.getRequestedAmountPaise());
        detail.put("reason_code", refund.getReasonCode());
        detail.put("reason_note", refund.getReasonNote());
        detail.put("requested_at", refund.getRequestedAt());
        detail.put("decision", decision == null ? null : Map.of(
                "state", decision.getDecisionStatus(),
                "approved_amount_paise", decision.getApprovedAmountPaise(),
                "decided_at", decision.getDecidedAt(),
                "decision_reason_code", decision.getDecisionReasonCode(),
                "decision_note", decision.getDecisionNote()
        ));
        detail.put("provider_transactions", transactions.stream().map(tx -> Map.of(
                "refund_transaction_id", tx.getId(),
                "provider_refund_id", tx.getProviderRefundId(),
                "amount_paise", tx.getAmountPaise(),
                "state", tx.getStatus(),
                "processed_at", tx.getProcessedAt()
        )).toList());
        return detail;
    }

    @Transactional
    public Map<String, Object> processWebhook(String provider, String signature, JsonNode payload) {
        if (signature == null || !signature.equals(rydvrseProperties.getPayments().getWebhookSecret())) {
            throw ApiException.unauthorized(ErrorCode.INVALID_WEBHOOK_SIGNATURE, "Invalid payment webhook signature");
        }
        String providerOrderId = payload.path("provider_order_id").asText(payload.path("payment_order_id").asText(null));
        if (providerOrderId == null || providerOrderId.isBlank()) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "provider_order_id is required");
        }
        PaymentOrderEntity order = paymentOrderRepository.findByProviderOrderId(providerOrderId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.RESOURCE_NOT_FOUND, "Payment order not found"));
        String status = payload.path("status").asText(payload.path("event_type").asText("CREATED")).toUpperCase();
        String normalizedStatus = normalizeWebhookStatus(status);
        PaymentTransactionEntity transaction = paymentTransactionRepository.findByProviderTransactionId(payload.path("provider_transaction_id").asText(""))
                .orElseGet(PaymentTransactionEntity::new);
        transaction.setPaymentOrderId(order.getId());
        transaction.setProviderTransactionId(payload.path("provider_transaction_id").asText(order.getProviderOrderId()));
        transaction.setTransactionType(payload.path("transaction_type").asText("PAYMENT"));
        transaction.setStatus(normalizedStatus);
        transaction.setAmountPaise(payload.path("amount_paise").asLong(order.getAmountPaise()));
        transaction.setProviderEventAt(OffsetDateTime.now());
        transaction.setProviderPayload(payload);
        transaction.setRecordedAt(OffsetDateTime.now());
        paymentTransactionRepository.save(transaction);

        order.setStatus(normalizedStatus);
        paymentOrderRepository.save(order);

        BookingEntity booking = bookingRepository.findById(order.getBookingId()).orElseThrow();
        if ("CAPTURED".equals(normalizedStatus)) {
            booking.setPaymentState("CAPTURED");
            booking.setStatus("COMPLETED");
        } else if ("FAILED".equals(normalizedStatus)) {
            booking.setPaymentState("FAILED");
        }
        bookingRepository.save(booking);
        outboxService.publish("payment_order", order.getId(), "PaymentWebhookProcessedEvent", Map.of("provider", provider, "state", normalizedStatus));
        return Map.of("accepted", true, "payment_id", order.getId(), "state", normalizedStatus);
    }

    private String normalizeWebhookStatus(String status) {
        return switch (status) {
            case "SUCCESS", "CAPTURED", "PAID" -> "CAPTURED";
            case "FAILED", "PAYMENT_FAILED" -> "FAILED";
            case "REFUNDED" -> "REFUNDED";
            default -> "CREATED";
        };
    }

    private Map<String, Object> toRefundSummary(RefundRequestEntity refund) {
        return Map.of(
                "refund_id", refund.getId(),
                "booking_id", refund.getBookingId(),
                "payment_id", refund.getPaymentOrderId(),
                "state", refund.getStatus(),
                "reason_code", refund.getReasonCode(),
                "requested_amount_paise", refund.getRequestedAmountPaise(),
                "requested_at", refund.getRequestedAt()
        );
    }

    public record AdminRefundCommand(
            UUID bookingId,
            UUID paymentId,
            long amountPaise,
            String reasonCode,
            String reasonNote,
            Long rowVersion
    ) {
    }
}
