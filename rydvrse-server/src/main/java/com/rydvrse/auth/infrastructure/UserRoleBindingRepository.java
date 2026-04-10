package com.rydvrse.auth.infrastructure;

import com.rydvrse.auth.domain.UserRoleBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface UserRoleBindingRepository extends JpaRepository<UserRoleBindingEntity, UUID> {

    @Query("""
            select binding
            from UserRoleBindingEntity binding
            where binding.userAccountId = :userAccountId
              and binding.bindingStatus = :bindingStatus
              and (binding.effectiveTo is null or binding.effectiveTo > :asOf)
            """)
    List<UserRoleBindingEntity> findActiveBindings(
            @Param("userAccountId") UUID userAccountId,
            @Param("bindingStatus") String bindingStatus,
            @Param("asOf") OffsetDateTime asOf
    );
}
