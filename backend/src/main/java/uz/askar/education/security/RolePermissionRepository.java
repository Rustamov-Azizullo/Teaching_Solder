package uz.askar.education.security;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    boolean existsByRoleAndPermission(Role role, Permission permission);

    List<RolePermission> findByRole(Role role);

    List<RolePermission> findByRoleIn(Collection<Role> roles);

    void deleteByRoleAndPermission(Role role, Permission permission);
}
