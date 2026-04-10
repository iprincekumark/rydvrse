package com.rydvrse.auth.infrastructure;

import com.rydvrse.auth.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    List<RoleEntity> findByIdIn(Collection<UUID> ids);

    List<RoleEntity> findByRoleCodeIn(Collection<String> roleCodes);
}
