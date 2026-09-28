package uz.askar.education.auth;

import jakarta.validation.constraints.NotBlank;
import uz.askar.education.users.UserDtos.UserDto;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password, String otp) {
    }

    public record LoginResponse(String accessToken, long expiresInSeconds, UserDto user,
                                boolean twoFactorEnabled, boolean twoFactorSetupRequired) {
    }

    public record TwoFactorSetup(String secret, String otpauthUri) {
    }

    public record OtpRequest(@NotBlank String code) {
    }
}
