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

    private static final String INVALID_CREDENTIALS = "Login yoki parol noto'g'ri";

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final AuditService audit;
    private final SecurityProperties securityProperties;
    private final PermissionEvaluatorService permissions;

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByUsername(request.username()).orElseThrow(() -> failedLogin(request.username()));
        if (!user.isActive()) {
            throw failedLogin(request.username());
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new InvalidCredentialsException(
                    "Hisob vaqtincha bloklangan. Birozdan keyin qayta urinib ko'ring");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailure(user);
            throw failedLogin(request.username());
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

    private InvalidCredentialsException failedLogin(String username) {
        return new InvalidCredentialsException(INVALID_CREDENTIALS);
    }
}
