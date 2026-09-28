package uz.askar.education.users;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.security.Access;
import uz.askar.education.security.Role;
import uz.askar.education.users.UserDtos.CreateUserRequest;
import uz.askar.education.users.UserDtos.UpdateUserRequest;
import uz.askar.education.users.UserDtos.UserDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Foydalanuvchilar", description = "Foydalanuvchilar va rollarni boshqarish (administrator)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize(Access.ADMIN)
    public List<UserDto> list() {
        return userService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.ADMIN)
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.ADMIN)
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @GetMapping("/roles")
    @PreAuthorize(Access.ADMIN)
    public List<RoleOption> roles() {
        return Arrays.stream(Role.values())
                .map(role -> new RoleOption(role.name(), role.label(), role.scopeLevel().name()))
                .toList();
    }

    public record RoleOption(String code, String label, String scopeLevel) {
    }
}
