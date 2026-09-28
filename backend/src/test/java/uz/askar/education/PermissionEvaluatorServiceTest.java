package uz.askar.education;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.security.Role;
import uz.askar.education.security.RolePermission;
import uz.askar.education.security.RolePermissionRepository;
import uz.askar.education.security.UserPermission;
import uz.askar.education.security.UserPermissionRepository;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;

/** {@code @perm.has(...)} qoidalari; har bir test tranzaksiyasi oxirida bekor qilinadi (umumiy bazaga ta'sir qilmaydi). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PermissionEvaluatorServiceTest {

    @Autowired
    private PermissionEvaluatorService perm;

    @Autowired
    private RolePermissionRepository rolePermissions;

    @Autowired
    private UserPermissionRepository userPermissions;

    @Autowired
    private AppUserRepository users;

    @Test
    void megaSuperAdminPassesEveryCheckRegardlessOfStoredGrants() {
        rolePermissions.deleteAll();
        userPermissions.deleteAll();
        Authentication superAdmin = authentication(userId("superadmin"), Role.SUPER_ADMIN);
        Authentication megaSuperAdmin = authentication(userId("megasuperadmin"), Role.MEGA_SUPER_ADMIN);
        for (Permission permission : Permission.values()) {
            assertTrue(perm.has(megaSuperAdmin, permission.name()), permission::name);
            assertFalse(perm.has(superAdmin, permission.name()), "SuperAdmin ruxsati bazadagi yozuvga bog'liq");
        }
        assertFalse(perm.has(authentication(userId("okrug1"), Role.ADMIN), Permission.ADMIN.name()));
    }

    @Test
    void adminIsDeniedWithoutRoleGrantAndAllowedOnceGranted() {
        Authentication admin = authentication(userId("okrug1"), Role.ADMIN);
        assertFalse(perm.has(admin, Permission.DICTIONARY_WRITE.name()));
        rolePermissions.save(new RolePermission(Role.ADMIN, Permission.DICTIONARY_WRITE));
        assertTrue(perm.has(admin, Permission.DICTIONARY_WRITE.name()));
    }

    @Test
    void userOverrideIsAdditiveOnTopOfRoleGrants() {
        Long leaderId = userId("katta1");
        Authentication leader = authentication(leaderId, Role.USER);
        assertTrue(perm.has(leader, Permission.GROUP_READ.name()), "USER rolining umumiy ruxsati");
        assertFalse(perm.has(leader, Permission.RESULT_READ.name()));
        userPermissions.save(new UserPermission(leaderId, Permission.RESULT_READ));
        assertTrue(perm.has(leader, Permission.RESULT_READ.name()));
        assertFalse(perm.has(authentication(userId("katta2"), Role.USER), Permission.RESULT_READ.name()),
                "Shaxsiy ruxsat faqat o'sha foydalanuvchiga tegishli");
    }

    @Test
    void unauthenticatedCallerIsDenied() {
        assertFalse(perm.has(null, Permission.GROUP_READ.name()));
    }

    private Long userId(String username) {
        return users.findByUsername(username).map(AppUser::getId).orElseThrow();
    }

    private Authentication authentication(Long userId, Role role) {
        Jwt jwt = Jwt.withTokenValue("test")
                .header("alg", "HS256")
                .subject("test")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim(CurrentUser.CLAIM_USER_ID, userId)
                .claim(CurrentUser.CLAIM_ROLE, role.name())
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }
}
