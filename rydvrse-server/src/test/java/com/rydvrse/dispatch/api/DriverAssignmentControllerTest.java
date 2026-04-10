package com.rydvrse.dispatch.api;

import com.rydvrse.dispatch.application.AssignmentLockService;
import com.rydvrse.dispatch.application.AssignmentOfferService;
import com.rydvrse.dispatch.application.AssignmentQueryService;
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
class DriverAssignmentControllerTest {

    @Mock
    private AssignmentQueryService assignmentQueryService;

    @Mock
    private AssignmentOfferService assignmentOfferService;

    @Mock
    private AssignmentLockService assignmentLockService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new DriverAssignmentController(assignmentQueryService, assignmentOfferService, assignmentLockService)
        ).build();
    }

    @Test
    void assignmentDetailEndpointShouldReturnAssignmentPayload() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        when(assignmentQueryService.assignmentDetail(assignmentId)).thenReturn(Map.of(
                "assignment", Map.of("assignment_id", assignmentId, "state", "OFFERED"),
                "booking", Map.of("booking_id", UUID.randomUUID(), "service_type", "SCHEDULED_LOCAL")
        ));

        mockMvc.perform(get("/api/v1/drivers/assignments/{assignmentId}", assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignment.assignment_id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.data.assignment.state").value("OFFERED"));
    }
}
