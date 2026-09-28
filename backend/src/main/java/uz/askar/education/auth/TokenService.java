package uz.askar.education.auth;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import uz.askar.education.config.JwtProperties;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.users.AppUser;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public Duration lifetime() {
        return Duration.ofMinutes(properties.ttlMinutes());
    }

    public String issue(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime()))
                .claim(CurrentUser.CLAIM_USER_ID, user.getId())
                .claim(CurrentUser.CLAIM_ROLE, user.getRole().name());
        if (user.effectiveDistrictId() != null) {
            claims.claim(CurrentUser.CLAIM_DISTRICT_ID, user.effectiveDistrictId());
        }
        if (user.getMilitaryUnit() != null) {
            claims.claim(CurrentUser.CLAIM_UNIT_ID, user.getMilitaryUnit().getId());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
