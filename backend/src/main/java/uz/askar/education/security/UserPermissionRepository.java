package uz.askar.education.security;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    boolean existsByUserIdAndPermission(Long userId, Permission permission);

    List<UserPermission> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
