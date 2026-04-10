package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.BusinessConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessConfigRepository extends JpaRepository<BusinessConfigEntity, UUID> {

    List<BusinessConfigEntity> findByConfigKeyAndActiveTrue(String configKey);

    List<BusinessConfigEntity> findByActiveTrue();
}
