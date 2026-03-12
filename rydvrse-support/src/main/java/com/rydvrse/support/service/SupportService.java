package com.rydvrse.support.service;

import com.rydvrse.shared.exception.ResourceNotFoundException;
import com.rydvrse.support.domain.SupportTicket;
import com.rydvrse.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository ticketRepository;

    @Transactional
    public SupportTicket createTicket(SupportTicket ticket) {
        SupportTicket saved = ticketRepository.save(ticket);
        log.info("Support ticket {} created by {} {}", saved.getTicketNumber(),
                saved.getUserType(), saved.getUserId());
        return saved;
    }

    @Transactional
    public SupportTicket updateStatus(UUID ticketId, String status, String notes) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", ticketId.toString()));
        ticket.setStatus(status);
        if (notes != null) ticket.setResolutionNotes(notes);
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> getUserTickets(UUID userId, Pageable pageable) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> getTicketsByStatus(String status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
}
