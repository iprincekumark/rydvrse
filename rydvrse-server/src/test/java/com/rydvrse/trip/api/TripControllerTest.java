package com.rydvrse.trip.api;

import com.rydvrse.finance.application.FinanceOperationsService;
import com.rydvrse.trip.application.TripExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private TripExecutionService tripExecutionService;

    @Mock
    private FinanceOperationsService financeOperationsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TripController(tripExecutionService, financeOperationsService)).build();
    }

    @Test
    void tripDetailEndpointShouldReturnTripPayload() throws Exception {
        UUID tripId = UUID.randomUUID();
        when(tripExecutionService.tripDetail(tripId)).thenReturn(Map.of(
                "trip_id", tripId,
                "state", "IN_PROGRESS",
                "booking_summary", Map.of("service_type", "SCHEDULED_LOCAL")
        ));

        mockMvc.perform(get("/api/v1/trips/{tripId}", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trip_id").value(tripId.toString()))
                .andExpect(jsonPath("$.data.state").value("IN_PROGRESS"));
    }
}
