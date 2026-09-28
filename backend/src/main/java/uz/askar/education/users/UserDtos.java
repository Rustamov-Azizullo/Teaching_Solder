package uz.askar.education.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import uz.askar.education.locations.Location;
import uz.askar.education.locations.LocationLevel;
import uz.askar.education.security.Permission;
import uz.askar.education.security.Role;

public final class UserDtos {

    private UserDtos() {
    }

    /**
     * Foydalanuvchi ma'lumotlari. {@code militaryDistrictId}/{@code militaryUnitId}/{@code militaryUnitName}
     * {@code location} dan hisoblanadi va mavjud frontend bilan moslik uchun saqlangan.
     * {@code permissions} — foydalanuvchi amalda ega bo'lgan barcha ruxsatlar (rol + shaxsiy).
     */
    public record UserDto(Long id, String username, String fullName, Role role, String roleLabel,
                          Long locationId, String locationName, LocationLevel locationLevel,
                          Long militaryDistrictId, Long militaryUnitId, String militaryUnitName, boolean active,
                          List<String> permissions) {

        public static UserDto from(AppUser user, Collection<Permission> permissions) {
            Location location = user.getLocation();
            boolean isUnit = location != null && location.getLevel() == LocationLevel.UNIT;
            return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getRole(),
                    user.getRole().label(),
                    location != null ? location.getId() : null,
                    location != null ? location.getName() : null,
                    location != null ? location.getLevel() : null,
                    user.effectiveDistrictId(), user.effectiveUnitId(),
                    isUnit ? location.getMilitaryUnit().getName() : null,
                    user.isActive(),
                    permissions.stream().map(Permission::name).sorted().toList());
        }
    }

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 60) String username,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank String fullName,
            @NotNull Role role,
            Long locationId) {
    }

    public record UpdateUserRequest(
            @NotBlank String fullName,
            @NotNull Role role,
            Long locationId,
            boolean active,
            @Size(min = 8, max = 100) String newPassword) {
    }
}
