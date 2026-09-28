package uz.askar.education.security;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BadRequestException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;

/**
 * Rol-ruxsat matritsasi (ADMIN, USER) va foydalanuvchiga shaxsiy ruxsatlarni boshqarish.
 * Yozuvlar bir-bir qo'llanadi: {@code granted=true} — ruxsat qo'shiladi, {@code false} — olib tashlanadi;
 * so'rovda ko'rsatilmagan juftliklar o'zgarmaydi.
 */
@Service
@RequiredArgsConstructor
public class PermissionManagementService {

    public record RolePermissionEntry(Role role, Permission permission, boolean granted) {
    }

    public record UserPermissionEntry(Permission permission, boolean granted) {
    }

    /** {@code granted} — shaxsiy ruxsat bor; {@code grantedByRole} — rol orqali allaqachon bor. */
    public record UserPermissionState(Permission permission, boolean granted, boolean grantedByRole) {
    }

    private static final List<Role> CONFIGURABLE_ROLES =
            Arrays.stream(Role.values()).filter(Role::isConfigurable).toList();

    private final RolePermissionRepository rolePermissions;
    private final UserPermissionRepository userPermissions;
    private final AppUserRepository users;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<RolePermissionEntry> roleMatrix() {
        Set<String> granted = rolePermissions.findByRoleIn(CONFIGURABLE_ROLES).stream()
                .map(grant -> key(grant.getRole(), grant.getPermission()))
                .collect(Collectors.toSet());
        return CONFIGURABLE_ROLES.stream()
                .flatMap(role -> Arrays.stream(Permission.values())
                        .map(permission -> new RolePermissionEntry(role, permission,
                                granted.contains(key(role, permission)))))
                .toList();
    }

    @Transactional
    public List<RolePermissionEntry> updateRoleMatrix(List<RolePermissionEntry> entries) {
        entries.forEach(entry -> {
            requirePresent(entry.role(), entry.permission());
            requireConfigurable(entry.role());
        });
        for (RolePermissionEntry entry : entries) {
            boolean exists = rolePermissions.existsByRoleAndPermission(entry.role(), entry.permission());
            if (entry.granted() && !exists) {
                rolePermissions.save(new RolePermission(entry.role(), entry.permission()));
            } else if (!entry.granted() && exists) {
                rolePermissions.deleteByRoleAndPermission(entry.role(), entry.permission());
            }
        }
        audit.record("UPDATE", "RolePermission", null, "o'zgarishlar: " + entries.size());
        return roleMatrix();
    }

    @Transactional(readOnly = true)
    public List<UserPermissionState> userPermissions(Long userId) {
        AppUser user = findUser(userId);
        Set<Permission> overrides = overridesOf(userId);
        Set<Permission> byRole = EnumSet.noneOf(Permission.class);
        if (user.getRole().isAlwaysAllowed()) {
            byRole.addAll(EnumSet.allOf(Permission.class));
        } else {
            rolePermissions.findByRole(user.getRole()).forEach(grant -> byRole.add(grant.getPermission()));
        }
        return Arrays.stream(Permission.values())
                .map(permission -> new UserPermissionState(permission, overrides.contains(permission),
                        byRole.contains(permission)))
                .toList();
    }

    @Transactional
    public List<UserPermissionState> updateUserPermissions(Long userId, List<UserPermissionEntry> entries) {
        AppUser user = findUser(userId);
        requireConfigurable(user.getRole());
        entries.forEach(entry -> requirePresent(user.getRole(), entry.permission()));
        Set<Permission> overrides = overridesOf(userId);
        entries.stream().filter(UserPermissionEntry::granted).map(UserPermissionEntry::permission)
                .filter(permission -> !overrides.contains(permission))
                .distinct()
                .forEach(permission -> userPermissions.save(new UserPermission(userId, permission)));
        Set<Permission> revoked = entries.stream().filter(entry -> !entry.granted())
                .map(UserPermissionEntry::permission).collect(Collectors.toSet());
        userPermissions.findByUserId(userId).stream()
                .filter(grant -> revoked.contains(grant.getPermission()))
                .forEach(userPermissions::delete);
        audit.record("UPDATE", "UserPermission", userId, "login=" + user.getUsername() + ", o'zgarishlar: "
                + entries.size());
        return userPermissions(userId);
    }

    private Set<Permission> overridesOf(Long userId) {
        Set<Permission> result = EnumSet.noneOf(Permission.class);
        userPermissions.findByUserId(userId).forEach(grant -> result.add(grant.getPermission()));
        return result;
    }

    private AppUser findUser(Long userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
    }

    private void requirePresent(Role role, Permission permission) {
        if (role == null || permission == null) {
            throw new BadRequestException("Rol va ruxsat ko'rsatilishi shart");
        }
    }

    private void requireConfigurable(Role role) {
        if (!role.isConfigurable()) {
            throw new BadRequestException(role.label() + " roli har doim barcha ruxsatlarga ega va sozlanmaydi");
        }
    }

    private String key(Role role, Permission permission) {
        return role.name() + ":" + permission.name();
    }
}
