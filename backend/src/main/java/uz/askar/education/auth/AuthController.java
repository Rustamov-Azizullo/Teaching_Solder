package uz.askar.education.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.auth.AuthDtos.LoginRequest;
import uz.askar.education.auth.AuthDtos.LoginResponse;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.users.UserDtos.UserDto;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autentifikatsiya", description = "Tizimga kirish (JWT)")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/2fa/setup")
    public AuthDtos.TwoFactorSetup setup() {
        return authService.setupTwoFactor(currentUser.username());
    }

    @PostMapping("/2fa/enable")
    public void enable(@Valid @RequestBody AuthDtos.OtpRequest request) {
        authService.enableTwoFactor(currentUser.username(), request.code());
    }

    @PostMapping("/2fa/disable")
    public void disable(@Valid @RequestBody AuthDtos.OtpRequest request) {
        authService.disableTwoFactor(currentUser.username(), request.code());
    }

    @GetMapping("/me")
    public UserDto me() {
        return authService.me(currentUser.username());
    }
}
