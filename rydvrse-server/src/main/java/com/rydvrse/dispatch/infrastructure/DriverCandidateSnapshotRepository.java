package com.rydvrse.dispatch.infrastructure;

import com.rydvrse.dispatch.domain.DriverCandidateSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverCandidateSnapshotRepository extends JpaRepository<DriverCandidateSnapshotEntity, UUID> {

    List<DriverCandidateSnapshotEntity> findByAssignmentIdOrderBySnapshotRankAsc(UUID assignmentId);
}
