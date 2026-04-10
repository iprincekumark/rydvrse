package com.rydvrse.finance.application;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.audit.AuditService;
import com.rydvrse.common.config.RydvrseProperties;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.finance.domain.InvoiceEntity;
import com.rydvrse.finance.infrastructure.InvoiceRepository;
import com.rydvrse.finance.infrastructure.PaymentOrderRepository;
import com.rydvrse.finance.infrastructure.PaymentTransactionRepository;
import com.rydvrse.finance.infrastructure.RefundDecisionRepository;
import com.rydvrse.finance.infrastructure.RefundRequestRepository;
import com.rydvrse.finance.infrastructure.RefundTransactionRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceOperationsServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Mock
    private RefundDecisionRepository refundDecisionRepository;

    @Mock
    private RefundTransactionRepository refundTransactionRepository;

    @Mock
    private CurrentActorService currentActorService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private JsonNodeUtils jsonNodeUtils;

    @Mock
    private OutboxService outboxService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private FinanceOperationsService financeOperationsService;

    @Test
    void createPaymentOrderShouldAutoCaptureWhenSandboxModeEnabled() {
        UUID bookingId = UUID.randomUUID();
        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setCurrentTripId(UUID.randomUUID());
        booking.setStatus("COMPLETED_PAYMENT_PENDING");
        booking.setCurrentTotalPaise(49900);

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setId(UUID.randomUUID());
        invoice.setBookingId(bookingId);
        invoice.setTotalPaise(49900);
        invoice.setTaxPaise(7600);

        RydvrseProperties properties = new RydvrseProperties();
        properties.getPayments().setSandboxAutoCapture(true);
        properties.getPayments().setWebhookSecret("test-secret");

        financeOperationsService = new FinanceOperationsService(
                bookingRepository,
                tripRepository,
                paymentOrderRepository,
                paymentTransactionRepository,
                invoiceRepository,
                refundRequestRepository,
                refundDecisionRepository,
                refundTransactionRepository,
                currentActorService,
                idempotencyService,
                jsonNodeUtils,
                outboxService,
                auditService,
                properties
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(invoiceRepository.findByBookingId(bookingId)).thenReturn(Optional.of(invoice));
        when(paymentOrderRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());
        when(idempotencyService.checkExisting(any(), any(), any())).thenReturn(Optional.empty());
        when(jsonNodeUtils.toJsonNode(any())).thenReturn(JsonNodeFactory.instance.objectNode());
        AtomicReference<UUID> savedPaymentId = new AtomicReference<>();
        doAnswer(invocation -> {
            var entity = invocation.getArgument(0, com.rydvrse.finance.domain.PaymentOrderEntity.class);
            UUID id = UUID.randomUUID();
            entity.setId(id);
            savedPaymentId.set(id);
            return entity;
        }).when(paymentOrderRepository).save(any(com.rydvrse.finance.domain.PaymentOrderEntity.class));
        when(paymentOrderRepository.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0, UUID.class);
            if (!id.equals(savedPaymentId.get())) {
                return Optional.empty();
            }
            com.rydvrse.finance.domain.PaymentOrderEntity order = new com.rydvrse.finance.domain.PaymentOrderEntity();
            order.setId(id);
            order.setBookingId(bookingId);
            order.setTripId(booking.getCurrentTripId());
            order.setInvoiceId(invoice.getId());
            order.setProviderName("SANDBOX");
            order.setProviderOrderId("pay_mock");
            order.setCurrencyCode("INR");
            order.setAmountPaise(49900);
            order.setStatus("CAPTURED");
            order.setPaymentMethodType("UPI_INTENT");
            return Optional.of(order);
        });
        when(paymentTransactionRepository.findTopByPaymentOrderIdOrderByRecordedAtDesc(any())).thenReturn(Optional.empty());

        Map<String, Object> response = financeOperationsService.createPaymentOrder(bookingId, "UPI_INTENT", "idem-pay");

        assertThat(response).containsKey("payment_order");
        assertThat(booking.getStatus()).isEqualTo("COMPLETED");
        assertThat(booking.getPaymentState()).isEqualTo("CAPTURED");
        assertThat(savedPaymentId.get()).isNotNull();
        verify(paymentOrderRepository).save(any());
        verify(paymentTransactionRepository).save(any());
        verify(bookingRepository).save(booking);
        verify(idempotencyService).store(any(), eq("idem-pay"), eq("UPI_INTENT"), eq("PAYMENT_ORDER"), any(), eq("SUCCEEDED"));
    }

    @Test
    void processWebhookShouldRejectInvalidSignature() {
        RydvrseProperties properties = new RydvrseProperties();
        properties.getPayments().setWebhookSecret("expected");

        financeOperationsService = new FinanceOperationsService(
                bookingRepository,
                tripRepository,
                paymentOrderRepository,
                paymentTransactionRepository,
                invoiceRepository,
                refundRequestRepository,
                refundDecisionRepository,
                refundTransactionRepository,
                currentActorService,
                idempotencyService,
                jsonNodeUtils,
                outboxService,
                auditService,
                properties
        );

        assertThatThrownBy(() -> financeOperationsService.processWebhook("sandbox", "wrong", JsonNodeFactory.instance.objectNode()))
                .hasMessageContaining("Invalid payment webhook signature");
    }
}
