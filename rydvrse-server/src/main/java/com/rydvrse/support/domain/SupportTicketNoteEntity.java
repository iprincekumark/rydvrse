package com.rydvrse.support.domain;

import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "support_ticket_note", schema = "support")
public class SupportTicketNoteEntity extends AbstractAppendOnlyEntity {

    @Column(name = "support_ticket_id", nullable = false)
    private UUID supportTicketId;

    @Column(name = "note_type", nullable = false)
    private String noteType;

    @Column(name = "is_internal", nullable = false)
    private boolean internal;

    @Column(name = "author_user_id")
    private UUID authorUserId;

    @Column(name = "author_role")
    private String authorRole;

    @Column(name = "note_text", nullable = false)
    private String noteText;

    public UUID getSupportTicketId() {
        return supportTicketId;
    }

    public void setSupportTicketId(UUID supportTicketId) {
        this.supportTicketId = supportTicketId;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(UUID authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}
