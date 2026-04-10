package com.rydvrse.dispatch.application;

import com.rydvrse.admin.application.AdminBookingService;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ReassignmentService {

    private final AdminBookingService adminBookingService;
    private final CurrentActorService currentActorService;

    public ReassignmentService(AdminBookingService adminBookingService, CurrentActorService currentActorService) {
        this.adminBookingService = adminBookingService;
        this.currentActorService = currentActorService;
    }

    @Transactional
    public Map<String, Object> adminReassign(UUID bookingId, AdminBookingService.ReassignCommand command, String idempotencyKey) {
        currentActorService.requireActor(ActorType.ADMIN);
        return adminBookingService.reassign(bookingId, command, idempotencyKey);
    }
}
