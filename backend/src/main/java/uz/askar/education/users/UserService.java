package uz.askar.education.users;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.organization.MilitaryDistrictRepository;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.security.Role;
import uz.askar.education.security.ScopeLevel;
import uz.askar.education.users.UserDtos.CreateUserRequest;
import uz.askar.education.users.UserDtos.UpdateUserRequest;
import uz.askar.education.users.UserDtos.UserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository users;
    private final MilitaryDistrictRepository militaryDistricts;
    private final MilitaryUnitRepository militaryUnits;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return users.findAllByOrderByFullNameAsc().stream().map(UserDto::from).toList();
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
        applyProfile(user, request.fullName(), request.role(), request.militaryDistrictId(),
                request.militaryUnitId());
        AppUser saved = users.save(user);
        audit.record("CREATE", "AppUser", saved.getId(), "login=" + saved.getUsername() + ", rol=" + saved.getRole());
        return UserDto.from(saved);
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        AppUser user = users.findById(id).orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        applyProfile(user, request.fullName(), request.role(), request.militaryDistrictId(),
                request.militaryUnitId());
        user.setActive(request.active());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            PasswordPolicy.check(request.newPassword());
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        audit.record("UPDATE", "AppUser", id, "rol=" + user.getRole() + ", faol=" + user.isActive());
        return UserDto.from(user);
    }

    private void applyProfile(AppUser user, String fullName, Role role, Long districtId, Long unitId) {
        user.setFullName(fullName);
        user.setRole(role);
        user.setMilitaryDistrict(null);
        user.setMilitaryUnit(null);
        if (role.scopeLevel() == ScopeLevel.DISTRICT) {
            user.setMilitaryDistrict(militaryDistricts.findById(requireId(districtId, "Harbiy okrug"))
                    .orElseThrow(() -> new NotFoundException("Harbiy okrug topilmadi")));
        }
        if (role.scopeLevel() == ScopeLevel.UNIT) {
            user.setMilitaryUnit(militaryUnits.findById(requireId(unitId, "Harbiy qism"))
                    .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi")));
        }
    }

    private Long requireId(Long id, String label) {
        if (id == null) {
            throw new BusinessRuleException(label + " tanlanishi shart");
        }
        return id;
    }
}
