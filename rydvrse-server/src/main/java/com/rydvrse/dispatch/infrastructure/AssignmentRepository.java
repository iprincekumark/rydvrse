package com.rydvrse.dispatch.infrastructure;

import com.rydvrse.dispatch.domain.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    Optional<AssignmentEntity> findByIdAndBookingId(UUID id, UUID bookingId);

    Optional<AssignmentEntity> findByBookingIdAndCurrentTrue(UUID bookingId);

    List<AssignmentEntity> findByStatusInOrderByUpdatedAtDesc(List<String> statuses);

    List<AssignmentEntity> findByDriverProfileIdAndStatusOrderByUpdatedAtDesc(UUID driverProfileId, String status);

    List<AssignmentEntity> findByDriverProfileIdAndStatusInOrderByUpdatedAtDesc(UUID driverProfileId, List<String> statuses);
}
