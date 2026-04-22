package com.thanghub.authservice.rolePermission;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RolePermissionId.class)
public class RolePermission {

    @Id
    private UUID roleId;

    @Id
    private UUID permissionId;

    private LocalDateTime assignedAt;
}
