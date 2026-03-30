package com.rydvrse.driver.repository;

import com.rydvrse.driver.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    Optional<Driver> findByPhone(String phone);
    Optional<Driver> findByEmail(String email);
    boolean existsByPhone(String phone);
    Page<Driver> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
