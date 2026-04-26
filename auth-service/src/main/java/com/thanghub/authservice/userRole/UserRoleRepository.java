package com.thanghub.authservice.userRole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);
}