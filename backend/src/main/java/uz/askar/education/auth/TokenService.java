package uz.askar.education.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import uz.askar.education.config.JwtProperties;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.ScopeLevel;
import uz.askar.education.users.AppUser;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public Duration lifetime() {
        return Duration.ofMinutes(properties.ttlMinutes());
    }

    /**
     * Token hali ham foydalanuvchining joriy holatiga mos ekanini tekshiradi: hisob faol, rol va hudud da'volari
     * o'zgarmagan. Aks holda foydalanuvchi o'chirilgan yoki boshqa qismga o'tkazilgan bo'lsa ham eski token yaroqli qolardi.
     */
    public boolean matches(Jwt token, AppUser user) {
        if (!user.isActive() || !user.getRole().name().equals(token.getClaimAsString(CurrentUser.CLAIM_ROLE))) {
            return false;
        }
        if (user.getRole().scopeLevel() == ScopeLevel.REPUBLIC) {
            return true;
        }
        return Objects.equals(user.effectiveDistrictId(), longClaim(token, CurrentUser.CLAIM_DISTRICT_ID))
                && Objects.equals(user.effectiveUnitId(), longClaim(token, CurrentUser.CLAIM_UNIT_ID));
    }

    private static Long longClaim(Jwt token, String name) {
        Object value = token.getClaim(name);
        return value instanceof Number number ? number.longValue() : null;
    }

    public String issue(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime()))
                .claim(CurrentUser.CLAIM_USER_ID, user.getId())
                .claim(CurrentUser.CLAIM_ROLE, user.getRole().name());
        // Vazirlik darajasidagi rollar uchun hudud da'volari berilmaydi: ular har doim butun vazirlikni qamraydi.
        if (user.getRole().scopeLevel() != ScopeLevel.REPUBLIC) {
            Long districtId = user.effectiveDistrictId();
            Long unitId = user.effectiveUnitId();
            if (districtId != null) {
                claims.claim(CurrentUser.CLAIM_DISTRICT_ID, districtId);
            }
            if (unitId != null) {
                claims.claim(CurrentUser.CLAIM_UNIT_ID, unitId);
            }
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
