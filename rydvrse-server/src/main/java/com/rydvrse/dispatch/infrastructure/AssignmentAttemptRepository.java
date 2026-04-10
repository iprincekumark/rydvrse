package com.rydvrse.dispatch.infrastructure;

import com.rydvrse.dispatch.domain.AssignmentAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentAttemptRepository extends JpaRepository<AssignmentAttemptEntity, UUID> {

    List<AssignmentAttemptEntity> findByAssignmentIdOrderByOfferSequenceNoAsc(UUID assignmentId);

    Optional<AssignmentAttemptEntity> findFirstByAssignmentIdAndDriverProfileIdOrderByOfferSequenceNoDesc(UUID assignmentId, UUID driverProfileId);

    List<AssignmentAttemptEntity> findByAttemptStatusAndOfferedAtBefore(String attemptStatus, OffsetDateTime offeredBefore);
}
