package uz.askar.education.security;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.security.PermissionManagementService.RolePermissionEntry;
import uz.askar.education.security.PermissionManagementService.UserPermissionEntry;
import uz.askar.education.security.PermissionManagementService.UserPermissionState;

/** Ruxsatlarni boshqarish (faqat SuperAdmin / Mega SuperAdmin; statik tekshiruv). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Ruxsatlar", description = "Rol-ruxsat matritsasi va foydalanuvchiga shaxsiy ruxsatlar")
public class PermissionController {

    private final PermissionManagementService service;

    @GetMapping("/role-permissions")
    @PreAuthorize(Access.PERMISSION_MANAGE)
    public List<RolePermissionEntry> roleMatrix() {
        return service.roleMatrix();
    }

    @PutMapping("/role-permissions")
    @PreAuthorize(Access.PERMISSION_MANAGE)
    public List<RolePermissionEntry> updateRoleMatrix(@RequestBody List<RolePermissionEntry> entries) {
        return service.updateRoleMatrix(entries);
    }

    @GetMapping("/users/{id}/permissions")
    @PreAuthorize(Access.PERMISSION_MANAGE)
    public List<UserPermissionState> userPermissions(@PathVariable Long id) {
        return service.userPermissions(id);
    }

    @PutMapping("/users/{id}/permissions")
    @PreAuthorize(Access.PERMISSION_MANAGE)
    public List<UserPermissionState> updateUserPermissions(@PathVariable Long id,
                                                           @RequestBody List<UserPermissionEntry> entries) {
        return service.updateUserPermissions(id, entries);
    }
}
