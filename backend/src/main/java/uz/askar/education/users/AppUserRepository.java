package uz.askar.education.users;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.askar.education.security.Role;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AppUser> findByRoleAndMilitaryUnitIdAndActiveTrue(Role role, Long unitId);

    List<AppUser> findAllByOrderByFullNameAsc();

    List<AppUser> findByRoleInAndActiveTrue(java.util.Collection<Role> roles);

    List<AppUser> findByRoleInAndMilitaryUnitIdAndActiveTrue(java.util.Collection<Role> roles, Long unitId);

    List<AppUser> findByRoleInAndMilitaryDistrictIdAndActiveTrue(java.util.Collection<Role> roles, Long districtId);
}
