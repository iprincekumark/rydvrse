package com.rydvrse.support.application;

import com.rydvrse.common.audit.AuditService;
import com.rydvrse.common.idempotency.IdempotencyService;
import com.rydvrse.common.outbox.OutboxService;
import com.rydvrse.common.security.ActorType;
import com.rydvrse.common.security.CurrentActorService;
import com.rydvrse.customer.application.CustomerProfileService;
import com.rydvrse.driver.application.DriverProfileService;
import com.rydvrse.support.domain.IncidentCaseEntity;
import com.rydvrse.support.domain.SupportTicketEntity;
import com.rydvrse.support.domain.SupportTicketNoteEntity;
import com.rydvrse.support.infrastructure.IncidentCaseRepository;
import com.rydvrse.support.infrastructure.SupportTicketNoteRepository;
import com.rydvrse.support.infrastructure.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketNoteRepository supportTicketNoteRepository;
    private final IncidentCaseRepository incidentCaseRepository;
    private final CurrentActorService currentActorService;
    private final CustomerProfileService customerProfileService;
    private final DriverProfileService driverProfileService;
    private final IdempotencyService idempotencyService;
    private final OutboxService outboxService;
    private final AuditService auditService;

    public SupportTicketService(
            SupportTicketRepository supportTicketRepository,
            SupportTicketNoteRepository supportTicketNoteRepository,
            IncidentCaseRepository incidentCaseRepository,
            CurrentActorService currentActorService,
            CustomerProfileService customerProfileService,
            DriverProfileService driverProfileService,
            IdempotencyService idempotencyService,
            OutboxService outboxService,
            AuditService auditService
    ) {
        this.supportTicketRepository = supportTicketRepository;
        this.supportTicketNoteRepository = supportTicketNoteRepository;
        this.incidentCaseRepository = incidentCaseRepository;
        this.currentActorService = currentActorService;
        this.customerProfileService = customerProfileService;
        this.driverProfileService = driverProfileService;
        this.idempotencyService = idempotencyService;
        this.outboxService = outboxService;
        this.auditService = auditService;
    }

    @Transactional
    public Map<String, Object> createCustomerTicket(CreateTicketCommand command, String idempotencyKey) {
        UUID customerId = customerProfileService.requireCurrentProfile().getId();
        return createTicket("POST:/api/v1/support/tickets:" + customerId, "CUSTOMER", customerId, null, command, idempotencyKey);
    }

    @Transactional
    public Map<String, Object> createDriverTicket(CreateTicketCommand command, String idempotencyKey) {
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        return createTicket("POST:/api/v1/drivers/support/tickets:" + driverId, "DRIVER", null, driverId, command, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listMine() {
        ActorType actorType = currentActorService.requireCurrentActor().actorType();
        if (actorType == ActorType.CUSTOMER) {
            UUID customerId = customerProfileService.requireCurrentProfile().getId();
            return supportTicketRepository.findAll().stream()
                    .filter(ticket -> customerId.equals(ticket.getCustomerProfileId()))
                    .map(this::toSummary)
                    .toList();
        }
        UUID driverId = driverProfileService.requireCurrentProfile().getId();
        return supportTicketRepository.findAll().stream()
                .filter(ticket -> driverId.equals(ticket.getDriverProfileId()))
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(UUID ticketId) {
        SupportTicketEntity ticket = supportTicketRepository.findById(ticketId).orElseThrow();
        return toDetail(ticket);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listAll() {
        currentActorService.requireActor(ActorType.ADMIN);
        return supportTicketRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Transactional
    public Map<String, Object> adminAction(UUID ticketId, String actionType, Map<String, Object> payload) {
        currentActorService.requireActor(ActorType.ADMIN);
        SupportTicketEntity ticket = supportTicketRepository.findById(ticketId).orElseThrow();
        switch (actionType) {
            case "ASSIGN_OWNER" -> ticket.setCurrentOwnerUserId(UUID.fromString(String.valueOf(payload.get("owner_user_id"))));
            case "CHANGE_SEVERITY" -> ticket.setSeverity(String.valueOf(payload.get("severity")));
            case "RESOLVE" -> {
                ticket.setStatus("RESOLVED");
                ticket.setResolvedAt(OffsetDateTime.now());
            }
            case "REOPEN" -> ticket.setStatus("REOPENED");
            case "ESCALATE_INCIDENT" -> {
                IncidentCaseEntity incident = incidentCaseRepository.findBySupportTicketId(ticketId).orElseGet(IncidentCaseEntity::new);
                incident.setSupportTicketId(ticketId);
                incident.setBookingId(ticket.getBookingId());
                incident.setTripId(ticket.getTripId());
                incident.setIncidentType(String.valueOf(payload.getOrDefault("incident_type", ticket.getCategoryCode())));
                incident.setSeverity(ticket.getSeverity());
                incident.setStatus("OPEN");
                incident.setOpenedAt(OffsetDateTime.now());
                incident.setSummary(ticket.getDescription());
                incidentCaseRepository.save(incident);
            }
            default -> {
            }
        }
        supportTicketRepository.save(ticket);
        SupportTicketNoteEntity note = new SupportTicketNoteEntity();
        note.setSupportTicketId(ticketId);
        note.setNoteType(actionType);
        note.setInternal(true);
        note.setAuthorUserId(currentActorService.requireCurrentActor().userId());
        note.setAuthorRole(currentActorService.requireCurrentActor().actorType().name());
        note.setNoteText(String.valueOf(payload));
        supportTicketNoteRepository.save(note);
        auditService.record("SUPPORT_ACTION", "SUPPORT_TICKET", ticketId, null, payload, actionType, null);
        return toDetail(ticket);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> incidents() {
        currentActorService.requireActor(ActorType.ADMIN);
        return incidentCaseRepository.findAll().stream().map(this::toIncidentSummary).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> incident(UUID incidentId) {
        currentActorService.requireActor(ActorType.ADMIN);
        IncidentCaseEntity incident = incidentCaseRepository.findById(incidentId).orElseThrow();
        return toIncidentSummary(incident);
    }

    @Transactional
    public Map<String, Object> incidentAction(UUID incidentId, String actionType, Map<String, Object> payload) {
        currentActorService.requireActor(ActorType.ADMIN);
        IncidentCaseEntity incident = incidentCaseRepository.findById(incidentId).orElseThrow();
        switch (actionType) {
            case "ASSIGN_OWNER" -> incident.setOwnerUserId(UUID.fromString(String.valueOf(payload.get("owner_user_id"))));
            case "CLOSE" -> {
                incident.setStatus("CLOSED");
                incident.setClosedAt(OffsetDateTime.now());
            }
            case "REOPEN" -> incident.setStatus("OPEN");
            case "ESCALATE" -> incident.setStatus("INVESTIGATING");
            default -> {
            }
        }
        incidentCaseRepository.save(incident);
        auditService.record("INCIDENT_ACTION", "INCIDENT_CASE", incidentId, null, payload, actionType, null);
        return toIncidentSummary(incident);
    }

    private Map<String, Object> createTicket(
            String scope,
            String ticketType,
            UUID customerProfileId,
            UUID driverProfileId,
            CreateTicketCommand command,
            String idempotencyKey
    ) {
        return idempotencyService.checkExisting(scope, idempotencyKey, idempotencyService.hashPayload(command))
                .map(existing -> Map.<String, Object>of("ticket_id", existing.responseReferenceId()))
                .orElseGet(() -> {
                    SupportTicketEntity ticket = new SupportTicketEntity();
                    ticket.setTicketCode("SUP-" + System.currentTimeMillis());
                    ticket.setTicketType(ticketType);
                    ticket.setBookingId(command.bookingId());
                    ticket.setTripId(command.tripId());
                    ticket.setCustomerProfileId(customerProfileId);
                    ticket.setDriverProfileId(driverProfileId);
                    ticket.setCategoryCode(command.category());
                    ticket.setSubCategoryCode(command.subCategory());
                    ticket.setSeverity(command.severity());
                    ticket.setStatus("OPEN");
                    ticket.setDescription(command.description());
                    ticket.setOpenedAt(OffsetDateTime.now());
                    supportTicketRepository.save(ticket);

                    SupportTicketNoteEntity note = new SupportTicketNoteEntity();
                    note.setSupportTicketId(ticket.getId());
                    note.setNoteType("INITIAL_REPORT");
                    note.setInternal(false);
                    note.setAuthorUserId(currentActorService.requireCurrentActor().userId());
                    note.setAuthorRole(currentActorService.requireCurrentActor().actorType().name());
                    note.setNoteText(command.description());
                    supportTicketNoteRepository.save(note);

                    outboxService.publish("support_ticket", ticket.getId(), "SupportTicketCreatedEvent", Map.of("ticket_id", ticket.getId()));
                    idempotencyService.store(scope, idempotencyKey, command, "SUPPORT_TICKET", ticket.getId(), "SUCCEEDED");
                    return toDetail(ticket);
                });
    }

    private Map<String, Object> toSummary(SupportTicketEntity ticket) {
        return Map.of(
                "ticket_id", ticket.getId(),
                "state", ticket.getStatus(),
                "category", ticket.getCategoryCode(),
                "sub_category", ticket.getSubCategoryCode(),
                "severity", ticket.getSeverity(),
                "description", ticket.getDescription(),
                "booking_id", ticket.getBookingId(),
                "trip_id", ticket.getTripId(),
                "created_at", ticket.getOpenedAt(),
                "last_updated_at", ticket.getUpdatedAt()
        );
    }

    private Map<String, Object> toDetail(SupportTicketEntity ticket) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("ticket", toSummary(ticket));
        detail.put("timeline", supportTicketNoteRepository.findBySupportTicketIdOrderByCreatedAtAsc(ticket.getId()).stream()
                .map(note -> Map.of(
                        "note_type", note.getNoteType(),
                        "is_internal", note.isInternal(),
                        "note_text", note.getNoteText(),
                        "created_at", note.getCreatedAt()
                )).toList());
        detail.put("linked_resources", Map.of("booking_id", ticket.getBookingId(), "trip_id", ticket.getTripId()));
        detail.put("current_sla_state", ticket.getStatus());
        detail.put("incident", incidentCaseRepository.findBySupportTicketId(ticket.getId()).map(this::toIncidentSummary).orElse(null));
        return detail;
    }

    private Map<String, Object> toIncidentSummary(IncidentCaseEntity incident) {
        return Map.of(
                "incident_id", incident.getId(),
                "support_ticket_id", incident.getSupportTicketId(),
                "state", incident.getStatus(),
                "severity", incident.getSeverity(),
                "summary", incident.getSummary(),
                "owner_user_id", incident.getOwnerUserId(),
                "opened_at", incident.getOpenedAt()
        );
    }

    public record CreateTicketCommand(
            String category,
            String subCategory,
            String severity,
            String description,
            UUID bookingId,
            UUID tripId
    ) {
    }
}
