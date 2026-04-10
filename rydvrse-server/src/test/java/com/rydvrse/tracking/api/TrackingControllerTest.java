package com.rydvrse.tracking.api;

import com.rydvrse.tracking.application.TrackingQueryService;
import com.rydvrse.tracking.application.TrackingStreamService;
import com.rydvrse.tracking.application.TripShareLinkService;
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
class TrackingControllerTest {

    @Mock
    private TrackingQueryService trackingQueryService;

    @Mock
    private TrackingStreamService trackingStreamService;

    @Mock
    private TripShareLinkService tripShareLinkService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new TrackingController(trackingQueryService, trackingStreamService, tripShareLinkService)
        ).build();
    }

    @Test
    void trackingEndpointShouldReturnSnapshot() throws Exception {
        UUID tripId = UUID.randomUUID();
        when(trackingQueryService.trackingSnapshot(tripId)).thenReturn(Map.of(
                "trip_id", tripId,
                "trip_state", "IN_PROGRESS",
                "eta_minutes", 8
        ));

        mockMvc.perform(get("/api/v1/trips/{tripId}/tracking", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trip_id").value(tripId.toString()))
                .andExpect(jsonPath("$.data.trip_state").value("IN_PROGRESS"));
    }
}
