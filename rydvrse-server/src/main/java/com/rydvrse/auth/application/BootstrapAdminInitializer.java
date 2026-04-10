package com.rydvrse.auth.application;

import com.rydvrse.auth.domain.UserAccountEntity;
import com.rydvrse.auth.domain.UserRoleBindingEntity;
import com.rydvrse.auth.infrastructure.RoleRepository;
import com.rydvrse.auth.infrastructure.UserAccountRepository;
import com.rydvrse.auth.infrastructure.UserRoleBindingRepository;
import com.rydvrse.common.config.RydvrseProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;

@Configuration
public class BootstrapAdminInitializer {

    @Bean
    ApplicationRunner bootstrapAdminRunner(
            UserAccountRepository userAccountRepository,
            UserRoleBindingRepository userRoleBindingRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            RydvrseProperties rydvrseProperties
    ) {
        return args -> userAccountRepository.findByEmailIgnoreCase(rydvrseProperties.getSecurity().getBootstrapAdminEmail())
                .orElseGet(() -> {
                    UserAccountEntity admin = new UserAccountEntity();
                    admin.setUserType("ADMIN");
                    admin.setMobileCountryCode("+91");
                    admin.setMobileNumberE164("+910000000000");
                    admin.setEmail(rydvrseProperties.getSecurity().getBootstrapAdminEmail());
                    admin.setPasswordHash(passwordEncoder.encode(rydvrseProperties.getSecurity().getBootstrapAdminPassword()));
                    admin.setStatus("ACTIVE");
                    admin.setMobileVerified(true);
                    userAccountRepository.saveAndFlush(admin);

                    roleRepository.findByRoleCodeIn(java.util.List.of("SUPER_ADMIN")).stream().findFirst().ifPresent(role -> {
                        UserRoleBindingEntity binding = new UserRoleBindingEntity();
                        binding.setUserAccountId(admin.getId());
                        binding.setRoleId(role.getId());
                        binding.setBindingStatus("ACTIVE");
                        binding.setEffectiveFrom(OffsetDateTime.now());
                        userRoleBindingRepository.save(binding);
                    });
                    return admin;
                });
    }
}
