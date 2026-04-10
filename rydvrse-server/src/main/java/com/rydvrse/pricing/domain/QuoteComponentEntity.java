package com.rydvrse.pricing.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.rydvrse.common.persistence.AbstractAppendOnlyEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "quote_component", schema = "commercial")
public class QuoteComponentEntity extends AbstractAppendOnlyEntity {

    @Column(name = "quote_id", nullable = false)
    private UUID quoteId;

    @Column(name = "component_type", nullable = false)
    private String componentType;

    @Column(name = "display_label", nullable = false)
    private String displayLabel;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_tax", nullable = false)
    private boolean tax;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "component_payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode componentPayload;

    public UUID getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(UUID quoteId) {
        this.quoteId = quoteId;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public void setAmountPaise(long amountPaise) {
        this.amountPaise = amountPaise;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isTax() {
        return tax;
    }

    public void setTax(boolean tax) {
        this.tax = tax;
    }

    public JsonNode getComponentPayload() {
        return componentPayload;
    }

    public void setComponentPayload(JsonNode componentPayload) {
        this.componentPayload = componentPayload;
    }
}
