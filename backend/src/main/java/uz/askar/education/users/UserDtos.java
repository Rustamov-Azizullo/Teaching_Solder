package uz.askar.education.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.askar.education.security.Role;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserDto(Long id, String username, String fullName, Role role, String roleLabel,
                          Long militaryDistrictId, Long militaryUnitId, String militaryUnitName, boolean active, boolean twoFactorEnabled) {

        public static UserDto from(AppUser user) {
            return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getRole(),
                    user.getRole().label(), user.effectiveDistrictId(),
                    user.getMilitaryUnit() != null ? user.getMilitaryUnit().getId() : null,
                    user.getMilitaryUnit() != null ? user.getMilitaryUnit().getName() : null, user.isActive(),
                    user.isTotpEnabled());
        }
    }

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 60) String username,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank String fullName,
            @NotNull Role role,
            Long militaryDistrictId,
            Long militaryUnitId) {
    }

    public record UpdateUserRequest(
            @NotBlank String fullName,
            @NotNull Role role,
            Long militaryDistrictId,
            Long militaryUnitId,
            boolean active,
            @Size(min = 8, max = 100) String newPassword) {
    }
}
