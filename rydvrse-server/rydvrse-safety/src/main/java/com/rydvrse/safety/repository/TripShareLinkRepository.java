package com.rydvrse.safety.repository;

import com.rydvrse.safety.entity.TripShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripShareLinkRepository extends JpaRepository<TripShareLink, UUID> {
    Optional<TripShareLink> findByShareTokenAndIsActiveTrue(UUID shareToken);
}
