package uz.askar.education.users;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.ForbiddenException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.locations.Location;
import uz.askar.education.locations.LocationRepository;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.security.Role;
import uz.askar.education.security.ScopeLevel;
import uz.askar.education.users.UserDtos.CreateUserRequest;
import uz.askar.education.users.UserDtos.UpdateUserRequest;
import uz.askar.education.users.UserDtos.UserDto;

/**
 * Foydalanuvchilarni boshqarish. Chaqiruvchi faqat o'z vakolat doirasidagi (okrug admini — o'z okrugidagi)
 * va o'zidan yuqori bo'lmagan roldagi hisoblarni ko'radi va boshqaradi.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository users;
    private final LocationRepository locations;
    private final PasswordEncoder passwordEncoder;
    private final PermissionEvaluatorService permissions;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        AccessScope scope = currentUser.scope();
        return users.findInDistrictScope(scope.districtFilter()).stream()
                .filter(user -> scope.covers(user.effectiveDistrictId(), user.effectiveUnitId()))
                .map(this::toDto)
                .toList();
    }

    /** Joriy foydalanuvchi bera oladigan rollar (o'zidan yuqori bo'lmaganlari). */
    public List<Role> assignableRoles() {
        Role callerRole = currentUser.role();
        return Arrays.stream(Role.values()).filter(callerRole::isAtLeast).toList();
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new BusinessRuleException("Bunday login allaqachon mavjud");
        }
        PasswordPolicy.check(request.password());
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        applyProfile(user, request.fullName(), request.role(), request.locationId());
        AppUser saved = users.save(user);
        audit.record("CREATE", "AppUser", saved.getId(), "login=" + saved.getUsername() + ", rol=" + saved.getRole());
        return toDto(saved);
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        AppUser user = findManageable(id);
        String previousUsername = user.getUsername();
        changeUsername(user, request.username());
        applyProfile(user, request.fullName(), request.role(), request.locationId());
        user.setActive(request.active());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            PasswordPolicy.check(request.newPassword());
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        audit.record("UPDATE", "AppUser", id, "login=" + previousUsername + " -> " + user.getUsername()
                + ", rol=" + user.getRole() + ", faol=" + user.isActive());
        return toDto(user);
    }

    /** Foydalanuvchini o'chiradi; o'zini o'chirib bo'lmaydi, anketa/guruhga bog'langan bo'lsa — faolsizlantirish tavsiya etiladi. */
    @Transactional
    public void delete(Long id) {
        AppUser user = findManageable(id);
        if (user.getId().equals(currentUser.id())) {
            throw new BusinessRuleException("O'zingizning hisobingizni o'chirib bo'lmaydi");
        }
        String username = user.getUsername();
        try {
            users.delete(user);
            users.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Foydalanuvchi anketa yoki guruhlarga bog'langan, o'chirib bo'lmaydi. "
                    + "Uni bloklang");
        }
        audit.record("DELETE", "AppUser", id, "login=" + username);
    }

    /** Chaqiruvchi boshqara oladigan foydalanuvchini topadi (vakolat doirasi va rol ierarxiyasi bo'yicha). */
    public AppUser findManageable(Long id) {
        AppUser user = users.findById(id).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        currentUser.scope().require(user.effectiveDistrictId(), user.effectiveUnitId());
        requireAssignable(user.getRole());
        return user;
    }

    public UserDto toDto(AppUser user) {
        return UserDto.from(user, permissions.effectivePermissions(user.getId(), user.getRole()));
    }

    /** Login o'zgartirilsa, u boshqa hisobda band bo'lmasligi shart. */
    private void changeUsername(AppUser user, String requested) {
        if (requested == null || requested.isBlank() || requested.trim().equals(user.getUsername())) {
            return;
        }
        String username = requested.trim();
        if (users.existsByUsername(username)) {
            throw new BusinessRuleException("Bunday login allaqachon mavjud");
        }
        user.setUsername(username);
    }

    private void applyProfile(AppUser user, String fullName, Role role, Long locationId) {
        requireAssignable(role);
        user.setFullName(fullName);
        user.setRole(role);
        user.setLocation(resolveLocation(role, locationId));
    }

    private void requireAssignable(Role role) {
        if (!currentUser.role().isAtLeast(role)) {
            throw new ForbiddenException("O'zingizdan yuqori roldagi hisobni boshqarish mumkin emas");
        }
    }

    /** Rolning vakolat darajasiga mos hududni tanlaydi va u chaqiruvchining vakolat doirasida ekanini tekshiradi. */
    private Location resolveLocation(Role role, Long locationId) {
        ScopeLevel level = role.scopeLevel();
        if (level == ScopeLevel.REPUBLIC) {
            return null;
        }
        if (locationId == null) {
            throw new BusinessRuleException(levelLabel(level) + " tanlanishi shart");
        }
        Location location = locations.findById(locationId)
                .orElseThrow(() -> new NotFoundException("Hudud topilmadi"));
        if (!location.getLevel().name().equals(level.name())) {
            throw new BusinessRuleException(role.label() + " roli uchun " + levelLabel(level).toLowerCase()
                    + " tanlanishi kerak");
        }
        currentUser.scope().require(location.districtId(), location.unitId());
        return location;
    }

    private String levelLabel(ScopeLevel level) {
        return level == ScopeLevel.DISTRICT ? "Harbiy okrug" : "Harbiy qism";
    }
}
