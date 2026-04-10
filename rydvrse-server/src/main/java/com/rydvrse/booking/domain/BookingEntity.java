package com.rydvrse.booking.domain;

import com.rydvrse.common.persistence.AbstractMutableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking", schema = "booking")
public class BookingEntity extends AbstractMutableEntity {

    @Column(name = "booking_code", nullable = false, unique = true)
    private String bookingCode;

    @Column(name = "customer_profile_id", nullable = false)
    private UUID customerProfileId;

    @Column(name = "city_id", nullable = false)
    private UUID cityId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

    @Column(name = "pickup_zone_id")
    private UUID pickupZoneId;

    @Column(name = "drop_zone_id")
    private UUID dropZoneId;

    @Column(name = "pickup_address_text", nullable = false)
    private String pickupAddressText;

    @Column(name = "drop_address_text")
    private String dropAddressText;

    @Column(name = "pickup_latitude", nullable = false)
    private BigDecimal pickupLatitude;

    @Column(name = "pickup_longitude", nullable = false)
    private BigDecimal pickupLongitude;

    @Column(name = "drop_latitude")
    private BigDecimal dropLatitude;

    @Column(name = "drop_longitude")
    private BigDecimal dropLongitude;

    @Column(name = "scheduled_pickup_at", nullable = false)
    private OffsetDateTime scheduledPickupAt;

    @Column(name = "quoted_duration_minutes")
    private Integer quotedDurationMinutes;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "current_assignment_id")
    private UUID currentAssignmentId;

    @Column(name = "current_trip_id")
    private UUID currentTripId;

    @Column(name = "cancellation_policy_id", nullable = false)
    private UUID cancellationPolicyId;

    @Column(name = "refund_policy_id", nullable = false)
    private UUID refundPolicyId;

    @Column(name = "fare_snapshot_id")
    private UUID fareSnapshotId;

    @Column(name = "current_total_paise", nullable = false)
    private long currentTotalPaise;

    @Column(name = "payment_state", nullable = false)
    private String paymentState;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public UUID getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(UUID customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public UUID getPickupZoneId() {
        return pickupZoneId;
    }

    public void setPickupZoneId(UUID pickupZoneId) {
        this.pickupZoneId = pickupZoneId;
    }

    public UUID getDropZoneId() {
        return dropZoneId;
    }

    public void setDropZoneId(UUID dropZoneId) {
        this.dropZoneId = dropZoneId;
    }

    public String getPickupAddressText() {
        return pickupAddressText;
    }

    public void setPickupAddressText(String pickupAddressText) {
        this.pickupAddressText = pickupAddressText;
    }

    public String getDropAddressText() {
        return dropAddressText;
    }

    public void setDropAddressText(String dropAddressText) {
        this.dropAddressText = dropAddressText;
    }

    public BigDecimal getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(BigDecimal pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public BigDecimal getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(BigDecimal pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public BigDecimal getDropLatitude() {
        return dropLatitude;
    }

    public void setDropLatitude(BigDecimal dropLatitude) {
        this.dropLatitude = dropLatitude;
    }

    public BigDecimal getDropLongitude() {
        return dropLongitude;
    }

    public void setDropLongitude(BigDecimal dropLongitude) {
        this.dropLongitude = dropLongitude;
    }

    public OffsetDateTime getScheduledPickupAt() {
        return scheduledPickupAt;
    }

    public void setScheduledPickupAt(OffsetDateTime scheduledPickupAt) {
        this.scheduledPickupAt = scheduledPickupAt;
    }

    public Integer getQuotedDurationMinutes() {
        return quotedDurationMinutes;
    }

    public void setQuotedDurationMinutes(Integer quotedDurationMinutes) {
        this.quotedDurationMinutes = quotedDurationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCurrentAssignmentId() {
        return currentAssignmentId;
    }

    public void setCurrentAssignmentId(UUID currentAssignmentId) {
        this.currentAssignmentId = currentAssignmentId;
    }

    public UUID getCurrentTripId() {
        return currentTripId;
    }

    public void setCurrentTripId(UUID currentTripId) {
        this.currentTripId = currentTripId;
    }

    public UUID getCancellationPolicyId() {
        return cancellationPolicyId;
    }

    public void setCancellationPolicyId(UUID cancellationPolicyId) {
        this.cancellationPolicyId = cancellationPolicyId;
    }

    public UUID getRefundPolicyId() {
        return refundPolicyId;
    }

    public void setRefundPolicyId(UUID refundPolicyId) {
        this.refundPolicyId = refundPolicyId;
    }

    public UUID getFareSnapshotId() {
        return fareSnapshotId;
    }

    public void setFareSnapshotId(UUID fareSnapshotId) {
        this.fareSnapshotId = fareSnapshotId;
    }

    public long getCurrentTotalPaise() {
        return currentTotalPaise;
    }

    public void setCurrentTotalPaise(long currentTotalPaise) {
        this.currentTotalPaise = currentTotalPaise;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
