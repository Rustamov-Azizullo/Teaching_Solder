package uz.askar.education.auth;

import java.time.Instant;
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
    private final uz.askar.education.settings.SettingsService settings;
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
        verifyOtp(user, request.otp());
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        audit.record(user.getUsername(), "LOGIN", "AppUser", user.getId(), "Tizimga kirdi");
        boolean setupRequired = !user.isTotpEnabled() && user.getRole().scopeLevel() != uz.askar.education.security.ScopeLevel.UNIT
                && "true".equals(settings.get("security.2fa.required", "false"));
        return new LoginResponse(tokens.issue(user), tokens.lifetime().toSeconds(), toDto(user),
                user.isTotpEnabled(), setupRequired);
    }

    @Transactional(readOnly = true)
    public UserDto me(String username) {
        return users.findByUsername(username).map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
    }

    @Transactional
    public AuthDtos.TwoFactorSetup setupTwoFactor(String username) {
        AppUser user = users.findByUsername(username).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        user.setTotpSecret(Totp.newSecret());
        user.setTotpEnabled(false);
        String uri = "otpauth://totp/AskarTalimi:" + user.getUsername() + "?secret=" + user.getTotpSecret()
                + "&issuer=AskarTalimi";
        return new AuthDtos.TwoFactorSetup(user.getTotpSecret(), uri);
    }

    @Transactional
    public void enableTwoFactor(String username, String code) {
        AppUser user = users.findByUsername(username).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        if (user.getTotpSecret() == null || !Totp.verify(user.getTotpSecret(), code, Instant.now().getEpochSecond())) {
            throw new InvalidCredentialsException("Kod noto'g'ri", InvalidCredentialsException.OTP_INVALID);
        }
        user.setTotpEnabled(true);
        audit.record(username, "2FA_ENABLED", "AppUser", user.getId(), "Ikki bosqichli autentifikatsiya yoqildi");
    }

    @Transactional
    public void disableTwoFactor(String username, String code) {
        AppUser user = users.findByUsername(username).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        if (!user.isTotpEnabled() || !Totp.verify(user.getTotpSecret(), code, Instant.now().getEpochSecond())) {
            throw new InvalidCredentialsException("Kod noto'g'ri", InvalidCredentialsException.OTP_INVALID);
        }
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        audit.record(username, "2FA_DISABLED", "AppUser", user.getId(), "Ikki bosqichli autentifikatsiya o'chirildi");
    }

    private void verifyOtp(AppUser user, String otp) {
        if (!user.isTotpEnabled()) {
            return;
        }
        if (otp == null || otp.isBlank()) {
            throw new InvalidCredentialsException("Autentifikator ilovasidagi 6 xonali kodni kiriting",
                    InvalidCredentialsException.OTP_REQUIRED);
        }
        if (!Totp.verify(user.getTotpSecret(), otp, Instant.now().getEpochSecond())) {
            registerFailure(user);
            throw new InvalidCredentialsException("Kod noto'g'ri", InvalidCredentialsException.OTP_INVALID);
        }
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
