package com.thanghub.authservice.rolePermission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}