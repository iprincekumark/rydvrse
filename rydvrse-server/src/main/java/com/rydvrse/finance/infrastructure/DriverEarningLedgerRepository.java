package com.rydvrse.finance.infrastructure;

import com.rydvrse.finance.domain.DriverEarningLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverEarningLedgerRepository extends JpaRepository<DriverEarningLedgerEntity, UUID> {

    @Query("select coalesce(sum(ledger.netPayoutPaise), 0) from DriverEarningLedgerEntity ledger where ledger.driverProfileId = :driverProfileId")
    Optional<Long> sumNetPayoutPaiseByDriverProfileId(UUID driverProfileId);

    List<DriverEarningLedgerEntity> findByDriverProfileIdOrderByCreatedAtDesc(UUID driverProfileId);

    @Query("""
            select coalesce(sum(ledger.netPayoutPaise), 0)
            from DriverEarningLedgerEntity ledger
            where ledger.driverProfileId = :driverProfileId
              and ledger.createdAt between :from and :to
            """)
    Optional<Long> sumNetPayoutPaiseByDriverProfileIdAndCreatedAtBetween(UUID driverProfileId, OffsetDateTime from, OffsetDateTime to);

    long countByDriverProfileIdAndCreatedAtBetween(UUID driverProfileId, OffsetDateTime from, OffsetDateTime to);

    @Query("""
            select coalesce(sum(ledger.netPayoutPaise), 0)
            from DriverEarningLedgerEntity ledger
            where ledger.driverProfileId = :driverProfileId
              and ledger.ledgerStatus <> 'SETTLED'
            """)
    Optional<Long> sumPendingByDriverProfileId(UUID driverProfileId);
}
