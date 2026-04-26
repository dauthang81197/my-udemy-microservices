package com.thanghub.authservice.seed;

import com.thanghub.authservice.common.enums.UserStatusEnum;
import com.thanghub.authservice.permission.Permission;
import com.thanghub.authservice.permission.PermissionRepository;
import com.thanghub.authservice.role.Role;
import com.thanghub.authservice.role.RoleRepository;
import com.thanghub.authservice.rolePermission.RolePermission;
import com.thanghub.authservice.rolePermission.RolePermissionRepository;
import com.thanghub.authservice.user.User;
import com.thanghub.authservice.user.UserRepository;
import com.thanghub.authservice.userRole.UserRole;
import com.thanghub.authservice.userRole.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RBACSeeder implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // User
        List<User> users = List.of(
                new User("admin", "admin", "admin@example.com", "Admin@123", UserStatusEnum.ACTIVE),
                new User("dauthang", "Dau Thang", "thangdau811@gmail.com", "Admin@123", UserStatusEnum.ACTIVE)
        );
        users.forEach(u ->
                userRepository.findByEmail(u.getUsername())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.setFull_name(u.getFull_name());
                                    existing.setStatus(u.getStatus());
                                    existing.setPassword(passwordEncoder.encode(u.getPassword()));
                                    userRepository.save(existing);
                                },
                                () -> {
                                    u.setPassword(passwordEncoder.encode(u.getPassword()));
                                    userRepository.save(u);
                                }
                        )
        );
        System.out.println("✅ Seeded/updated " + users.size() + " users");

        // Permission
        List<Permission> permissions = List.of(
                new Permission("Create Admin Course", "ADMIN::COURSE::CREATE", "Create course"),
                new Permission("Update Admin Course", "ADMIN::COURSE::UPDATE", "Update course"),
                new Permission("Delete Admin Course", "ADMIN::COURSE::DELETE", "Delete course"),
                new Permission("Get Admin Course", "ADMIN::COURSE::GET", "Get course"),

                new Permission("Create Course", "USER::COURSE::CREATE", "Create course"),
                new Permission("Update Course", "USER::COURSE::UPDATE", "Update course"),
                new Permission("Delete Course", "USER::COURSE::DELETE", "Delete course"),
                new Permission("Get Course", "USER::COURSE::GET", "Get course")
        );
        permissions.forEach(p ->
                permissionRepository.findByCode(p.getCode())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.setName(p.getName());
                                    existing.setDescription(p.getDescription());
                                    permissionRepository.save(existing);
                                },
                                () -> permissionRepository.save(p)
                        )
        );
        System.out.println("✅ Seeded/updated " + permissions.size() + " permissions");

        // Role
        List<Role> roles = List.of(
                new Role("Admin", "ADMIN", "Full access to all resources"),
                new Role("User", "USER", "Standard user access"),
                new Role("Instructor", "INSTRUCTOR", "Can create and manage courses"),
                new Role("Guest", "GUEST", "Read-only access to public content")
        );
        roles.forEach(r ->
                roleRepository.findByCode(r.getCode())
                        .ifPresentOrElse(
                                existing -> {
                                    existing.setName(r.getName());
                                    existing.setDescription(r.getDescription());
                                    roleRepository.save(existing);
                                },
                                () -> roleRepository.save(r)
                        )
        );
        System.out.println("✅ Seeded/updated " + roles.size() + " roles");

        // RolePermission
        // ADMIN      → ADMIN::COURSE::CREATE/UPDATE/DELETE/GET
        // USER       → USER::COURSE::CREATE/UPDATE/DELETE/GET
        // INSTRUCTOR → ADMIN::COURSE::CREATE/UPDATE/GET
        // GUEST      → USER::COURSE::GET
        Map<String, List<String>> rolePermissionMap = Map.of(
                "ADMIN", List.of("ADMIN::COURSE::CREATE", "ADMIN::COURSE::UPDATE", "ADMIN::COURSE::DELETE", "ADMIN::COURSE::GET"),
                "USER", List.of("USER::COURSE::CREATE", "USER::COURSE::UPDATE", "USER::COURSE::DELETE", "USER::COURSE::GET"),
                "INSTRUCTOR", List.of("ADMIN::COURSE::CREATE", "ADMIN::COURSE::UPDATE", "ADMIN::COURSE::GET"),
                "GUEST", List.of("USER::COURSE::GET")
        );

        int[] rpCount = {0};
        rolePermissionMap.forEach((roleCode, permCodes) -> {
            Optional<Role> role = roleRepository.findByCode(roleCode);
            if (role.isEmpty()) return;
            permCodes.forEach(permCode -> {
                Optional<Permission> permission = permissionRepository.findByCode(permCode);
                if (permission.isEmpty()) return;
                if (!rolePermissionRepository.existsByRoleIdAndPermissionId(role.get().getId(), permission.get().getId())) {
                    rolePermissionRepository.save(new RolePermission(role.get().getId(), permission.get().getId(), LocalDateTime.now()));
                    rpCount[0]++;
                }
            });
        });
        System.out.println("✅ Seeded " + rpCount[0] + " role-permission assignments");

        // UserRole
        // admin@example.com     → ADMIN
        // thangdau811@gmail.com → USER
        Map<String, String> userRoleMap = Map.of(
                "admin@example.com", "ADMIN",
                "thangdau811@gmail.com", "USER"
        );

        int[] urCount = {0};
        userRoleMap.forEach((email, roleCode) -> {
            Optional<User> user = userRepository.findByEmail(email);
            Optional<Role> role = roleRepository.findByCode(roleCode);
            if (user.isEmpty() || role.isEmpty()) return;
            if (!userRoleRepository.existsByUserIdAndRoleId(user.get().getId(), role.get().getId())) {
                userRoleRepository.save(new UserRole(user.get().getId(), role.get().getId(), LocalDateTime.now()));
                urCount[0]++;
            }
        });
        System.out.println("✅ Seeded " + urCount[0] + " user-role assignments");
    }
}