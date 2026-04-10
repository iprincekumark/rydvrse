package com.rydvrse.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.outbox.OutboxEventEntity;
import com.rydvrse.common.persistence.JsonNodeUtils;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.customer.infrastructure.CustomerProfileRepository;
import com.rydvrse.notification.domain.NotificationEventEntity;
import com.rydvrse.notification.domain.NotificationTemplateEntity;
import com.rydvrse.notification.infrastructure.NotificationDeliveryRepository;
import com.rydvrse.notification.infrastructure.NotificationEventRepository;
import com.rydvrse.notification.infrastructure.NotificationTemplateRepository;
import com.rydvrse.support.infrastructure.SupportTicketRepository;
import com.rydvrse.trip.infrastructure.TripRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationPlannerServiceTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;
    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;
    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private SupportTicketRepository supportTicketRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @Test
    void plannerShouldCreateNotificationEventAndDeliveryForBookingConfirmed() {
        NotificationPlannerService service = new NotificationPlannerService(
                notificationEventRepository,
                notificationDeliveryRepository,
                notificationTemplateRepository,
                new NotificationPreferenceService(),
                bookingRepository,
                tripRepository,
                supportTicketRepository,
                customerProfileRepository,
                userAccountRepository,
                new JsonNodeUtils(new ObjectMapper().registerModule(new JavaTimeModule()))
        );

        UUID bookingId = UUID.randomUUID();
        UUID customerProfileId = UUID.randomUUID();
        UUID userAccountId = UUID.randomUUID();

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setCustomerProfileId(customerProfileId);
        booking.setScheduledPickupAt(OffsetDateTime.now().plusHours(3));

        CustomerProfileEntity profile = new CustomerProfileEntity();
        profile.setId(customerProfileId);
        profile.setUserAccountId(userAccountId);

        UserAccountEntity userAccount = new UserAccountEntity();
        userAccount.setId(userAccountId);
        userAccount.setMobileNumberE164("+919999999999");

        NotificationTemplateEntity template = new NotificationTemplateEntity();
        template.setId(UUID.randomUUID());
        template.setTemplateCode("BOOKING_CONFIRMED");
        template.setChannel("PUSH");
        template.setLanguageCode("en");
        template.setBodyTemplate("body");
        template.setActive(true);

        OutboxEventEntity event = new OutboxEventEntity();
        event.setEventType("BookingConfirmedEvent");
        event.setPayload(new JsonNodeUtils(new ObjectMapper().registerModule(new JavaTimeModule())).toJsonNode(java.util.Map.of("booking_id", bookingId)));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(customerProfileRepository.findById(customerProfileId)).thenReturn(Optional.of(profile));
        when(userAccountRepository.findById(userAccountId)).thenReturn(Optional.of(userAccount));
        when(notificationTemplateRepository.findByTemplateCodeAndChannelAndLanguageCodeAndActiveTrue("BOOKING_CONFIRMED", "PUSH", "en"))
                .thenReturn(Optional.of(template));
        when(notificationTemplateRepository.findByTemplateCodeAndChannelAndLanguageCodeAndActiveTrue("BOOKING_CONFIRMED", "SMS", "en"))
                .thenReturn(Optional.empty());
        doAnswer(invocation -> {
            NotificationEventEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        }).when(notificationEventRepository).save(any(NotificationEventEntity.class));

        int deliveries = service.planFromOutbox(event);

        assertThat(deliveries).isEqualTo(1);
        verify(notificationDeliveryRepository).save(any());
    }
}
