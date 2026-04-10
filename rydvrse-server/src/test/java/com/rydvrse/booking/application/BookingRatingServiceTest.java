package com.rydvrse.booking.application;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.rydvrse.booking.domain.BookingEntity;
import com.rydvrse.booking.infrastructure.BookingRepository;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.customer.domain.CustomerProfileEntity;
import com.rydvrse.dispatch.domain.AssignmentEntity;
import com.rydvrse.dispatch.infrastructure.AssignmentRepository;
import com.rydvrse.driver.domain.DriverProfileEntity;
import com.rydvrse.driver.infrastructure.DriverProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingRatingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerProfileService customerProfileService;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private BookingRatingService bookingRatingService;

    @Test
    void submitShouldPersistRatingAndUpdateDriverAggregate() {
        UUID bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        CustomerProfileEntity customer = new CustomerProfileEntity();
        customer.setId(customerId);

        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setCustomerProfileId(customerId);
        booking.setCurrentAssignmentId(assignmentId);
        booking.setStatus("COMPLETED");
        booking.setMetadata(JsonNodeFactory.instance.objectNode());

        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setId(assignmentId);
        assignment.setDriverProfileId(driverId);

        DriverProfileEntity driver = new DriverProfileEntity();
        driver.setId(driverId);
        driver.setRatingAvg(new BigDecimal("4.50"));
        driver.setRatingCount(2);

        when(customerProfileService.requireCurrentProfile()).thenReturn(customer);
        when(bookingRepository.findByIdAndCustomerProfileId(bookingId, customerId)).thenReturn(Optional.of(booking));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(driverProfileRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(idempotencyService.checkExisting(any(), any(), any())).thenReturn(Optional.empty());

        var response = bookingRatingService.submit(bookingId, 5, List.of("PUNCTUAL", "POLITE"), "Smooth trip", "idem-1");

        assertThat(response).containsEntry("booking_id", bookingId);
        assertThat(response).containsEntry("rating", 5);
        assertThat(booking.getMetadata().path("customer_rating").asInt()).isEqualTo(5);
        assertThat(driver.getRatingCount()).isEqualTo(3);
        assertThat(driver.getRatingAvg()).isEqualByComparingTo("4.67");
        verify(bookingRepository).save(booking);
        verify(driverProfileRepository).save(driver);
        verify(idempotencyService).store(any(), eq("idem-1"), any(), eq("BOOKING"), eq(bookingId), eq("SUCCEEDED"));
    }
}
