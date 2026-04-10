package com.rydvrse.master.infrastructure;

import com.rydvrse.master.domain.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<CityEntity, UUID> {

    Optional<CityEntity> findByCityCode(String cityCode);
}
