package uz.askar.education.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import uz.askar.education.common.ForbiddenException;

/**
 * Joriy so'rovni yuborgan foydalanuvchi (JWT da'volaridan). Okrug/qism da'volari tizimga kirishda
 * foydalanuvchining {@code Location} yozuvidan hisoblanadi (qarang: {@code TokenService}).
 */
@Component
public class CurrentUser {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_DISTRICT_ID = "districtId";
    public static final String CLAIM_UNIT_ID = "unitId";

    public Long id() {
        return jwt().getClaim(CLAIM_USER_ID);
    }

    public String username() {
        return jwt().getSubject();
    }

    public Role role() {
        return Role.valueOf(jwt().getClaimAsString(CLAIM_ROLE));
    }

    public AccessScope scope() {
        Jwt token = jwt();
        return new AccessScope(role().scopeLevel(), token.getClaim(CLAIM_DISTRICT_ID), token.getClaim(CLAIM_UNIT_ID));
    }

    private Jwt jwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw new ForbiddenException("Foydalanuvchi aniqlanmadi");
    }
}
