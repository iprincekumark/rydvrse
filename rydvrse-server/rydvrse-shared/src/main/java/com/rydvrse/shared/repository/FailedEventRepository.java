package com.rydvrse.shared.repository;

import com.rydvrse.shared.entity.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent, UUID> {
}
