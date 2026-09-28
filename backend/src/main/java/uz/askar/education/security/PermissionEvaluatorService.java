package uz.askar.education.security;

import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Dinamik ruxsat tekshiruvi. SpEL'da {@code @perm} nomi bilan ishlatiladi:
 * {@code @PreAuthorize("@perm.has(authentication,'SOLDIER_WRITE')")}.
 *
 * <ul>
 *   <li>SUPER_ADMIN va MEGA_SUPER_ADMIN — har doim ruxsat (o'zgartirib bo'lmaydi);</li>
 *   <li>ADMIN va USER — foydalanuvchining shaxsiy ruxsati YOKI rolining ruxsati bo'lsa.</li>
 * </ul>
 */
@Component("perm")
@RequiredArgsConstructor
public class PermissionEvaluatorService {

    private final RolePermissionRepository rolePermissions;
    private final UserPermissionRepository userPermissions;

    /** SpEL kirish nuqtasi. Noma'lum ruxsat nomi dasturchi xatosi hisoblanadi va istisno bilan to'xtatiladi. */
    public boolean has(Authentication authentication, String permission) {
        Permission required = Permission.valueOf(permission);
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return false;
        }
        Jwt token = jwtAuthentication.getToken();
        Object userId = token.getClaim(CurrentUser.CLAIM_USER_ID);
        String role = token.getClaimAsString(CurrentUser.CLAIM_ROLE);
        if (!(userId instanceof Number number) || role == null) {
            return false;
        }
        return holds(number.longValue(), Role.valueOf(role), required);
    }

    /** Joriy so'rov foydalanuvchisi uchun (servis qatlamidagi ma'lumotga bog'liq tekshiruvlar uchun). */
    public boolean currentUserHas(Permission permission) {
        return has(SecurityContextHolder.getContext().getAuthentication(), permission.name());
    }

    public boolean holds(Long userId, Role role, Permission permission) {
        if (role.isAlwaysAllowed()) {
            return true;
        }
        return userPermissions.existsByUserIdAndPermission(userId, permission)
                || rolePermissions.existsByRoleAndPermission(role, permission);
    }

    /** Foydalanuvchi amalda ega bo'lgan barcha ruxsatlar (rol + shaxsiy). */
    public Set<Permission> effectivePermissions(Long userId, Role role) {
        if (role.isAlwaysAllowed()) {
            return EnumSet.allOf(Permission.class);
        }
        Set<Permission> result = EnumSet.noneOf(Permission.class);
        rolePermissions.findByRole(role).forEach(grant -> result.add(grant.getPermission()));
        if (userId != null) {
            userPermissions.findByUserId(userId).forEach(grant -> result.add(grant.getPermission()));
        }
        return result;
    }
}
