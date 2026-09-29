package uz.askar.education.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.users.AppUserRepository;

/** Har bir so'rovda token egasining hisobi hali faol va rol/hudud o'zgarmaganini tekshiradi. */
@Component
@RequiredArgsConstructor
public class TokenStateValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error STALE_TOKEN =
            new OAuth2Error("invalid_token", "Token eskirgan: hisob holati o'zgargan", null);

    private final AppUserRepository users;
    private final TokenService tokens;

    @Override
    @Transactional(readOnly = true)
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Object userId = token.getClaim(CurrentUser.CLAIM_USER_ID);
        if (!(userId instanceof Number id)) {
            return OAuth2TokenValidatorResult.failure(STALE_TOKEN);
        }
        boolean current = users.findWithLocationById(id.longValue()).map(user -> tokens.matches(token, user)).orElse(false);
        return current ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(STALE_TOKEN);
    }
}
