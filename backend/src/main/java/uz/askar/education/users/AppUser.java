package uz.askar.education.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.locations.Location;
import uz.askar.education.security.Role;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    /** Hududiy biriktirish; respublika darajasidagi rollar (SuperAdmin, Mega SuperAdmin) uchun {@code null}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private boolean active = true;
    private int failedAttempts;
    private LocalDateTime lockedUntil;

    @Column(name = "totp_secret")
    private String totpSecret;

    private boolean totpEnabled;

    /** Okrug darajasidagi foydalanuvchi uchun okrug, qism darajasidagi uchun qismning okrugi. */
    public Long effectiveDistrictId() {
        return location != null ? location.districtId() : null;
    }

    /** Qism darajasidagi foydalanuvchi uchun qism, aks holda {@code null}. */
    public Long effectiveUnitId() {
        return location != null ? location.unitId() : null;
    }
}
