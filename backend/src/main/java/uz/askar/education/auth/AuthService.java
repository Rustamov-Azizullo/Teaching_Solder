package uz.askar.education.auth;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.auth.AuthDtos.LoginRequest;
import uz.askar.education.auth.AuthDtos.LoginResponse;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.config.SecurityProperties;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;
import uz.askar.education.users.UserDtos.UserDto;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_PASSWORD = "dummy-password-for-timing";
    private static final String INVALID_CREDENTIALS =
            "Login yoki parol noto'g'ri, yoki hisob vaqtincha bloklangan. Birozdan keyin qayta urinib ko'ring";

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final AuditService audit;
    private final SecurityProperties securityProperties;
    private final PermissionEvaluatorService permissions;
    private volatile String dummyPasswordHash;

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByUsername(request.username()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash());
            throw failedLogin();
        }
        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!user.isActive() || isLocked(user)) {
            throw failedLogin();
        }
        if (!passwordMatches) {
            registerFailure(user);
            throw failedLogin();
        }
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        audit.record(user.getUsername(), "LOGIN", "AppUser", user.getId(), "Tizimga kirdi");
        return new LoginResponse(tokens.issue(user), tokens.lifetime().toSeconds(), toDto(user));
    }

    @Transactional(readOnly = true)
    public UserDto me(String username) {
        return users.findByUsername(username).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
    }

    private void registerFailure(AppUser user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= securityProperties.security().maxFailedLogins()) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(securityProperties.security().lockMinutes()));
            user.setFailedAttempts(0);
            audit.record(user.getUsername(), "ACCOUNT_LOCKED", "AppUser", user.getId(),
                    "Ketma-ket muvaffaqiyatsiz urinishlar");
        }
    }

    private UserDto toDto(AppUser user) {
        return UserDto.from(user, permissions.effectivePermissions(user.getId(), user.getRole()));
    }

    /** Mavjud bo'lmagan login uchun ham parol tekshiruvi vaqtini tenglashtiradi (login nomini aniqlashga qarshi). */
    private String dummyHash() {
        if (dummyPasswordHash == null) {
            dummyPasswordHash = passwordEncoder.encode(DUMMY_PASSWORD);
        }
        return dummyPasswordHash;
    }

    private boolean isLocked(AppUser user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private InvalidCredentialsException failedLogin() {
        return new InvalidCredentialsException(INVALID_CREDENTIALS);
    }
}
